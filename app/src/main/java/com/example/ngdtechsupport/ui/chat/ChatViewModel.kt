package com.example.ngdtechsupport.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.data.repository.ChatRepository

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _messages = MutableLiveData<List<ChatMessageModel>>()
    val messages: LiveData<List<ChatMessageModel>> = _messages

    private val currentList = mutableListOf<ChatMessageModel>()

    fun loadInitial(companyId: String, businessId: String) {
        repository.loadInitialMessages(companyId, businessId) { list ->
            currentList.clear()
            currentList.addAll(list)
            _messages.postValue(currentList.toList())
        }
    }

    fun loadMore(companyId: String, businessId: String) {
        repository.loadMoreMessages(companyId, businessId) { list ->
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

        repository.sendMessage(
            companyId,
            businessId,
            text,
            senderId,
            replyToId,
            replyToText
        )
    }
}