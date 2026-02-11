package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.AppModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AppRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getAppsForUser(userId: String): List<AppModel> {
        return try {
            val snapshot = db.collection("apps")
                .whereEqualTo("clientId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(AppModel::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}