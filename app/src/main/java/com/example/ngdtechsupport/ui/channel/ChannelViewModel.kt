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

    fun loadChannels(companyId: String, businessId: String) {
        repository.listenChannels(companyId, businessId) {
            _channels.postValue(it)
        }
    }
}