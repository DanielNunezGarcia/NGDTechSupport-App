package com.example.ngdtechsupport.data.repository

import android.util.Log
import com.example.ngdtechsupport.data.model.ChannelModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import com.example.ngdtechsupport.model.ChannelMember

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
        Log.d("ChannelRepository", "createPrivateChannel called with companyId=$companyId, channelId=$channelId, adminUid=$adminUid, memberUid=$memberUid")
        if (companyId.isEmpty() || channelId.isEmpty() || adminUid.isEmpty() || memberUid.isEmpty()) {
            Log.e("ChannelRepository", "Invalid parameters: companyId=$companyId, channelId=$channelId, adminUid=$adminUid, memberUid=$memberUid")
            return false
        }
        return try {
            val membersMap = mutableMapOf(
                adminUid to ChannelMember(role = "admin")
            )
            if (memberUid != adminUid) {
                membersMap[memberUid] = ChannelMember(role = "member")
            }

            val unreadCountMap = mutableMapOf(
                adminUid to 0L
            )
            if (memberUid != adminUid) {
                unreadCountMap[memberUid] = 0L
            }
            
            val channelData = ChannelModel(
                id = channelId,
                name = "Canal Privado",
                createdAt = Timestamp.now(),
                isArchived = false,
                pinned = false,
                members = membersMap,
                mutedUsers = emptyMap(),
                unreadCount = unreadCountMap
            )

            firestore.collection("companies")
                .document(companyId)
                .collection("channels")
                .document(channelId)
                .set(channelData, SetOptions.merge())
                .await()
            
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
