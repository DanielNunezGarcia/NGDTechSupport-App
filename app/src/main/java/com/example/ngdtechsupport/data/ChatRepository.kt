package com.example.ngdtechsupport.data.repository

import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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
}