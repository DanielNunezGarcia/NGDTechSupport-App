package com.example.ngdtechsupport.ai

data class AiConversation(
    val id: String = "",
    val companyId: String = "",
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)