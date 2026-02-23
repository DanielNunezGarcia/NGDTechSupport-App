package com.example.ngdtechsupport.data.repository

import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private var lastVisible: com.google.firebase.firestore.DocumentSnapshot? = null

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
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(30)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.isEmpty) {

                    lastVisible = snapshot.documents.last()

                    val messages = snapshot.toObjects(ChatMessageModel::class.java)
                        .reversed()

                    onResult(messages)
                }
            }
    }

    fun sendMessage(
        companyId: String,
        businessId: String,
        text: String,
        senderId: String,
        senderName: String
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
            "senderName" to senderName,
            "timestamp" to Timestamp.now(),
            "status" to "sent"
        )

        messageRef.set(message)
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

                snapshot.documents.forEach { doc ->
                    doc.reference.update("status", "read")
                }
            }
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

                    if (isTyping) onResult(userName)
                    else onResult(null)
                }
            }
    }
}