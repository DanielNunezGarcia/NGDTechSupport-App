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
                    name = it.getString("name") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}