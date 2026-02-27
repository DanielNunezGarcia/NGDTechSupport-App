package com.example.ngdtechsupport.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.data.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.data.repository.ChannelRepository

class ChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()
    private val channelRepository = ChannelRepository()
    private val repository = ChannelRepository()

    private val _messages = MutableLiveData<List<ChatMessageModel>>()
    val messages: LiveData<List<ChatMessageModel>> = _messages

    private val currentList = mutableListOf<ChatMessageModel>()

    fun loadInitial(companyId: String, businessId: String) {
        chatRepository.loadInitialMessages(companyId, businessId) { list ->
            currentList.clear()
            currentList.addAll(list)
            _messages.postValue(currentList.toList())
        }

        resetUnread(companyId, businessId, currentUserId)
    }

    fun loadMore(companyId: String, businessId: String) {
        chatRepository.loadMoreMessages(companyId, businessId) { list ->
            currentList.addAll(0, list)
            _messages.postValue(currentList.toList())
        }
    }

    fun sendMessage(
        companyId: String,
        businessId: String,
        text: String,
        senderId: String,
        replyToId: String?,
        replyToText: String?,
        replyTo: String? = null
    ) {

        chatRepository.sendMessage(
            companyId,
            businessId,
            text,
            senderId,
            replyToId,
            replyToText
        )
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