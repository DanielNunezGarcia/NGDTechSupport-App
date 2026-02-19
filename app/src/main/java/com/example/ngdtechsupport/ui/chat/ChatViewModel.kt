package com.example.ngdtechsupport.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngdtechsupport.data.model.ChatMessageModel
import com.example.ngdtechsupport.data.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _messages = MutableLiveData<List<ChatMessageModel>>()
    val messages: LiveData<List<ChatMessageModel>> = _messages

    fun loadMessages(companyId: String, businessId: String) {
        viewModelScope.launch {
            val result = repository.getMessages(companyId, businessId)
            _messages.value = result
        }
    }

    fun sendMessage(
        companyId: String,
        businessId: String,
        message: String,
        senderId: String,
        senderRole: String
    ) {
        viewModelScope.launch {
            repository.sendMessage(
                companyId,
                businessId,
                message,
                senderId,
                senderRole
            )
            loadMessages(companyId, businessId)
        }
    }
}