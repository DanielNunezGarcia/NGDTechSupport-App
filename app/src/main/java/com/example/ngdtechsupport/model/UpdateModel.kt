package com.example.ngdtechsupport.model

import com.google.firebase.Timestamp

data class UpdateModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0,
    val date: String = "",
    val type: String = "",
    val createdAt: Long = 0,
    val createdBy: String = ""
)