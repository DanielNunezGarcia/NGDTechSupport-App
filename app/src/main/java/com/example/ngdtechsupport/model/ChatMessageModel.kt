package com.example.ngdtechsupport.data.model

import com.google.firebase.Timestamp

data class ChatMessageModel(
    val id: String = "",
    val message: String = "",
    val senderId: String = "",
    val senderRole: String = "",
    val senderName: String = "",
    val isDelivered: Boolean = false,
    val isRead: Boolean = false,
    val createdAt: Timestamp? = null
)