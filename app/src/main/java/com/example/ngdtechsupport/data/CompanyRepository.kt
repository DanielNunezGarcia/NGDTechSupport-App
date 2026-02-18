package com.example.ngdtechsupport.data

import com.example.ngdtechsupport.model.BusinessModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CompanyRepository {

    private val db = FirebaseFirestore.getInstance()

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