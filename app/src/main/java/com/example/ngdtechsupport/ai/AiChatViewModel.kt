package com.example.ngdtechsupport.ai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AiChatViewModel : ViewModel() {

    private val repository = AiRepositoryImpl()

    private val _messages = MutableLiveData<List<AiMessage>>(emptyList())
    val messages: LiveData<List<AiMessage>> = _messages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentConversationId: String? = null

    fun sendMessage(companyId: String, userMessage: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Agregar mensaje del usuario
                val currentList = _messages.value?.toMutableList() ?: mutableListOf()
                currentList.add(AiMessage(
                    content = userMessage,
                    role = AiRole.USER
                ))
                _messages.value = currentList

                // Enviar a la IA
                val response = repository.sendMessage(
                    companyId = companyId,
                    conversationId = currentConversationId ?: "",
                    message = AiMessage(content = userMessage, role = AiRole.USER)
                )

                // Obtener conversationId si es nuevo
                if (currentConversationId == null) {
                    currentConversationId = companyId
                }

                // Agregar respuesta de IA
                val updatedList = _messages.value?.toMutableList() ?: mutableListOf()
                updatedList.add(AiMessage(
                    content = response,
                    role = AiRole.ASSISTANT
                ))
                _messages.value = updatedList

            } catch (e: Exception) {
                _error.value = e.message ?: "Error al comunicarse con la IA"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun transferToHuman(companyId: String, userId: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val channelId = repository.transferToHuman(companyId, userId)
                channelId?.let { onSuccess(it) }
            } catch (e: Exception) {
                _error.value = "Error al transferir a soporte humano"
            }
        }
    }
}
