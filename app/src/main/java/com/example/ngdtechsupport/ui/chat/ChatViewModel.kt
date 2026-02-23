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

    private val _typingUser = MutableLiveData<String?>()
    val typingUser: LiveData<String?> = _typingUser

    fun listenMessages(companyId: String, businessId: String) {
        repository.listenForMessages(companyId, businessId) {
            _messages.postValue(it)
        }
    }

    fun sendMessage(
        companyId: String,
        businessId: String,
        text: String,
        senderId: String,
        senderName: String
    ) {
        repository.sendMessage(
            companyId,
            businessId,
            text,
            senderId,
            senderName
        )
    }

    fun markAsRead(
        companyId: String,
        businessId: String,
        currentUserId: String
    ) {
        repository.markMessagesAsRead(companyId, businessId, currentUserId)
    }

    fun setTyping(
        companyId: String,
        businessId: String,
        userId: String,
        userName: String,
        isTyping: Boolean
    ) {
        repository.setTypingStatus(companyId, businessId, userId, userName, isTyping)
    }

    fun listenTyping(companyId: String, businessId: String) {
        repository.listenTypingStatus(companyId, businessId) {
            _typingUser.postValue(it)
        }
    }
}