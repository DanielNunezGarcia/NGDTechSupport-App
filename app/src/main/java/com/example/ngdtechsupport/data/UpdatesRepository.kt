package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.UpdateModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import android.util.Log

class UpdatesRepository {

    companion object {
        private const val TAG = "UpdatesRepository"
    }

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
            doc.toUpdateModel()
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
        adminId: String,
        priority: Int = 1,
        color: String = "#2196F3",
        publishDate: Long = 0
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
            "createdBy" to adminId,
            "priority" to priority,
            "color" to color,
            "publishDate" to publishDate
        )

        ref.set(update).await()
    }

    // Updates se actualizan automáticamente
    fun listenUpdates(
        companyId: String,
        businessId: String,
        onResult: (List<UpdateModel>) -> Unit,
        onError: (Throwable) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        Log.d(TAG, "listenUpdates: companyId=$companyId, businessId=$businessId")

        val updatesRef = firestore.collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .collection("updates")

        var fallbackRegistration: com.google.firebase.firestore.ListenerRegistration? = null
        var fallbackAttached = false

        val orderedRegistration = updatesRef
            .orderBy("pinned", Query.Direction.DESCENDING)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching updates (ordered query): ${error.message}", error)

                    val code = (error as? FirebaseFirestoreException)?.code
                    if (code == FirebaseFirestoreException.Code.FAILED_PRECONDITION ||
                        code == FirebaseFirestoreException.Code.INVALID_ARGUMENT
                    ) {
                        if (!fallbackAttached) {
                            Log.w(TAG, "Falling back to non-ordered updates query due to missing index/type issue")
                            fallbackAttached = true
                            fallbackRegistration = listenUpdatesFallback(updatesRef, onResult, onError)
                        }
                    } else {
                        onError(error)
                    }
                    return@addSnapshotListener
                }

                Log.d(TAG, "Ordered updates snapshot size: ${snapshot?.documents?.size ?: 0}")

                val updates = snapshot?.documents?.map { doc ->
                    doc.toUpdateModel().also {
                        Log.d(TAG, "Update parsed: id=${it.id}, title=${it.title}")
                    }
                } ?: emptyList()

                Log.d(TAG, "Parsed updates count: ${updates.size}")
                onResult(updates)
            }

        return com.google.firebase.firestore.ListenerRegistration {
            orderedRegistration.remove()
            fallbackRegistration?.remove()
        }
    }

    private fun listenUpdatesFallback(
        updatesRef: com.google.firebase.firestore.CollectionReference,
        onResult: (List<UpdateModel>) -> Unit,
        onError: (Throwable) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        return updatesRef
            .addSnapshotListener { snapshot, fallbackError ->
                if (fallbackError != null) {
                    Log.e(TAG, "Error fetching updates (fallback query): ${fallbackError.message}", fallbackError)
                    onError(fallbackError)
                    return@addSnapshotListener
                }

                val updates = snapshot?.documents
                    ?.map { it.toUpdateModel() }
                    ?.sortedWith(
                        compareByDescending<UpdateModel> { it.pinned }
                            .thenByDescending { it.createdAt }
                    )
                    .orEmpty()

                Log.d(TAG, "Fallback updates parsed count: ${updates.size}")
                onResult(updates)
            }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toUpdateModel(): UpdateModel {
        return UpdateModel(
            id = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            type = getString("type") ?: "",
            version = getString("version") ?: "",
            createdAt = getTimestamp("createdAt")
                ?.toDate()
                ?.time
                ?: getLong("createdAt")
                ?: 0L,
            createdBy = getString("createdBy") ?: "",
            status = getString("status") ?: "",
            isActive = getBoolean("isActive") ?: true,
            priority = getLong("priority")?.toInt() ?: 1,
            pinned = getBoolean("pinned") ?: false,
            color = getString("color") ?: "#2196F3",
            publishDate = getLong("publishDate") ?: 0L
        )
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
            .set(
                mapOf("lastUpdateRead" to System.currentTimeMillis()),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .await()
    }
}
