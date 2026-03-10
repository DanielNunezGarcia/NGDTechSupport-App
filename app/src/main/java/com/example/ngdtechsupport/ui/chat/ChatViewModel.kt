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

class ChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()
    private val channelRepository = ChannelRepository()
    private val repository = ChannelRepository()

    private val _messages = MutableLiveData<List<ChatMessageModel>>()
    val messages: LiveData<List<ChatMessageModel>> = _messages

    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private val currentList = mutableListOf<ChatMessageModel>()

    private val _channels = MutableLiveData<List<ChannelModel>>()
    val channels: LiveData<List<ChannelModel>> = _channels

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
        businessId: String,
        text: String,
        senderId: String,
        replyToId: String?,
        replyToText: String?
    ) {
        viewModelScope.launch {
            chatRepository.sendMessage(
                companyId,
                businessId,
                text,
                senderId,
                replyToId,
                replyToText
            )
        }
    }

    fun setChannelMuted(
        companyId: String,
        channelId: String,
        userId: String,
        muted: Boolean
    ) {
        viewModelScope.launch {
            channelRepository.setChannelMuted(
                companyId,
                channelId,
                userId,
                muted
            )
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
}