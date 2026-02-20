package com.example.ngdtechsupport.data.repository

import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun listenForMessages(
        companyId: String,
        businessId: String,
        onResult: (List<ChatMessageModel>) -> Unit
    ) {

        firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("chat")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null) {

                    val messages = snapshot.documents.map { doc ->
                        ChatMessageModel(
                            id = doc.id,
                            message = doc.getString("message") ?: "",
                            senderId = doc.getString("senderId") ?: "",
                            senderRole = doc.getString("senderRole") ?: "",
                            senderName = doc.getString("senderName") ?: "",
                            isDelivered = doc.getBoolean("isDelivered") ?: false,
                            isRead = doc.getBoolean("isRead") ?: false,
                            createdAt = doc.getTimestamp("createdAt")
                        )
                    }

                    onResult(messages)
                }
            }
    }

    fun markMessagesAsRead(
        companyId: String,
        businessId: String,
        currentUserId: String
    ) {

        firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("chat")
            .whereNotEqualTo("senderId", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->

                for (doc in snapshot.documents) {
                    doc.reference.update(
                        mapOf(
                            "isDelivered" to true,
                            "isRead" to true
                        )
                    )
                }
            }
    }

    fun sendMessage(
        companyId: String,
        businessId: String,
        message: String,
        senderId: String,
        senderRole: String,
        senderName: String
    ) {
        val messageData = hashMapOf(
            "message" to message,
            "senderId" to senderId,
            "senderRole" to senderRole,
            "senderName" to senderName,
            "isDelivered" to false,
            "isRead" to false,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("chat")
            .add(messageData)
            .await()
    }

    fun setTypingStatus(
        companyId: String,
        businessId: String,
        userId: String,
        userName: String,
        isTyping: Boolean
    ) {

        val data = mapOf(
            "userId" to userId,
            "userName" to userName,
            "isTyping" to isTyping
        )

        firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("chatStatus")
            .document("typing")
            .set(data)
    }

    fun listenTypingStatus(
        companyId: String,
        businessId: String,
        onResult: (String?) -> Unit
    ) {

        firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("chatStatus")
            .document("typing")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null && snapshot.exists()) {

                    val isTyping = snapshot.getBoolean("isTyping") ?: false
                    val userName = snapshot.getString("userName") ?: ""

                    if (isTyping) {
                        onResult(userName)
                    } else {
                        onResult(null)
                    }
                }
            }
    }
}