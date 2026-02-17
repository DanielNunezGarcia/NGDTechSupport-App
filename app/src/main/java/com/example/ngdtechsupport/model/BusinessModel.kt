package com.example.ngdtechsupport.model

import com.google.firebase.Timestamp

data class BusinessModel(
    val id: String = "",
    val name: String = "",
    val status: String = "",
    val progress: Int = 0,
    val version: String = "",
    val supportType: String = "",
    val lastUpdate: String = "",
    val createdAt: Timestamp? = null,
    val isActive: Boolean = true
)