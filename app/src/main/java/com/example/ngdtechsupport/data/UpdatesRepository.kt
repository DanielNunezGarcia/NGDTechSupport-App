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
            .orderBy("pinned", Query.Direction.DESCENDING)
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
        version: String,
        adminId: String
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
    ): com.google.firebase.firestore.ListenerRegistration {
        android.util.Log.d("UpdatesRepository", "listenUpdates: companyId=$companyId, businessId=$businessId")

        val updatesRef = firestore.collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("updates")

        return updatesRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("UpdatesRepository", "Error fetching updates: ${error.message}")
                    android.util.Log.e("UpdatesRepository", "Error code: ${error.code}")
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                android.util.Log.d("UpdatesRepository", "Snapshot size: ${snapshot?.documents?.size ?: 0}")

                val updates = snapshot?.documents?.map { doc ->
                    android.util.Log.d("UpdatesRepository", "Doc: ${doc.id} - ${doc.getString("title")}")
                    UpdateModel(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        type = doc.getString("type") ?: "",
                        version = doc.getString("version") ?: "",
                        createdAt = doc.getTimestamp("createdAt")
                            ?.toDate()?.time ?: doc.getLong("createdAt") ?: 0L,
                        createdBy = doc.getString("createdBy") ?: "",
                        pinned = doc.getBoolean("pinned") ?: false
                    )
                } ?: emptyList()

                android.util.Log.d("UpdatesRepository", "Parsed updates: ${updates.size}")
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

    suspend fun markUpdatesAsRead(userId: String) {

        firestore
            .collection("users")
            .document(userId)
            .update(
                "lastUpdateRead",
                System.currentTimeMillis()
            )
            .await()
    }
}