package com.example.ngdtechsupport.data.repository

import android.util.Log
import com.example.ngdtechsupport.ai.AiMessage
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class AiRepositoryImpl : com.example.ngdtechsupport.ai.AiRepository {

    private val functions = FirebaseFunctions.getInstance()

    override suspend fun createConversation(companyId: String, userId: String): String {
        return try {
            val result = functions.getHttpsCallable("chatWithAI")
                .call(hashMapOf(
                    "companyId" to companyId,
                    "message" to "",
                    "conversationId" to null
                )).await()

            (result.data as? HashMap<*, *>)?.get("conversationId") as? String ?: ""
        } catch (e: Exception) {
            Log.e("AiRepository", "Error creating conversation", e)
            ""
        }
    }

    override suspend fun sendMessage(
        companyId: String,
        conversationId: String,
        message: AiMessage
    ): String {
        return try {
            val result = functions.getHttpsCallable("chatWithAI")
                .call(hashMapOf(
                    "companyId" to companyId,
                    "message" to message.content,
                    "conversationId" to conversationId
                )).await()

            val data = result.data as? HashMap<*, *>
            data?.get("response") as? String ?: "Error al obtener respuesta"
        } catch (e: Exception) {
            Log.e("AiRepository", "Error sending message", e)
            "Disculpa, tuve un problema al procesar tu mensaje."
        }
    }

    override suspend fun getConversationMessages(
        companyId: String,
        conversationId: String
    ): List<AiMessage> {
        // Por ahora retornamos vacío, se puede implementar lectura desde Firestore
        return emptyList()
    }

    suspend fun transferToHuman(companyId: String, userId: String): String? {
        return try {
            val result = functions.getHttpsCallable("transferToHuman")
                .call(hashMapOf(
                    "companyId" to companyId,
                    "userId" to userId
                )).await()

            val data = result.data as? HashMap<*, *>
            data?.get("channelId") as? String
        } catch (e: Exception) {
            Log.e("AiRepository", "Error transferring to human", e)
            null
        }
    }
}
