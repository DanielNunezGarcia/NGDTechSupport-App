package com.example.ngdtechsupport.model

import com.google.firebase.Timestamp

data class UpdateModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val type: String = "",
    val createdAt: Timestamp? = null,
    val createdBy: String = ""
)