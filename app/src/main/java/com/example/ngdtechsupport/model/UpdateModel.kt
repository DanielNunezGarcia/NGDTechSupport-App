package com.example.ngdtechsupport.model

import com.google.firebase.Timestamp

data class UpdateModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val version: String = "",
    val type: String = "",
    val createdAt: Long = 0,
    val createdBy: String = "",
    val status: String = "",
    val isActive: Boolean = true,
    val priority: Int = 1,
    val pinned: Boolean = false

)