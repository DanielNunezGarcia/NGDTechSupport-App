package com.example.ngdtechsupport.data.repository

import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private var lastVisible: DocumentSnapshot? = null

    fun loadInitialMessages(
        companyId: String,
        businessId: String,
        onResult: (List<ChatMessageModel>) -> Unit
    ) {

        firestore.collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("chat")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.isEmpty) {

                    lastVisible = snapshot.documents.last()

                    val messages = snapshot
                        .toObjects(ChatMessageModel::class.java)
                        .reversed()

                    onResult(messages)
                }
            }
    }

    fun loadMoreMessages(
        companyId: String,
        businessId: String,
        onResult: (List<ChatMessageModel>) -> Unit
    ) {

        val lastDoc = lastVisible ?: return

        firestore.collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("chat")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .startAfter(lastDoc)
            .limit(30)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.isEmpty) {

                    lastVisible = snapshot.documents.last()

                    val messages = snapshot
                        .toObjects(ChatMessageModel::class.java)
                        .reversed()

                    onResult(messages)
                }
            }
    }

    suspend fun sendMessage(
        companyId: String,
        businessId: String,
        text: String,
        senderId: String,
        replyToId: String?,
        replyToText: String?
    ) {

        val messageRef = firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("chat")
            .document()

        val message = hashMapOf(
            "id" to messageRef.id,
            "message" to text,
            "senderId" to senderId,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "status" to "sent",
            "replyToId" to replyToId,
            "replyToText" to replyToText
        )

        messageRef.set(message).await()
        incrementUnread(companyId, businessId, senderId)
    }

    fun listenChannels(
        companyId: String,
        businessId: String,
        onResult: (List<ChannelModel>) -> Unit
    ) {

        firestore.collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("channels")
            .orderBy("pinned", Query.Direction.DESCENDING)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null) {
                    val channels = snapshot.toObjects(ChannelModel::class.java)
                    onResult(channels)
                }
            }
    }

    fun markChannelAsRead(
        companyId: String,
        businessId: String,
        channelId: String,
        userId: String
    ) {
        firestore.collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("channels")
            .document(channelId)
            .update("unreadCount.$userId", 0)
    }

    fun setChannelPinned(
        companyId: String,
        businessId: String,
        channelId: String,
        pinned: Boolean
    ) {
        firestore.collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("channels")
            .document(channelId)
            .update("pinned", pinned)
    }

    private suspend fun incrementUnread(
        companyId: String,
        businessId: String,
        senderId: String
    ) {
        val channelRef = firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)

        val channelSnapshot = channelRef.get().await()

        val members = channelSnapshot.get("members") as? Map<String, *>
            ?: return

        val updates = mutableMapOf<String, Any>()

        for (uid in members.keys) {
            if (uid != senderId) {
                updates["unreadCount.$uid"] =
                    FieldValue.increment(1)
            }
        }

        if (updates.isNotEmpty()) {
            channelRef.update(updates).await()
        }
    }
}