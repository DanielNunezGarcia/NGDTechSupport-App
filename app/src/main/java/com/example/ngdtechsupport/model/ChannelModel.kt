package com.example.ngdtechsupport.data.model

import com.google.firebase.Timestamp

data class ChannelModel(
    val id: String = "",
    val name: String = "",
    val createdAt: Timestamp? = null,
    val members: List<String> = emptyList(),
    val unreadCount: Map<String, Long> = emptyMap()
)