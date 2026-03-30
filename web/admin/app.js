// Constantes de roles
const ADMIN_EMAILS = [
    'admin@test.com',
    'admin@ngd.com'
];

// Variables globales para sesión
let currentUserRole = 'guest';
let currentUserEmail = '';

// Variables para limpiar listeners de Firestore
let conversationsUnsubscribe = null;
let messagesUnsubscribe = null;

// Variables para debounce
let searchTimeout = null;
const DEBOUNCE_DELAY = 300;

// Variables para estado de conexión
let isOnline = navigator.onLine;
let connectionStatusEl = null;

// Función para mostrar toast notifications
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    // Auto-remove after 4 seconds
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Función para crear skeleton de conversación
function createConversationSkeleton() {
    const div = document.createElement('div');
    div.className = 'skeleton-item';
    div.innerHTML = `
        <div class="skeleton-header">
            <div class="skeleton skeleton-name"></div>
            <div class="skeleton skeleton-timestamp"></div>
        </div>
        <div class="skeleton skeleton-message"></div>
        <div class="skeleton skeleton-id"></div>
    `;
    return div;
}

// Función para mostrar skeletons de conversaciones
function showConversationSkeletons(count = 5) {
    const conversationsList = document.getElementById('conversations-list');
    if (!conversationsList) return;

    conversationsList.innerHTML = '';
    for (let i = 0; i < count; i++) {
        conversationsList.appendChild(createConversationSkeleton());
    }
}

// Función para mostrar skeletons de mensajes
function showMessageSkeletons(count = 8) {
    const messagesContainer = document.getElementById('messages-container');
    if (!messagesContainer) return;

    messagesContainer.innerHTML = '';
    for (let i = 0; i < count; i++) {
        const div = document.createElement('div');
        div.className = 'skeleton-item';
        div.innerHTML = `
            <div class="skeleton" style="width: ${Math.random() * 40 + 30}%; height: 16px; margin-bottom: 8px;"></div>
            <div class="skeleton" style="width: ${Math.random() * 60 + 40}%; height: 14px;"></div>
        `;
        messagesContainer.appendChild(div);
    }
}

// Función para actualizar estado de conexión
function updateConnectionStatus() {
    if (!connectionStatusEl) {
        connectionStatusEl = document.createElement('div');
        connectionStatusEl.id = 'connection-status';
        connectionStatusEl.className = 'connection-status';
        document.body.appendChild(connectionStatusEl);
    }

    if (isOnline) {
        connectionStatusEl.textContent = 'En línea';
        connectionStatusEl.className = 'connection-status online';
    } else {
        connectionStatusEl.textContent = 'Sin conexión - Algunas funciones pueden no estar disponibles';
        connectionStatusEl.className = 'connection-status offline';
    }
}

// Detectar cambios de conexión
function setupConnectionDetection() {
    window.addEventListener('online', () => {
        isOnline = true;
        updateConnectionStatus();
        showToast('Conexión restaurada', 'success');
    });

    window.addEventListener('offline', () => {
        isOnline = false;
        updateConnectionStatus();
        showToast('Conexión perdida', 'warning');
    });

    // Initial status
    updateConnectionStatus();
}

// Configurar FCM topic messaging (simulado - requiere service worker)
function setupFCMTopics() {
    // En producción, esto se conectaría con FCM
    // Por ahora, solo registramos el soporte
    if ('Notification' in window) {
        console.log('FCM Topic Messaging soportado');
        // En implementación real, suscribirse a tópicos aquí
    }
}

// Funciones de autenticación
function login(email, password) {
    return auth.signInWithEmailAndPassword(email, password)
        .then((userCredential) => {
            const user = userCredential.user;
            
            // Verificar rol del usuario
            const isAdmin = ADMIN_EMAILS.includes(user.email.toLowerCase());
            currentUserRole = isAdmin ? 'admin' : 'client';
            currentUserEmail = user.email;
            
            return user;
        })
        .catch((error) => {
            console.error('Error de autenticación:', error.message);
            throw error;
        });
}

function logout() {
    // Limpiar listeners de Firestore
    if (conversationsUnsubscribe) {
        conversationsUnsubscribe();
        conversationsUnsubscribe = null;
    }
    if (messagesUnsubscribe) {
        messagesUnsubscribe();
        messagesUnsubscribe = null;
    }
    
    // Limpiar sesión
    currentUserRole = 'guest';
    currentUserEmail = '';
    
    return auth.signOut()
        .then(() => {
            showLogin();
        })
        .catch((error) => {
            console.error('Error al cerrar sesión:', error.message);
        });
}

function onAuthStateChange(callback) {
    auth.onAuthStateChanged((user) => {
        if (user) {
            // Verificar rol
            const isAdmin = ADMIN_EMAILS.includes(user.email.toLowerCase());
            currentUserRole = isAdmin ? 'admin' : 'client';
            currentUserEmail = user.email;
        } else {
            currentUserRole = 'guest';
            currentUserEmail = '';
        }
        callback(user);
    });
}

// Cargar conversaciones desde Firestore
function loadConversations(searchTerm = '') {
    const conversationsList = document.getElementById('conversations-list');
    if (!conversationsList) return;

    showConversationSkeletons();

    // Limpiar listener anterior
    if (conversationsUnsubscribe) {
        conversationsUnsubscribe();
    }

    conversationsUnsubscribe = db.collection('companies').doc('NGDStudios')
        .collection('channels')
        .orderBy('lastMessageAt', 'desc')
        .limit(50)
        .onSnapshot((snapshot) => {
            conversationsList.innerHTML = '';
            
            if (snapshot.empty) {
                conversationsList.innerHTML = '<p class="empty">No hay conversaciones</p>';
                return;
            }

            snapshot.forEach((doc) => {
                const conversation = doc.data();
                const conversationEl = createConversationElement(doc.id, conversation);
                
                // Filter by search term if provided
                if (searchTerm) {
                    const userName = (conversation.userName || conversation.userEmail || '').toLowerCase();
                    const lastMessage = (conversation.lastMessage || '').toLowerCase();
                    const searchLower = searchTerm.toLowerCase();
                    
                    if (!userName.includes(searchLower) && !lastMessage.includes(searchLower)) {
                        return;
                    }
                }
                
                conversationsList.appendChild(conversationEl);
            });
            
            // Show empty state if no results after filtering
            if (conversationsList.children.length === 0) {
                conversationsList.innerHTML = '<p class="empty">No se encontraron conversaciones</p>';
            }
        }, (error) => {
            console.error('Error al cargar conversaciones:', error);
            conversationsList.innerHTML = '<p class="error">Error al cargar conversaciones</p>';
        });
}

function createConversationElement(docId, conversation) {
    const div = document.createElement('div');
    div.className = 'conversation-item';
    div.onclick = () => openConversation(docId, conversation);

    const userName = conversation.userName || conversation.userEmail || 'Usuario desconocido';
    const lastMessage = conversation.lastMessage || 'Sin mensajes';
    const timestamp = conversation.lastMessageAt ? conversation.lastMessageAt.toDate().toLocaleString() : '';

    div.innerHTML = `
        <div class="conversation-header">
            <span class="user-name">${userName}</span>
            <span class="timestamp">${timestamp}</span>
        </div>
        <div class="last-message">${lastMessage}</div>
        <div class="conversation-id">ID: ${docId}</div>
    `;

    return div;
}

// Abrir conversación y cargar mensajes
let currentConversationId = null;

function openConversation(conversationId, conversation) {
    currentConversationId = conversationId;
    
    const chatPanel = document.getElementById('chat-panel');
    const chatHeader = document.getElementById('chat-header');
    const messagesContainer = document.getElementById('messages-container');
    
    if (chatPanel) {
        chatPanel.style.display = 'block';
    }
    
    if (chatHeader) {
        const userName = conversation.userName || conversation.userEmail || 'Usuario';
        chatHeader.innerHTML = `
            <h3>${userName}</h3>
            <button onclick="closeChat()">Cerrar</button>
        `;
    }

    loadMessages(conversationId);
}

function loadMessages(conversationId) {
    const messagesContainer = document.getElementById('messages-container');
    if (!messagesContainer) return;

    showMessageSkeletons();

    // Limpiar listener anterior
    if (messagesUnsubscribe) {
        messagesUnsubscribe();
    }

    messagesUnsubscribe = db.collection('companies').doc('NGDStudios')
        .collection('channels')
        .doc(conversationId)
        .collection('messages')
        .orderBy('timestamp', 'asc')
        .onSnapshot((snapshot) => {
            messagesContainer.innerHTML = '';
            
            if (snapshot.empty) {
                messagesContainer.innerHTML = '<p class="empty">No hay mensajes</p>';
                return;
            }

            snapshot.forEach((doc) => {
                const message = doc.data();
                const messageEl = createMessageElement(message);
                messagesContainer.appendChild(messageEl);
            });

            // Scroll al final
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }, (error) => {
            console.error('Error al cargar mensajes:', error);
            messagesContainer.innerHTML = '<p class="error">Error al cargar mensajes</p>';
        });
}

function createMessageElement(message) {
    const div = document.createElement('div');
    div.className = `message ${message.senderType || 'user'}`;

    const content = message.content || '';
    const timestamp = message.timestamp ? message.timestamp.toDate().toLocaleString() : '';
    const sender = message.senderType === 'ai' ? 'Asistente IA' : 'Usuario';

    div.innerHTML = `
        <div class="message-content">${content}</div>
        <div class="message-meta">
            <span class="sender">${sender}</span>
            <span class="timestamp">${timestamp}</span>
        </div>
    `;

    return div;
}

function closeChat() {
    // Limpiar listener de mensajes
    if (messagesUnsubscribe) {
        messagesUnsubscribe();
        messagesUnsubscribe = null;
    }
    
    const chatPanel = document.getElementById('chat-panel');
    if (chatPanel) {
        chatPanel.style.display = 'none';
    }
    currentConversationId = null;
}

// Enviar mensaje (respuesta del admin)
function sendMessage(content) {
    if (!currentConversationId || !content.trim()) return;

    const messageData = {
        content: content.trim(),
        senderType: 'admin',
        senderEmail: auth.currentUser?.email || 'admin@admin.com',
        timestamp: firebase.firestore.FieldValue.serverTimestamp(),
        createdAt: new Date().toISOString()
    };

    return db.collection('companies').doc('NGDStudios')
        .collection('channels')
        .doc(currentConversationId)
        .collection('messages')
        .add(messageData)
        .then(() => {
            // Actualizar último mensaje en la conversación
            return db.collection('companies').doc('NGDStudios')
                .collection('channels')
                .doc(currentConversationId)
                .update({
                    lastMessage: content.trim(),
                    lastMessageAt: firebase.firestore.FieldValue.serverTimestamp()
                });
        })
        .catch((error) => {
            console.error('Error al enviar mensaje:', error);
            throw error;
        });
}

// Funciones de configuración de IA
function loadAISettings() {
    const settingsRef = db.collection('settings').doc('ai_config');
    
    settingsRef.get()
        .then((doc) => {
            if (doc.exists) {
                const data = doc.data();
                document.getElementById('ai-model').value = data.model || 'gemini-2.0-flash';
                document.getElementById('ai-temperature').value = data.temperature || 0.7;
                document.getElementById('ai-system-prompt').value = data.systemPrompt || '';
                document.getElementById('ai-enabled').checked = data.enabled !== false;
            }
        })
        .catch((error) => {
            console.error('Error al cargar configuración de IA:', error);
        });
}

function saveAISettings() {
    const settings = {
        model: document.getElementById('ai-model').value,
        temperature: parseFloat(document.getElementById('ai-temperature').value),
        systemPrompt: document.getElementById('ai-system-prompt').value,
        enabled: document.getElementById('ai-enabled').checked,
        updatedAt: firebase.firestore.FieldValue.serverTimestamp(),
        updatedBy: auth.currentUser?.email || 'admin@admin.com'
    };

    return db.collection('settings').doc('ai_config')
        .set(settings, { merge: true })
        .then(() => {
            showToast('Configuración guardada correctamente', 'success');
        })
        .catch((error) => {
            console.error('Error al guardar configuración:', error);
            showToast('Error al guardar configuración', 'error');
        });
}

// Funciones de UI
function showLogin() {
    document.getElementById('login-section').style.display = 'flex';
    document.getElementById('dashboard-section').style.display = 'none';
}

function showDashboard() {
    document.getElementById('login-section').style.display = 'none';
    document.getElementById('dashboard-section').style.display = 'block';
    
    const isAdmin = currentUserRole === 'admin';
    
    // Mostrar email y rol del usuario
    const userEmailEl = document.getElementById('user-email');
    const userRoleEl = document.getElementById('user-role');
    const adminPanel = document.getElementById('admin-panel');
    const tabSettingsBtn = document.querySelector('[data-tab="settings"]');
    
    if (userEmailEl) {
        userEmailEl.textContent = currentUserEmail;
    }
    
    // Mostrar rol
    if (userRoleEl) {
        userRoleEl.textContent = isAdmin ? '👑 Admin' : '👤 Cliente';
        userRoleEl.className = isAdmin ? 'badge badge-admin' : 'badge badge-client';
    }
    
    // Mostrar/ocultar panel de admin según rol
    if (adminPanel) {
        adminPanel.style.display = isAdmin ? 'block' : 'none';
    }
    
    // Ocultar tab de settings para no-admins
    if (tabSettingsBtn) {
        tabSettingsBtn.style.display = isAdmin ? 'block' : 'none';
    }
    
    loadConversations();
    
    // Solo admins pueden cargar configuración de IA
    if (isAdmin) {
        loadAISettings();
    }
}

// Inicializar app
document.addEventListener('DOMContentLoaded', () => {
    onAuthStateChange((user) => {
        if (user) {
            showDashboard();
        } else {
            showLogin();
        }
    });

    // Configurar formulario de login
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const errorMsg = document.getElementById('login-error');

            try {
                await login(email, password);
                errorMsg.textContent = '';
            } catch (error) {
                errorMsg.textContent = error.message;
            }
        });
    }

    // Configurar botón de logout
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }

    // Configurar formulario de envío de mensajes
    const messageForm = document.getElementById('message-form');
    if (messageForm) {
        messageForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const messageInput = document.getElementById('message-input');
            const content = messageInput.value;
            
            try {
                await sendMessage(content);
                messageInput.value = '';
            } catch (error) {
                showToast('Error al enviar mensaje: ' + error.message, 'error');
            }
        });
    }

    // Configurar formulario de settings
    const settingsForm = document.getElementById('ai-settings-form');
    if (settingsForm) {
        settingsForm.addEventListener('submit', (e) => {
            e.preventDefault();
            saveAISettings();
        });
    }

    // Configurar tabs
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            
            document.querySelectorAll('.tab-content').forEach(content => {
                content.classList.remove('active');
            });
            
            const tabId = tab.getAttribute('data-tab');
            document.getElementById(`${tabId}-tab`).classList.add('active');
        });
    });

    // Configurar búsqueda de conversaciones con debounce
    const searchInput = document.getElementById('search-conversations');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                loadConversations(e.target.value);
            }, DEBOUNCE_DELAY);
        });
    }

    // Configurar navegación por teclado
    document.addEventListener('keydown', (e) => {
        // Enter para enviar mensaje si el input está enfocado
        if (e.key === 'Enter' && document.activeElement.id === 'message-input') {
            e.preventDefault();
            const form = document.getElementById('message-form');
            if (form) {
                form.dispatchEvent(new Event('submit'));
            }
        }
        
        // Escape para cerrar chat
        if (e.key === 'Escape') {
            closeChat();
        }
    });

    // Configurar detección de conexión
    setupConnectionDetection();
    
    // Configurar FCM topics
    setupFCMTopics();
});
