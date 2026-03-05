package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.BusinessModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CompanyRepository {

    private val db = FirebaseFirestore.getInstance()

    // Carga todos los negocios en el Dashboard
    suspend fun getBusinesses(companyId: String): List<BusinessModel> {
        return try {
            val snapshot = db.collection("companies")
                .document(companyId)
                .collection("businesses")
                .get()
                .await()

            snapshot.documents.map {
                BusinessModel(
                    id = it.id,
                    name = it.getString("name") ?: "",
                    status = it.getString("status") ?: "",
                    progress = it.getLong("progress")?.toInt() ?: 0,
                    version = it.getString("version") ?: "",
                    lastUpdate = it.getString("lastUpdate") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Carga un negocio en concreto
    suspend fun getBusiness(
        companyId: String,
        businessId: String
    ): BusinessModel? {

        return try {

            val document = db.collection("companies")
                .document(companyId)
                .collection("businesses")
                .document(businessId)
                .get()
                .await()

            BusinessModel(
                id = document.id,
                name = document.getString("name") ?: "",
                status = document.getString("status") ?: "",
                progress = document.getLong("progress")?.toInt() ?: 0,
                version = document.getString("version") ?: "",
                lastUpdate = document.getString("lastUpdate") ?: ""
            )

        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateBusinessLastUpdate(
        companyId: String,
        businessId: String,
        newDate: String
    ) {
        FirebaseFirestore.getInstance()
            .collection("companies")
            .document(companyId)
            .collection("businesses")
            .document(businessId)
            .update("lastUpdate", newDate)
    }
}