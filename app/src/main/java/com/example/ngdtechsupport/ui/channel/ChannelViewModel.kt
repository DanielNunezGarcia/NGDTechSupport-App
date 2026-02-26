package com.example.ngdtechsupport.ui.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.data.repository.ChannelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChannelViewModel : ViewModel() {

    private val repository = ChannelRepository()

    private val _channels = MutableStateFlow<List<ChannelModel>>(emptyList())
    val channels: StateFlow<List<ChannelModel>> = _channels

    fun loadChannels(companyId: String) {
        viewModelScope.launch {
            _channels.value = repository.getChannels(companyId)
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

    fun visibleChannels(currentUserId: String): List<ChannelModel> {
        return _channels.value.filter { channel ->
            channel.members.containsKey(currentUserId)
        }
    }

    fun archiveChannel(companyId: String, channelId: String, archived: Boolean) {
        viewModelScope.launch {
            repository.archiveChannel(companyId, channelId, archived)
        }
    }
}