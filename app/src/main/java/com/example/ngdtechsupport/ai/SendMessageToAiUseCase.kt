package com.example.ngdtechsupport.ai

class SendMessageToAiUseCase(
    private val repository: AiRepository
) {
    suspend operator fun invoke(
        companyId: String,
        conversationId: String,
        userMessage: String
    ): String {
        val userAiMessage = AiMessage(
            content = userMessage,
            role = AiRole.USER
        )
        val messageId = repository.sendMessage(companyId, conversationId, userAiMessage)
        
        return messageId
    }
}