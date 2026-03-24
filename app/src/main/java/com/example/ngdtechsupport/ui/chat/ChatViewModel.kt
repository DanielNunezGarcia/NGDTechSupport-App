package com.example.ngdtechsupport.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.ngdtechsupport.ai.AiResponseHelper
import com.example.ngdtechsupport.utils.SecurityValidator
import com.google.firebase.Timestamp
import java.security.SecureRandom

class ChatViewModel : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val chatRepository = ChatRepository()
    private val channelRepository = com.example.ngdtechsupport.data.repository.ChannelRepository()

    private val _messages = MutableLiveData<List<ChatMessageModel>>()
    val messages: LiveData<List<ChatMessageModel>> = _messages

    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private val currentUserName: String
        get() = FirebaseAuth.getInstance().currentUser?.displayName ?: "Usuario"

    private val currentList = mutableListOf<ChatMessageModel>()
    private val random = SecureRandom()

    private val _channels = MutableLiveData<List<ChannelModel>>()
    val channels: LiveData<List<ChannelModel>> = _channels

    private val _typingUsers = MutableLiveData<Map<String, Any>>()
    val typingUsers: LiveData<Map<String, Any>> = _typingUsers

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var typingListenerCleanup: (() -> Unit)? = null

    fun listenMessages(companyId: String, channelId: String) {
        chatRepository.listenMessages(companyId, channelId) { messages ->
            _messages.postValue(messages)
        }
    }

    fun listenTyping(companyId: String, channelId: String) {
        val uid = currentUserId ?: return
        
        chatRepository.listenTyping(companyId, channelId, uid) { typing ->
            _typingUsers.postValue(typing)
        }
    }

    fun setTyping(companyId: String, channelId: String, isTyping: Boolean) {
        val uid = currentUserId ?: return
        
        chatRepository.setTyping(
            companyId = companyId,
            channelId = channelId,
            userId = uid,
            userName = currentUserName,
            isTyping = isTyping
        )
    }

    fun sendMessage(
        companyId: String,
        channelId: String,
        text: String,
        senderId: String,
        replyToId: String?,
        replyToText: String?
    ) {
        if (companyId.isBlank() || channelId.isBlank() || senderId.isBlank()) {
            _error.postValue("No se pudo enviar el mensaje por datos incompletos")
            return
        }

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(
                    companyId,
                    channelId,
                    text,
                    senderId,
                    replyToId,
                    replyToText
                )

                chatRepository.setTyping(companyId, channelId, senderId, "", false)

                processMessageWithAi(companyId, channelId, text)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "No se pudo enviar el mensaje")
            }
        }
    }

    fun processMessageWithAi(companyId: String, channelId: String, userMessage: String) {
        viewModelScope.launch {
            try {
                val aiResponse = AiResponseHelper.getResponse(userMessage)

                val aiMessage = ChatMessageModel(
                    id = "",
                    message = aiResponse,
                    senderId = "ai_assistant",
                    senderName = "Asistente IA",
                    senderType = ChatMessageModel.SENDER_TYPE_AI,
                    timestamp = Timestamp.now(),
                    status = "sent"
                )

                val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
                currentMessages.add(aiMessage)
                _messages.postValue(currentMessages)

                chatRepository.sendAiMessage(companyId, channelId, aiMessage)
            } catch (_: Exception) {
                _error.postValue("No se pudo procesar la respuesta automatica")
            }
        }
    }

    fun sendWelcomeMessageOnOpen(companyId: String, channelId: String) {
        viewModelScope.launch {
            try {
                val configSnapshot = FirebaseFirestore.getInstance()
                    .collection("companies")
                    .document(companyId)
                    .collection("ai_config")
                    .document("settings")
                    .get()
                    .await()

                val aiEnabled = configSnapshot.getBoolean("aiEnabled") ?: true
                if (!aiEnabled) return@launch

                val welcomeMessages = (configSnapshot.get("greetingMessages") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.map { SecurityValidator.sanitizeInput(it).trim() }
                    ?.filter { it.isNotBlank() }
                    ?.ifEmpty { null }
                    ?: listOf("Hola, bienvenido a NGD Tech Solutions. ¿En qué puedo ayudarte hoy?")

                val selectedMessage = welcomeMessages[random.nextInt(welcomeMessages.size)]

                val welcomeAiMessage = ChatMessageModel(
                    id = "",
                    message = selectedMessage,
                    senderId = "ai_assistant",
                    senderName = "Asistente IA",
                    senderType = ChatMessageModel.SENDER_TYPE_AI,
                    timestamp = Timestamp.now(),
                    status = "sent"
                )

                chatRepository.sendAiMessage(companyId, channelId, welcomeAiMessage)
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo enviar la bienvenida automática", e)
            }
        }
    }

    fun setChannelMuted(
        companyId: String,
        channelId: String,
        userId: String,
        muted: Boolean
    ) {
        viewModelScope.launch {
            chatRepository.setChannelMuted(companyId, channelId, userId, muted)
        }
    }

    fun setChannelArchived(
        companyId: String,
        channelId: String,
        archived: Boolean
    ) {
        viewModelScope.launch {
            chatRepository.setChannelArchived(companyId, channelId, archived)
        }
    }

    fun createPrivateChannel(
        companyId: String,
        channelId: String,
        adminUid: String,
        memberUid: String
    ) {
        viewModelScope.launch {
            channelRepository.createPrivateChannel(
                companyId,
                channelId,
                adminUid,
                memberUid
            )
        }
    }

    fun updateLastRead(
        companyId: String,
        channelId: String,
        userId: String
    ) {
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("companies")
                    .document(companyId)
                    .collection("channels")
                    .document(channelId)
                    .update("unreadCount.$userId", 0)
            } catch (e: Exception) {
                // El canal puede no existir aún
            }
        }
    }

    fun markChatAsRead(
        companyId: String,
        channelId: String,
        isAdmin: Boolean
    ) {
        viewModelScope.launch {
            chatRepository.markChatAsRead(companyId, channelId, isAdmin)
        }
    }

    fun editMessage(
        companyId: String,
        channelId: String,
        messageId: String,
        newText: String
    ) {
        if (companyId.isBlank() || channelId.isBlank() || messageId.isBlank() || newText.isBlank()) {
            _error.postValue("No se pudo editar el mensaje")
            return
        }

        viewModelScope.launch {
            try {
                chatRepository.editMessage(companyId, channelId, messageId, newText)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "No se pudo editar el mensaje")
            }
        }
    }

    fun deleteMessage(
        companyId: String,
        channelId: String,
        messageId: String
    ) {
        if (companyId.isBlank() || channelId.isBlank() || messageId.isBlank()) {
            _error.postValue("No se pudo borrar el mensaje")
            return
        }

        viewModelScope.launch {
            try {
                chatRepository.deleteMessage(companyId, channelId, messageId)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "No se pudo borrar el mensaje")
            }
        }
    }

    fun clearChat(
        companyId: String,
        channelId: String
    ) {
        if (companyId.isBlank() || channelId.isBlank()) {
            _error.postValue("No se pudo borrar el chat")
            return
        }

        viewModelScope.launch {
            try {
                chatRepository.clearChat(companyId, channelId)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "No se pudo borrar el chat")
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        typingListenerCleanup?.invoke()
    }
}
