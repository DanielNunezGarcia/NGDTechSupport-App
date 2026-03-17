const functions = require("firebase-functions");
const admin = require("firebase-admin");
const https = require("https");

admin.initializeApp();

// ========== CHAT HUMANO ==========

exports.onMessageCreate =
functions.firestore
.document("companies/{companyId}/channels/{channelId}/messages/{messageId}")
.onCreate(async (snap, context) => {

    const { companyId, channelId } = context.params;

    const messageData = snap.data();
    const senderId = messageData.senderId;
    const messageText = messageData.message;

    const channelRef = admin.firestore()
        .collection("companies")
        .doc(companyId)
        .collection("channels")
        .doc(channelId);

    const channelDoc = await channelRef.get();
    const channelData = channelDoc.data();

    const members = channelData.members || {};
    const mutedUsers = channelData.mutedUsers || {};
    const typing = channelData.typing || {};

    // Eliminar typing del remitente
    if (typing[senderId]) {
        await channelRef.update({
            [`typing.${senderId}`]: admin.firestore.FieldValue.delete()
        });
    }

    const unreadUpdates = {};
    const tokens = [];

    for (const uid in members) {

        if (uid !== senderId) {

            unreadUpdates[`unreadCount.${uid}`] =
                admin.firestore.FieldValue.increment(1);

            const isMuted = mutedUsers[uid] === true;

            if (!isMuted) {
                const userDoc = await admin.firestore()
                    .collection("users")
                    .doc(uid)
                    .get();

                const userData = userDoc.data();

                if (userData && userData.fcmToken) {
                    tokens.push(userData.fcmToken);
                }
            }
        }
    }

    // Actualizar unread
    await channelRef.update(unreadUpdates);

    // Actualizar lastMessage
    await channelRef.update({
        lastMessage: messageText,
        lastMessageAt: admin.firestore.FieldValue.serverTimestamp()
    });

    // Enviar notificaciones si hay tokens
    if (tokens.length > 0) {

        const payload = {
            notification: {
                title: "Nuevo mensaje",
                body: messageText
            },
            data: {
                companyId: companyId,
                channelId: channelId
            }
        };

        await admin.messaging().sendEachForMulticast({
            tokens: tokens,
            ...payload
        });
    }

    return null;
});

exports.onTypingUpdate =
functions.firestore
.document("companies/{companyId}/channels/{channelId}")
.onUpdate(async (change, context) => {

    const { companyId, channelId } = context.params;
    const newData = change.after.data();
    const typing = newData.typing || {};

    // Limpiar typing antiguo (más de 30 segundos)
    const now = Date.now();
    const expiredTyping = {};

    for (const uid in typing) {
        const timestamp = typing[uid]?.timestamp || 0;
        if (now - timestamp > 30000) {
            expiredTyping[uid] = admin.firestore.FieldValue.delete();
        }
    }

    if (Object.keys(expiredTyping).length > 0) {
        await change.after.ref.update(expiredTyping);
    }

    return null;
});

// ========== CHAT CON IA ==========

exports.chatWithAI = functions.https.onCall(async (data, context) => {
    // Verificar autenticación
    if (!context.auth) {
        throw new functions.https.HttpsError(
            'unauthenticated',
            'Debes estar autenticado para usar el chat con IA'
        );
    }

    const { companyId, message, conversationId } = data;
    const userId = context.auth.uid;

    // Validar entrada
    if (!message || message.trim().length === 0) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'El mensaje no puede estar vacío'
        );
    }

    if (message.length > 10000) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'El mensaje es demasiado largo'
        );
    }

    // Obtener API Key de forma segura (usar Firebase Secrets en producción)
    const apiKey = process.env.OPENAI_API_KEY;
    if (!apiKey) {
        throw new functions.https.HttpsError(
            'internal',
            'Configuración de IA no disponible'
        );
    }

    try {
        // Obtener o crear conversación
        let convId = conversationId;
        let conversationRef;
        
        if (!convId) {
            // Crear nueva conversación
            const newConvRef = admin.firestore()
                .collection("companies")
                .doc(companyId)
                .collection("ai_conversations")
                .doc();
            
            await newConvRef.set({
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                userId: userId,
                companyId: companyId,
                title: "Nueva conversación"
            });
            
            convId = newConvRef.id;
            conversationRef = newConvRef.collection("messages");
        } else {
            conversationRef = admin.firestore()
                .collection("companies")
                .doc(companyId)
                .collection("ai_conversations")
                .doc(convId)
                .collection("messages");
        }

        // Guardar mensaje del usuario
        const userMessageRef = conversationRef.doc();
        await userMessageRef.set({
            role: "user",
            content: message,
            timestamp: admin.firestore.FieldValue.serverTimestamp()
        });

        // Obtener historial (últimos 10 mensajes)
        const historySnapshot = await conversationRef
            .orderBy("timestamp", "desc")
            .limit(10)
            .get();
        
        const history = historySnapshot.docs
            .reverse()
            .map(doc => ({
                role: doc.data().role,
                content: doc.data().content
            }));

        // Llamar a OpenAI
        const aiResponse = await callOpenAI(apiKey, history, message);

        // Guardar respuesta de IA
        const aiMessageRef = conversationRef.doc();
        await aiMessageRef.set({
            role: "assistant",
            content: aiResponse,
            timestamp: admin.firestore.FieldValue.serverTimestamp()
        });

        return {
            conversationId: convId,
            response: aiResponse
        };

    } catch (error) {
        console.error("Error en chatWithAI:", error);
        
        // Si la IA falla, sugerir hablar con humano
        return {
            conversationId: convId || null,
            response: "Disculpa, tuve un problema al procesar tu mensaje. ¿Prefieres hablar con un agente de soporte humano?",
            fallbackToHuman: true
        };
    }
});

// Función auxiliar para llamar a OpenAI
function callOpenAI(apiKey, history, currentMessage) {
    return new Promise((resolve, reject) => {
        const messages = [
            {
                role: "system",
                content: "Eres un asistente de soporte técnico profesional de NGD Tech Solutions. Ayudas a los usuarios con sus aplicaciones, resolve dudas técnicas y proporcionas información sobre el estado de sus proyectos. Sé amable, profesional y conciso."
            },
            ...history,
            { role: "user", content: currentMessage }
        ];

        const postData = JSON.stringify({
            model: "gpt-3.5-turbo",
            messages: messages,
            max_tokens: 500,
            temperature: 0.7
        });

        const options = {
            hostname: "api.openai.com",
            path: "/v1/chat/completions",
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${apiKey}`
            }
        };

        const req = https.request(options, (res) => {
            let data = "";

            res.on("data", (chunk) => {
                data += chunk;
            });

            res.on("end", () => {
                try {
                    const parsed = JSON.parse(data);
                    if (parsed.choices && parsed.choices[0]) {
                        resolve(parsed.choices[0].message.content);
                    } else {
                        reject(new Error("Respuesta inválida de OpenAI"));
                    }
                } catch (e) {
                    reject(e);
                }
            });
        });

        req.on("error", reject);
        req.write(postData);
        req.end();
    });
}

// ========== TRANSFERIR A HUMANO ==========

exports.transferToHuman = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError(
            'unauthenticated',
            'Debes estar autenticado'
        );
    }

    const { companyId, conversationId, userId } = data;

    // Crear canal de soporte humano
    const channelRef = admin.firestore()
        .collection("companies")
        .doc(companyId)
        .collection("channels")
        .doc(`support_${userId}`);

    await channelRef.set({
        name: "Soporte Humano",
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        members: {
            [userId]: { role: "client" },
            ["admin"]: { role: "admin" }
        },
        mutedUsers: {},
        unreadCount: {
            [userId]: 0,
            ["admin"]: 1
        },
        isArchived: false,
        pinned: false,
        type: "human_support"
    });

    return { success: true, channelId: `support_${userId}` };
});

// ========== CHAT HÍBRIDO - PROCESAR MENSAJE ==========

exports.onHybridMessage = functions.firestore
.document("companies/{companyId}/channels/{channelId}/messages/{messageId}")
.onCreate(async (snap, context) => {
    const { companyId, channelId } = context.params;
    const messageData = snap.data();
    const senderId = messageData.senderId;
    const messageText = messageData.message;

    // Si el mensaje es de un agente o de la IA, no procesar
    if (messageData.senderType === "agent" || messageData.senderType === "ai") {
        return null;
    }

    // Obtener configuración del canal
    const channelRef = admin.firestore()
        .collection("companies")
        .doc(companyId)
        .collection("channels")
        .doc(channelId);
    
    const channelDoc = await channelRef.get();
    const channelData = channelDoc.data() || {};

    // Si no está habilitado el chat híbrido, salir
    if (channelData.chatMode === "human_only") {
        return null;
    }

    // Keywords que disparan transferencia a humano
    const escalationKeywords = [
        "hablar con persona", "hablar con agente", "necesito ayuda humana",
        "no me puedes ayudar", "quiero hablar con alguien", "atención personal",
        "urgente", "hablar con un humano", "pasame con un agente",
        "hablar con alguien", "quiero ayuda humana"
    ];

    const needsHuman = escalationKeywords.some(keyword => 
        messageText.toLowerCase().includes(keyword.toLowerCase())
    );

    // Si el usuario pide hablar con un humano
    if (needsHuman) {
        const fallbackMessage = `
Entiendo que prefieres hablar con una persona. 😊

He transferido tu conversación a nuestro equipo de soporte.
Un agente humano revisará tu caso y te contactará pronto.

Mientras tanto, ¿puedes darme más detalles sobre tu problema?
Así podremos acelerar la atención cuando un agente te atienda.
        `.trim();

        // Enviar mensaje de la IA
        const aiMessageRef = channelRef.collection("messages").doc();
        await aiMessageRef.set({
            message: fallbackMessage,
            senderId: "ai_assistant",
            senderName: "Asistente IA",
            senderType: "ai",
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
            status: "sent"
        });

        // Marcar canal como pendiente de atención humana
        await channelRef.update({
            pendingHumanSupport: true,
            waitingSince: admin.firestore.FieldValue.serverTimestamp()
        });

        // Notificar a los agentes
        await notifyAgents(companyId, channelId, senderId);

        return null;
    }

    // Obtener API Key
    const apiKey = process.env.OPENAI_API_KEY;
    if (!apiKey) {
        return null;
    }

    // Obtener contexto del negocio/empresa
    const businessContext = await getBusinessContext(companyId);

    // Llamar a OpenAI con contexto
    const aiResponse = await callOpenAIWithContext(apiKey, messageText, businessContext);

    // Guardar respuesta de IA en el chat
    const aiMessageRef = channelRef.collection("messages").doc();
    await aiMessageRef.set({
        message: aiResponse,
        senderId: "ai_assistant",
        senderName: "Asistente IA",
        senderType: "ai",
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        status: "sent"
    });

    // Actualizar último mensaje del canal
    await channelRef.update({
        lastMessage: `IA: ${aiResponse.substring(0, 50)}...`,
        lastMessageAt: admin.firestore.FieldValue.serverTimestamp()
    });

    return null;
});

// ========== MENSAJE DE BIENVENIDA IA ==========

exports.sendAiGreeting = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'Debes estar autenticado');
    }

    const { companyId, channelId } = data;

    // Mensajes de bienvenida por defecto
    const defaultGreeting = [
        "¡Hola! 👋 Soy el asistente de NGD Tech Solutions.",
        "Somos especialistas en desarrollo de apps móviles con IA y soporte técnico.",
        "¿En qué puedo ayudarte hoy?",
        "",
        "Puedo asistirte con:",
        "📱 Desarrollo de apps (Android/iOS)",
        "🤖 Integración de Inteligencia Artificial",
        "🐛 Soporte técnico y mantenimiento",
        "📊 Estado de tus proyectos",
        "💬 Consultas generales",
        "",
        "O escribe directamente tu duda y te ayudo.",
        "Si prefieres hablar con una persona, escribe 'hablar con agente'",
        "y te conectaré con nuestro equipo. 😊"
    ].join("\n");

    // Enviar mensaje de bienvenida
    const channelRef = admin.firestore()
        .collection("companies")
        .doc(companyId)
        .collection("channels")
        .doc(channelId);

    const greetingRef = channelRef.collection("messages").doc();
    await greetingRef.set({
        message: defaultGreeting,
        senderId: "ai_assistant",
        senderName: "Asistente IA",
        senderType: "ai",
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        status: "sent"
    });

    return { success: true };
});

// ========== FUNCIONES AUXILIARES ==========

async function getBusinessContext(companyId) {
    try {
        const companyDoc = await admin.firestore()
            .collection("companies")
            .doc(companyId)
            .get();
        
        const companyData = companyDoc.data();
        
        if (!companyData) {
            return "NGD Tech Solutions es una empresa de desarrollo de apps móviles con IA y soporte técnico.";
        }

        return `
Empresa: ${companyData.name || 'NGD Tech Solutions'}
Servicios: Desarrollo de apps móviles (Android/iOS), Integración de IA, Soporte técnico, Mantenimiento.
Sectores: Comercio, Hostelería, Informática, Logística.
Clientes: Empresas, Freelancers, Autónomos.
`;
    } catch (error) {
        return "NGD Tech Solutions es una empresa de desarrollo de apps móviles con IA y soporte técnico.";
    }
}

async function callOpenAIWithContext(apiKey, userMessage, context) {
    const systemMessage = `
Eres el asistente de NGD Tech Solutions, una empresa especializada en:
- Desarrollo de apps móviles (Android/iOS)
- Integración de Inteligencia Artificial
- Soporte técnico y mantenimiento
- Servicios para: Comercio, Hostelería, Informática, Logística
- Clientes: Empresas, Freelancers, Autónomos

INSTRUCCIONES:
1. Sé amable, profesional y conciso
2. Si no puedes resolver el problema, sugiere hablar con un agente humano
3. No inventes información sobre proyectos específicos
4. Puedes consultar sobre estados de proyectos, errores, presupuestos
5. Si el usuario quiere hablar con alguien, usa la palabra "hablar con agente"

Contexto de la empresa:
${context}
`.trim();

    return new Promise((resolve, reject) => {
        const messages = [
            { role: "system", content: systemMessage },
            { role: "user", content: userMessage }
        ];

        const postData = JSON.stringify({
            model: "gpt-3.5-turbo",
            messages: messages,
            max_tokens: 500,
            temperature: 0.7
        });

        const options = {
            hostname: "api.openai.com",
            path: "/v1/chat/completions",
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${apiKey}`
            }
        };

        const req = https.request(options, (res) => {
            let data = "";
            res.on("data", (chunk) => { data += chunk; });
            res.on("end", () => {
                try {
                    const parsed = JSON.parse(data);
                    if (parsed.choices && parsed.choices[0]) {
                        resolve(parsed.choices[0].message.content);
                    } else {
                        resolve("Disculpa, tuve un problema al procesar tu mensaje. ¿Prefieres hablar con un agente humano?");
                    }
                } catch (e) {
                    resolve("Disculpa, tuve un problema al procesar tu mensaje. ¿Prefieres hablar con un agente humano?");
                }
            });
        });

        req.on("error", reject);
        req.write(postData);
        req.end();
    });
}

async function notifyAgents(companyId, channelId, userId) {
    // Obtener usuarios con rol de admin o soporte
    const usersSnapshot = await admin.firestore()
        .collection("users")
        .where("companyId", "==", companyId)
        .where("role", "in", ["admin", "support"])
        .get();

    const tokens = [];
    for (const doc of usersSnapshot.docs) {
        const userData = doc.data();
        if (userData.fcmToken) {
            tokens.push(userData.fcmToken);
        }
    }

    if (tokens.length > 0) {
        await admin.messaging().sendEachForMulticast({
            tokens: tokens,
            notification: {
                title: "Nuevo cliente esperando soporte",
                body: "Un usuario necesita atención humana"
            },
            data: {
                companyId: companyId,
                channelId: channelId
            }
        });
    }
}