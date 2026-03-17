const functions = require("firebase-functions");
const admin = require("firebase-admin");
const OpenAI = require("openai");

admin.initializeApp();

const openai = new OpenAI({
  apiKey: functions.config().openai.api_key,
});

const rateLimitStore = new Map();

const RATE_LIMIT_WINDOW = 60000;
const MAX_REQUESTS_PER_WINDOW = 20;
const MAX_TOKENS_PER_DAY = 100000;

async function checkRateLimit(uid) {
  const now = Date.now();
  const userHistory = rateLimitStore.get(uid) || { requests: [], tokens: 0 };
  
  userHistory.requests = userHistory.requests.filter(t => now - t < RATE_LIMIT_WINDOW);
  
  if (userHistory.requests.length >= MAX_REQUESTS_PER_WINDOW) {
    throw new functions.https.HttpsError(
      "resource-exhausted",
      "Rate limit exceeded. Try again later."
    );
  }
  
  if (userHistory.tokens >= MAX_TOKENS_PER_DAY) {
    throw new functions.https.HttpsError(
      "resource-exhausted",
      "Daily token limit exceeded."
    );
  }
  
  userHistory.requests.push(now);
  rateLimitStore.set(uid, userHistory);
  
  return userHistory;
}

function validateInput(text) {
  if (!text || typeof text !== "string") {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Message text is required."
    );
  }
  
  if (text.length > 10000) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Message too long. Maximum 10000 characters."
    );
  }
  
  const inappropriatePatterns = [
    /<script/i,
    /javascript:/i,
    /on\w+=/i,
  ];
  
  for (const pattern of inappropriatePatterns) {
    if (pattern.test(text)) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Message contains inappropriate content."
      );
    }
  }
  
  return text.trim();
}

function validateUserAccess(db, companyId, userId) {
  return db
    .collection("companies")
    .doc(companyId)
    .collection("members")
    .doc(userId)
    .get()
    .then(snap => snap.exists);
}

exports.chatWithAI = functions
  .runWith({
    secrets: ["OPENAI_API_KEY"],
    memory: "512MB",
    timeoutSeconds: 60,
  })
  .https.onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError(
        "unauthenticated",
        "Authentication required."
      );
    }

    const uid = context.auth.uid;
    const { companyId, message, conversationId, model = "gpt-4" } = data;

    if (!companyId || !message) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "companyId and message are required."
      );
    }

    const hasAccess = await validateUserAccess(
      admin.firestore(),
      companyId,
      uid
    );
    
    if (!hasAccess) {
      throw new functions.https.HttpsError(
        "permission-denied",
        "No access to this company's resources."
      );
    }

    await checkRateLimit(uid);

    const cleanMessage = validateInput(message);

    const conversationRef = admin.firestore()
      .collection("companies")
      .doc(companyId)
      .collection("aiConversations")
      .doc(conversationId);

    const conversationDoc = await conversationRef.get();
    const conversationData = conversationDoc.exists 
      ? conversationDoc.data() 
      : { messages: [], createdAt: admin.firestore.FieldValue.serverTimestamp() };

    const systemMessage = {
      role: "system",
      content: "You are a helpful AI assistant. Respond in a professional manner."
    };

    const userMessage = { role: "user", content: cleanMessage };

    const messages = [systemMessage, ...conversationData.messages, userMessage];

    const startTime = Date.now();
    let response;
    
    try {
      response = await openai.chat.completions.create({
        model: model,
        messages: messages,
        max_tokens: 2000,
        temperature: 0.7,
      });
    } catch (error) {
      functions.logger.error("OpenAI API error:", error);
      throw new functions.https.HttpsError(
        "internal",
        "Failed to get response from AI."
      );
    }

    const aiResponse = response.choices[0]?.message?.content || "No response";
    const tokensUsed = response.usage?.total_tokens || 0;

    const userHistory = rateLimitStore.get(uid);
    if (userHistory) {
      userHistory.tokens += tokensUsed;
      rateLimitStore.set(uid, userHistory);
    }

    const newMessages = [
      ...conversationData.messages,
      userMessage,
      { role: "assistant", content: aiResponse }
    ];

    await conversationRef.set({
      messages: newMessages,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      lastInteraction: admin.firestore.FieldValue.serverTimestamp(),
    }, { merge: true });

    await admin.firestore()
      .collection("companies")
      .doc(companyId)
      .collection("usage")
      .doc(uid)
      .set({
        tokensUsed: admin.firestore.FieldValue.increment(tokensUsed),
        requestsCount: admin.firestore.FieldValue.increment(1),
        lastRequest: admin.firestore.FieldValue.serverTimestamp(),
      }, { merge: true });

    return {
      response: aiResponse,
      conversationId: conversationId,
      tokensUsed: tokensUsed,
      model: model,
    };
  });

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

    await channelRef.update(unreadUpdates);

    await channelRef.update({
      lastMessage: messageText,
      lastMessageAt: admin.firestore.FieldValue.serverTimestamp()
    });

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
