package com.example.ngdtechsupport.data.repository

import com.example.ngdtechsupport.data.model.ChannelModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChannelRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getChannels(companyId: String): List<ChannelModel> {
        return try {
            val snapshot = firestore
                .collection("companies")
                .document(companyId)
                .collection("channels")
                .get()
                .await()

            snapshot.toObjects(ChannelModel::class.java)

        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createPrivateChannel(
        companyId: String,
        channelId: String,
        adminUid: String,
        memberUid: String
    ) {
        val channelData = hashMapOf(
            "id" to channelId,
            "name" to "Private Chat",
            "createdAt" to System.currentTimeMillis(),
            "members" to mapOf(
                adminUid to mapOf("role" to "admin"),
                memberUid to mapOf("role" to "member")
            ),
            "unreadCount" to mapOf(
                adminUid to 0,
                memberUid to 0
            )
        )

        firestore.collection("companies")
            .document(companyId)
            .collection("channels")
            .document(channelId)
            .set(channelData)
            .await()
    }

    suspend fun archiveChannel(
        companyId: String,
        channelId: String,
        archived: Boolean
    ) {
        firestore.collection("companies")
            .document(companyId)
            .collection("channels")
            .document(channelId)
            .update("isArchived", archived)
            .await()
    }
}