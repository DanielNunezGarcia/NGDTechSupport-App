package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.BusinessModel
import com.example.ngdtechsupport.model.UpdateModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

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
        Log.d("CompanyRepository", "getBusiness called with companyId=$companyId, businessId=$businessId")
        return try {
            val doc = db.collection("companies")
                .document(companyId)
                .collection("businesses")
                .document(businessId)
                .get()
                .await()
            Log.d("CompanyRepository", "Document exists: ${doc.exists()}")
            if (!doc.exists()) return null
            val business = BusinessModel(
                id = doc.id,
                name = doc.getString("name") ?: "",
                status = doc.getString("status") ?: "",
                progress = doc.getLong("progress")?.toInt() ?: 0,
                version = doc.getString("version") ?: "",
                supportType = doc.getString("supportType") ?: "",
                lastUpdate = doc.getString("lastUpdate") ?: ""
            )
            Log.d("CompanyRepository", "Business loaded: $business")
            business
        } catch (e: Exception) {
            Log.e("CompanyRepository", "Error getting business: ${e.message}", e)
            null
        }
    }

    // Recibir Updates / Noticias
    suspend fun getUpdates(
        companyId: String,
        businessId: String
    ): List<UpdateModel> {

        return try {

            val snapshot = db.collection("companies")
                .document(companyId)
                .collection("businesses")
                .document(businessId)
                .collection("updates")
                .get()
                .await()

            snapshot.documents.map {

                UpdateModel(
                    id = it.id,
                    title = it.getString("title") ?: "",
                    description = it.getString("description") ?: "",
                    version = it.getString("version") ?: "",
                    type = it.getString("type") ?: "",
                    createdAt = it.getLong("createdAt") ?: 0,
                    createdBy = it.getString("createdBy") ?: "",
                    status = it.getString("status") ?: ""
                )
            }

        } catch (e: Exception) {
            emptyList()
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