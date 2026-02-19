package com.example.ngdtechsupport.data.repository

import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getMessages(
        companyId: String,
        businessId: String
    ): List<ChatMessageModel> {

        val snapshot = firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("chat")
            .orderBy("createdAt")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            ChatMessageModel(
                id = doc.id,
                message = doc.getString("message") ?: "",
                senderId = doc.getString("senderId") ?: "",
                senderRole = doc.getString("senderRole") ?: "",
                createdAt = doc.getTimestamp("createdAt")
            )
        }
    }

    suspend fun sendMessage(
        companyId: String,
        businessId: String,
        message: String,
        senderId: String,
        senderRole: String
    ) {
        val messageData = hashMapOf(
            "message" to message,
            "senderId" to senderId,
            "senderRole" to senderRole,
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
}