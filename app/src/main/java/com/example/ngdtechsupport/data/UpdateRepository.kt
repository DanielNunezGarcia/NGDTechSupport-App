package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.UpdateModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UpdateRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getUpdatesForApp(appId: String): List<UpdateModel> {
        return try {
            val snapshot = db.collection("updates")
                .whereEqualTo("appId", appId)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(UpdateModel::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}