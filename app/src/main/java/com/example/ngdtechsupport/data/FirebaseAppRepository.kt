package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.AppModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Implementación concreta del repositorio usando Firebase Firestore
class FirebaseAppRepository : AppRepository {

    private val db = FirebaseFirestore.getInstance()

    override suspend fun getAppsForUser(userId: String): List<AppModel> {
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

    override suspend fun getAllApps(): List<AppModel> {
        return try {
            val snapshot = db.collection("apps")
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