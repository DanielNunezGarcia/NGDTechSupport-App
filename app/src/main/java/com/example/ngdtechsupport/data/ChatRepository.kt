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
                            createdAt = doc.getTimestamp("createdAt")
                        )
                    }

                    onResult(messages)
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