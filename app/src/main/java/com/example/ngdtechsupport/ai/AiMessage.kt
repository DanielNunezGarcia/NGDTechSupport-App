package com.example.ngdtechsupport.ai

data class AiMessage(
    val id: String = "",
    val content: String = "",
    val role: AiRole = AiRole.USER,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AiRole {
    USER,
    ASSISTANT
}