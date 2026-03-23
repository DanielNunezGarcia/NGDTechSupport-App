package com.example.ngdtechsupport.ui.channel

import android.util.Log
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

    private val _channelClickEvent = MutableLiveData<ChannelModel?>()
    val channelClickEvent: LiveData<ChannelModel?> = _channelClickEvent



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

    fun onChannelClick(channel: ChannelModel) {
        _channelClickEvent.value = channel
    }

    fun clearChannelClickEvent() {
        _channelClickEvent.value = null
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

    fun toggleMute(
        companyId: String,
        channelId: String,
        userId: String,
        muted: Boolean
    ) {
        viewModelScope.launch {
            repository.setChannelMuted(companyId, channelId, userId, muted)
        }
    }

    fun togglePin(
        companyId: String,
        channelId: String,
        pinned: Boolean
    ) {
        viewModelScope.launch {
            repository.setChannelPinned(companyId, channelId, pinned)
        }
    }

    fun createPrivateChannel(
        companyId: String,
        channelId: String,
        adminUid: String,
        memberUid: String
    ) {
        viewModelScope.launch {
            try {
                val success = repository.createPrivateChannel(
                    companyId,
                    channelId,
                    adminUid,
                    memberUid
                )
                if (success) {
                    // Canal creado sin mensaje
                } else {
                    // Error al crear canal, no mostrar toast
                    Log.e("ChannelViewModel", "Failed to create private channel")
                }
            } catch (e: Exception) {
                Log.e("ChannelViewModel", "Error creating private channel: ${e.message}", e)
            }
        }
    }
}
