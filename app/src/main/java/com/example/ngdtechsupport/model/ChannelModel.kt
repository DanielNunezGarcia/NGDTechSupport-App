package com.example.ngdtechsupport.data.model

import com.example.ngdtechsupport.model.ChannelMember
import com.google.firebase.Timestamp

data class ChannelModel(
    val id: String = "",
    val name: String = "",
    val createdAt: Timestamp? = null,
    val unreadCount: Map<String, Long> = emptyMap(),
    val isArchived: Boolean = false,
    val members: Map<String, ChannelMember> = emptyMap(),
    val mutedUsers: Map<String, Boolean> = emptyMap()
)