const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.incrementUnreadOnMessageCreate =
functions.firestore
.document("companies/{companyId}/businesses/{businessId}/chat/{messageId}")
.onCreate(async (snap, context) => {

    const { companyId, businessId } = context.params;

    const messageData = snap.data();
    const senderId = messageData.senderId;

    const channelRef = admin.firestore()
        .collection("companies")
        .doc(companyId)
        .collection("businesses")
        .doc(businessId);

    const channelDoc = await channelRef.get();

    const members = channelDoc.data().members || {};

    const updates = {};

    for (const uid in members) {
        if (uid !== senderId) {
            updates[`unreadCount.${uid}`] =
                admin.firestore.FieldValue.increment(1);
        }
    }

    return channelRef.update(updates);
});