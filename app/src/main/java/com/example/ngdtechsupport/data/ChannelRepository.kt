package com.example.ngdtechsupport.data.repository

import com.example.ngdtechsupport.data.model.ChannelModel
import com.google.firebase.firestore.FirebaseFirestore
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
            .addSnapshotListener { snapshot, _ ->

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
    }
}
