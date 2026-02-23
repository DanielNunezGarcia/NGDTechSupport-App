package com.example.ngdtechsupport.data.model

sealed class ChatItem {

    data class MessageItem(
        val message: ChatMessageModel
    ) : ChatItem()

    data class DateSeparator(
        val date: String
    ) : ChatItem()
}