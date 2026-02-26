package com.example.ngdtechsupport.ui.channel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.data.repository.ChannelRepository
import kotlinx.coroutines.launch

class ChannelViewModel : ViewModel() {

    private val repository = ChannelRepository()

    private val _channels = MutableLiveData<List<ChannelModel>>()
    val channels: LiveData<List<ChannelModel>> = _channels

    val visibleChannels = MediatorLiveData<List<ChannelModel>>()

    init {
        visibleChannels.addSource(_channels) { list ->
            visibleChannels.value = list.filter { !it.isArchived }
        }
    }

    fun loadChannels(companyId: String) {
        repository.listenChannels(companyId) { list ->
            _channels.postValue(list)
        }
    }

    fun archiveChannel(
        companyId: String,
        channelId: String,
        archived: Boolean
    ) {
        viewModelScope.launch {
            repository.archiveChannel(companyId, channelId, archived)
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
}