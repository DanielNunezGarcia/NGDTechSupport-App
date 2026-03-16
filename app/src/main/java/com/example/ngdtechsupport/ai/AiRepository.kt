package com.example.ngdtechsupport.ai

interface AiRepository {
    suspend fun createConversation(companyId: String, userId: String): String
    suspend fun sendMessage(companyId: String, conversationId: String, message: AiMessage): String
    suspend fun getConversationMessages(companyId: String, conversationId: String): List<AiMessage>
}