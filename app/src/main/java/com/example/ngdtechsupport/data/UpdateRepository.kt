package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.UpdateModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class UpdatesRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUpdates(
        companyId: String,
        businessId: String
    ): List<UpdateModel> {

        val snapshot = firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("updates")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            UpdateModel(
                id = doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                type = doc.getString("type") ?: "",
                createdAt = doc.getTimestamp("createdAt"),
                createdBy = doc.getString("createdBy") ?: ""
            )
        }
    }

    suspend fun createUpdate(
        companyId: String,
        businessId: String,
        update: UpdateModel
    ) {
        firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("updates")
            .add(update)
            .await()
    }
}