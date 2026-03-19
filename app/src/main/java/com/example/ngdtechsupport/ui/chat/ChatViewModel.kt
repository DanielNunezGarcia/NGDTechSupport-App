package com.example.ngdtechsupport.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.ngdtechsupport.ai.AiResponseHelper
import com.google.firebase.Timestamp

class ChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()
    private val channelRepository = com.example.ngdtechsupport.data.repository.ChannelRepository()

    private val _messages = MutableLiveData<List<ChatMessageModel>>()
    val messages: LiveData<List<ChatMessageModel>> = _messages

    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private val currentUserName: String
        get() = FirebaseAuth.getInstance().currentUser?.displayName ?: "Usuario"

    private val currentList = mutableListOf<ChatMessageModel>()

    private val _channels = MutableLiveData<List<ChannelModel>>()
    val channels: LiveData<List<ChannelModel>> = _channels

    private val _typingUsers = MutableLiveData<Map<String, Any>>()
    val typingUsers: LiveData<Map<String, Any>> = _typingUsers

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
        viewModelScope.launch {
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
        }
    }

    fun processMessageWithAi(companyId: String, channelId: String, userMessage: String) {
        viewModelScope.launch {
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

    override fun onCleared() {
        super.onCleared()
        typingListenerCleanup?.invoke()
    }
}
