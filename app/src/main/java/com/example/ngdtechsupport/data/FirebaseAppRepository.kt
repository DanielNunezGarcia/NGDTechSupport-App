package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.AppModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Implementación concreta del repositorio usando Firebase Firestore
class FirebaseAppRepository : AppRepository {

    private val db = FirebaseFirestore.getInstance()

    override suspend fun getAppsForUser(userId: String): List<AppModel> {
        // Ya no usamos apps
        return emptyList()
    }

    override suspend fun getAllApps(): List<AppModel> {
        // Ya no usamos apps
        return emptyList()
    }

    override suspend fun getBusinessesForCompany(companyId: String): List<AppModel> {
        return try {
            val snapshot = db.collection("companies")
                .document(companyId)
                .collection("businesses")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                AppModel(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    status = doc.getString("status") ?: "",
                    lastUpdate = doc.getString("lastUpdate") ?: ""
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getSingleBusiness(companyId: String, businessId: String): List<AppModel> {
        return try {
            val doc = db.collection("companies")
                .document(companyId)
                .collection("businesses")
                .document(businessId)
                .get()
                .await()

            if (doc.exists()) {
                listOf(
                    AppModel(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        status = doc.getString("status") ?: "",
                        lastUpdate = doc.getString("lastUpdate") ?: ""
                    )
                )
            } else emptyList()

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}