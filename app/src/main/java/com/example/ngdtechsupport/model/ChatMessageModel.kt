package com.example.ngdtechsupport.data.model

import com.google.firebase.Timestamp

data class ChatMessageModel(
    val id: String = "",
    val message: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderType: String = SENDER_TYPE_USER,  // user, ai, agent
    val timestamp: Timestamp? = null,
    val status: String = "sent",
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToUserName: String? = null,
    val requiresHumanSupport: Boolean = false  // Flag para escalar a humano
) {
    companion object {
        const val SENDER_TYPE_USER = "user"
        const val SENDER_TYPE_AI = "ai"
        const val SENDER_TYPE_AGENT = "agent"
    }
}