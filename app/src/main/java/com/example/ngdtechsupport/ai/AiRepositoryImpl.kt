package com.example.ngdtechsupport.ai

import kotlinx.coroutines.delay

class AiRepositoryImpl : AiRepository {

    private val conversations = mutableMapOf<String, MutableList<AiMessage>>()

    override suspend fun createConversation(companyId: String, userId: String): String {
        val conversationId = "${companyId}_${userId}_${System.currentTimeMillis()}"
        conversations[conversationId] = mutableListOf()
        return conversationId
    }

    override suspend fun sendMessage(companyId: String, conversationId: String, message: AiMessage): String {
        conversations.getOrPut(conversationId) { mutableListOf() }.add(message)
        
        delay(500)
        
        val responseText = AiResponseHelper.getResponse(message.content)
        val assistantMessage = AiMessage(
            content = responseText,
            role = AiRole.ASSISTANT
        )
        conversations[conversationId]?.add(assistantMessage)
        
        return responseText
    }

    override suspend fun getConversationMessages(companyId: String, conversationId: String): List<AiMessage> {
        return conversations[conversationId]?.toList() ?: emptyList()
    }

    suspend fun transferToHuman(companyId: String, userId: String): String {
        return createConversation(companyId, userId)
    }
}
