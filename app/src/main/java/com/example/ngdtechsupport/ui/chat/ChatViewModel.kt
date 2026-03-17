package com.example.ngdtechsupport.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.data.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.example.ngdtechsupport.data.repository.ChannelRepository
import com.example.ngdtechsupport.ai.AiResponseHelper
import com.google.firebase.Timestamp

class ChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()
    private val channelRepository = ChannelRepository()
    private val repository = ChannelRepository()

    private val _messages = MutableLiveData<List<ChatMessageModel>>()
    val messages: LiveData<List<ChatMessageModel>> = _messages

    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private val currentUserName: String
        get() = FirebaseAuth.getInstance().currentUser?.displayName ?: "Usuario"

    private val currentList = mutableListOf<ChatMessageModel>()

    private val _channels = MutableLiveData<List<ChannelModel>>()
    val channels: LiveData<List<ChannelModel>> = _channels

    // Typing indicator state
    private val _typingUsers = MutableLiveData<Map<String, Any>>()
    val typingUsers: LiveData<Map<String, Any>> = _typingUsers

    private var typingListenerCleanup: (() -> Unit)? = null

    // Cargado inicial de los mensajes
    fun loadInitial(companyId: String, businessId: String) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        chatRepository.loadInitialMessages(companyId, businessId) { list ->
            _messages.postValue(list)
        }

        resetUnread(companyId, businessId, uid)
    }

    fun listenMessages(companyId: String, businessId: String) {

        chatRepository.listenMessages(companyId, businessId) { messages ->
            _messages.postValue(messages)
        }
    }

    // Escuchar indicador de typing
    fun listenTyping(companyId: String, channelId: String) {
        val uid = currentUserId ?: return
        
        chatRepository.listenTyping(companyId, channelId, uid) { typing ->
            _typingUsers.postValue(typing)
        }
    }

    // Establecer que el usuario está escribiendo
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

    // Cargar más compañías en el caso de que haya más
    fun loadMore(companyId: String, businessId: String) {
        chatRepository.loadMoreMessages(companyId, businessId) { list ->
            currentList.addAll(0, list)
            _messages.postValue(currentList.toList())
        }
    }

    fun listenChannels(companyId: String, businessId: String) {

        chatRepository.listenChannels(companyId, businessId) { channelList ->

            _channels.value = channelList

        }
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
            
            // Dejar de mostrar "escribiendo" al enviar
            chatRepository.setTyping(companyId, channelId, senderId, "", false)

            // Procesar mensaje con IA
            processMessageWithAi(companyId, channelId, text)
        }
    }

    // Procesar mensaje con IA
    fun processMessageWithAi(companyId: String, channelId: String, userMessage: String) {
        viewModelScope.launch {
            // Obtener respuesta de IA
            val aiResponse = AiResponseHelper.getResponse(userMessage)

            // Crear mensaje de IA
            val aiMessage = ChatMessageModel(
                id = "",
                message = aiResponse,
                senderId = "ai_assistant",
                senderName = "Asistente IA",
                senderType = ChatMessageModel.SENDER_TYPE_AI,
                timestamp = Timestamp.now(),
                status = "sent"
            )

            // Añadir a la lista de mensajes
            val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
            currentMessages.add(aiMessage)
            _messages.postValue(currentMessages)

            // Guardar en Firestore
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
            repository.createPrivateChannel(
                companyId,
                channelId,
                adminUid,
                memberUid
            )
        }
    }

    fun updateLastRead(
        companyId: String,
        businessId: String,
        userId: String
    ) {

        viewModelScope.launch {

            chatRepository.updateLastRead(
                companyId,
                businessId,
                userId
            )

        }
    }

    fun markChatAsRead(
        companyId: String,
        channelId: String,
        isAdmin: Boolean
    ) {

        viewModelScope.launch {

            chatRepository.markChatAsRead(
                companyId,
                channelId,
                isAdmin
            )
        }
    }

    fun resetUnread(
        companyId: String,
        channelId: String,
        userId: String
    ) {
        viewModelScope.launch {
            FirebaseFirestore.getInstance()
                .collection("companies")
                .document(companyId)
                .collection("channels")
                .document(channelId)
                .update("unreadCount.$userId", 0)
        }
    }

    override fun onCleared() {
        super.onCleared()
        typingListenerCleanup?.invoke()
    }
}