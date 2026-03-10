package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.UpdateModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class UpdatesRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

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
                createdAt = doc.getTimestamp("createdAt") ?.toDate()?.time ?: 0L,
                createdBy = doc.getString("createdBy") ?: ""
            )
        }
    }

    suspend fun publishUpdate(
        companyId: String,
        businessId: String,
        update: UpdateModel
    ) {

        val businessRef = firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)

        // 1️⃣ Crear update
        businessRef
            .collection("updates")
            .add(update)
            .await()

        // 2️⃣ Actualizar lastUpdate automáticamente
        businessRef.update(
            "lastUpdate",
            update.createdAt
        ).await()
    }

    // Admin puede crear novedades
    suspend fun createUpdate(
        companyId: String,
        businessId: String,
        title: String,
        description: String,
        type: String,
        adminId: String,
        version: String
    ) {

        val ref = firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("updates")
            .document()

        val update = hashMapOf(
            "id" to ref.id,
            "title" to title,
            "description" to description,
            "type" to type,
            "version" to version,
            "createdAt" to Timestamp.now(),
            "createdBy" to adminId
        )

        ref.set(update).await()
    }

    // Updates se actualizan automáticamente
    fun listenUpdates(
        companyId: String,
        businessId: String,
        onResult: (List<UpdateModel>) -> Unit
    ) {

        firestore.collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("updates")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->

                val updates = snapshot?.documents?.map {

                    UpdateModel(
                        id = it.id,
                        title = it.getString("title") ?: "",
                        description = it.getString("description") ?: "",
                        type = it.getString("type") ?: "",
                        createdAt = it.getTimestamp("createdAt")
                            ?.toDate()?.time ?: 0L,
                        createdBy = it.getString("createdBy") ?: ""
                    )

                } ?: emptyList()

                onResult(updates)
            }
    }

    // Eliminar el Update
    suspend fun deleteUpdate(
        companyId: String,
        businessId: String,
        updateId: String
    ) {

        firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("updates")
            .document(updateId)
            .delete()
            .await()
    }

    // Editar los Updates
    suspend fun editUpdate(
        companyId: String,
        businessId: String,
        updateId: String,
        title: String,
        description: String
    ) {

        firestore
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("updates")
            .document(updateId)
            .update(
                mapOf(
                    "title" to title,
                    "description" to description
                )
            )
            .await()
    }
}