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
}