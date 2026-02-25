package com.example.ngdtechsupport.data.model

import com.google.firebase.Timestamp

data class ChatMessageModel(
    val id: String = "",
    val message: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val timestamp: Timestamp? = null,
    val status: String = "sent",
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToUserName: String? = null
)