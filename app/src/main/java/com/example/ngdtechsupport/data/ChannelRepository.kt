package com.example.ngdtechsupport.data.repository

import android.util.Log
import com.example.ngdtechsupport.data.model.ChannelModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class ChannelRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun listenChannels(
        companyId: String,
        onResult: (List<ChannelModel>) -> Unit
    ) {
        firestore.collection("companies")
            .document(companyId)
            .collection("channels")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChannelRepository", "Error listening channels: ${error.message}")
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull {
                        it.toObject(ChannelModel::class.java)
                    }
                    onResult(list)
                }
            }
    }

    suspend fun createPrivateChannel(
        companyId: String,
        channelId: String,
        adminUid: String,
        memberUid: String
    ): Boolean {
        return try {
            val channelData = hashMapOf(
                "name" to "Canal Privado",
                "createdAt" to Timestamp.now(),
                "isArchived" to false,
                "pinned" to false,
                "members" to mapOf(
                    adminUid to mapOf("role" to "admin"),
                    memberUid to mapOf("role" to "member")
                ),
                "mutedUsers" to emptyMap<String, Boolean>(),
                "unreadCount" to mapOf(
                    adminUid to 0L,
                    memberUid to 0L
                )
            )

            firestore.collection("companies")
                .document(companyId)
                .collection("channels")
                .document(channelId)
                .set(channelData)
                .await()
            
            Log.d("ChannelRepository", "Channel created successfully: $channelId")
            true
        } catch (e: Exception) {
            Log.e("ChannelRepository", "Error creating channel: ${e.message}", e)
            false
        }
    }

    suspend fun archiveChannel(
        companyId: String,
        channelId: String,
        archived: Boolean
    ) {
        try {
            firestore.collection("companies")
                .document(companyId)
                .collection("channels")
                .document(channelId)
                .update("isArchived", archived)
                .await()
        } catch (e: Exception) {
            Log.e("ChannelRepository", "Error archiving channel: ${e.message}")
        }
    }

    suspend fun setChannelMuted(
        companyId: String,
        channelId: String,
        userId: String,
        muted: Boolean
    ) {
        try {
            firestore.collection("companies")
                .document(companyId)
                .collection("channels")
                .document(channelId)
                .update("mutedUsers.$userId", muted)
                .await()
        } catch (e: Exception) {
            Log.e("ChannelRepository", "Error setting muted: ${e.message}")
        }
    }

    suspend fun setChannelPinned(
        companyId: String,
        channelId: String,
        pinned: Boolean
    ) {
        try {
            firestore.collection("companies")
                .document(companyId)
                .collection("channels")
                .document(channelId)
                .update("pinned", pinned)
                .await()
        } catch (e: Exception) {
            Log.e("ChannelRepository", "Error setting pinned: ${e.message}")
        }
    }
}
