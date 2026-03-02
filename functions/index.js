const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.onMessageCreate =
functions.firestore
.document("companies/{companyId}/businesses/{businessId}/chat/{messageId}")
.onCreate(async (snap, context) => {

    const { companyId, businessId } = context.params;

    const messageData = snap.data();
    const senderId = messageData.senderId;
    const messageText = messageData.message;

    const channelRef = admin.firestore()
        .collection("companies")
        .doc(companyId)
        .collection("businesses")
        .doc(businessId);

    const channelDoc = await channelRef.get();
    const channelData = channelDoc.data();

    const members = channelData.members || {};
    const mutedUsers = channelData.mutedUsers || {};

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

    // Enviar notificaciones si hay tokens
    if (tokens.length > 0) {

        const payload = {
            notification: {
                title: "Nuevo mensaje",
                body: messageText
            },
            data: {
                companyId: companyId,
                businessId: businessId
            }
        };

        await admin.messaging().sendEachForMulticast({
            tokens: tokens,
            ...payload
        });
    }

    return null;
});