package com.example.ngdtechsupport.data.repository

import android.util.Log
import com.example.ngdtechsupport.ai.AiMessage
import com.example.ngdtechsupport.ai.AiResponseHelper
import kotlinx.coroutines.delay

class AiRepositoryImpl : com.example.ngdtechsupport.ai.AiRepository {

    private val conversations = mutableMapOf<String, MutableList<AiMessage>>()

    override suspend fun createConversation(companyId: String, userId: String): String {
        val conversationId = "${companyId}_${userId}_${System.currentTimeMillis()}"
        conversations[conversationId] = mutableListOf()
        Log.d("AiRepository", "Created conversation: $conversationId")
        return conversationId
    }

    override suspend fun sendMessage(
        companyId: String,
        conversationId: String,
        message: AiMessage
    ): String {
        return try {
            conversations.getOrPut(conversationId) { mutableListOf() }.add(message)
            
            delay(500)
            
            val responseText = AiResponseHelper.getResponse(message.content)
            val assistantMessage = AiMessage(
                content = responseText,
                role = com.example.ngdtechsupport.ai.AiRole.ASSISTANT
            )
            conversations[conversationId]?.add(assistantMessage)
            
            responseText
        } catch (e: Exception) {
            Log.e("AiRepository", "Error sending message", e)
            "Disculpa, tuve un problema al procesar tu mensaje."
        }
    }

    override suspend fun getConversationMessages(
        companyId: String,
        conversationId: String
    ): List<AiMessage> {
        return conversations[conversationId]?.toList() ?: emptyList()
    }

    suspend fun transferToHuman(companyId: String, userId: String): String? {
        return try {
            val conversationId = createConversation(companyId, userId)
            conversationId
        } catch (e: Exception) {
            Log.e("AiRepository", "Error transferring to human", e)
            null
        }
    }
}
