package com.example.ngdtechsupport.data.repository

import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
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
            .limit(30)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.isEmpty) {

                    lastVisible = snapshot.documents.last()

                    val messages = snapshot
                        .toObjects(ChatMessageModel::class.java)

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
            .startAfter(lastDoc)
            .limit(30)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.isEmpty) {

                    lastVisible = snapshot.documents.last()

                    val messages = snapshot
                        .toObjects(ChatMessageModel::class.java)

                    onResult(messages)
                }
            }
    }

    suspend fun sendMessage(
        companyId: String,
        channelId: String,
        text: String,
        senderId: String,
        replyToId: String?,
        replyToText: String?
    ) {

        val messageRef = firestore
            .collection("companies")
            .document(companyId)
            .collection("channels")
            .document(channelId)
            .collection("messages")
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

        // 2️⃣ Incrementar unreadCount
        firestore.collection("companies")
            .document(companyId)
            .collection("channels")
            .document(channelId)
            .update(
                "unread_client",
                FieldValue.increment(1)
            )

        // 1️⃣ Guardar mensaje
        messageRef.set(message).await()

        val channelRef = firestore
            .collection("companies")
            .document(companyId)
            .collection("channels")
            .document(channelId)

        channelRef.update(
            mapOf(
                "lastMessage" to text,
                "lastMessageAt" to System.currentTimeMillis()
            )
        ).await()

        // Incrementar contador al enviar mensaje
        if (senderId.contains("admin")) {

            channelRef.update(
                "unread_client",
                FieldValue.increment(1)
            ).await()

        } else {

            channelRef.update(
                "unread_admin",
                FieldValue.increment(1)
            ).await()

        }
    }

    fun listenMessages(
        companyId: String,
        channelId: String,
        onMessagesChange: (List<ChatMessageModel>) -> Unit
    ) {

        firestore
            .collection("companies")
            .document(companyId)
            .collection("channels")
            .document(channelId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null) return@addSnapshotListener

                val messages = snapshot.documents.mapNotNull {
                    it.toObject(ChatMessageModel::class.java)
                }

                onMessagesChange(messages)
            }
    }

    /* private suspend fun incrementUnread(
        companyId: String,
        businessId: String,
        senderId: String
    ) {

        val chatStatusRef = firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("chatStatus")

        val snapshot = chatStatusRef.get().await()

        for (document in snapshot.documents) {

            val userId = document.id

            // No incrementar al que envía el mensaje
            if (userId != senderId) {

                chatStatusRef.document(userId).update(
                    "unreadCount",
                    com.google.firebase.firestore.FieldValue.increment(1)
                ).await()
            }
        }
    } */

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

    suspend fun updateLastRead(
        companyId: String,
        businessId: String,
        userId: String
    ) {

        firestore.collection("companies")
            .document(companyId)
            .collection("channels")
            .document("${businessId}_support")
            .update(
                "lastRead_$userId",
                System.currentTimeMillis()
            )
            .await()
    }

    // Resetea contador cuando se abre el chat
    suspend fun markChatAsRead(
        companyId: String,
        channelId: String,
        isAdmin: Boolean
    ) {

        val channelRef = firestore
            .collection("companies")
            .document(companyId)
            .collection("channels")
            .document(channelId)

        if (isAdmin) {

            channelRef.update("unread_admin", 0)

        } else {

            channelRef.update("unread_client", 0)
        }
    }

    suspend fun markChannelAsRead(
        companyId: String,
        businessId: String,
        userId: String
    ) {
        firestore.collection("companies")
            .document(companyId)
            .collection("channels")
            .document("${businessId}_support")
            .update("unreadCount.$userId", 0
        )
        .await()
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
}