package com.example.ngdtechsupport.ui.channel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.data.repository.ChatRepository

class ChannelViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _channels = MutableLiveData<List<ChannelModel>>()
    val channels: LiveData<List<ChannelModel>> = _channels

    private val _totalUnread = MutableLiveData<Int>()
    val totalUnread: LiveData<Int> = _totalUnread

    fun loadChannels(companyId: String, businessId: String, userId: String) {
        repository.listenChannels(companyId, businessId) { channels ->
            _channels.postValue(channels)

            val total = channels.sumOf {
                it.unreadCount[userId]?.toInt() ?: 0
            }

            _totalUnread.postValue(total)
        }
    }
}