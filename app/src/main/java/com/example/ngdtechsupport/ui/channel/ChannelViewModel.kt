package com.example.ngdtechsupport.ui.channel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngdtechsupport.data.model.ChannelModel
import com.example.ngdtechsupport.data.repository.ChannelRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ChannelViewModel : ViewModel() {

    data class PrivateChannelCreationResult(
        val created: Boolean,
        val channelId: String = "",
        val errorMessage: String = ""
    )

    private val repository = ChannelRepository()

    private val _channels = MutableLiveData<List<ChannelModel>>()
    val channels: LiveData<List<ChannelModel>> = _channels

    val visibleChannels = MediatorLiveData<List<ChannelModel>>()

    private val _channelClickEvent = MutableLiveData<ChannelModel?>()
    val channelClickEvent: LiveData<ChannelModel?> = _channelClickEvent

    private val _privateChannelCreated = MutableLiveData<PrivateChannelCreationResult?>()
    val privateChannelCreated: LiveData<PrivateChannelCreationResult?> = _privateChannelCreated

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    fun clearPrivateChannelState() {
        _privateChannelCreated.value = null
    }



    init {
        visibleChannels.addSource(_channels) { list ->
            val uid = currentUserId
            visibleChannels.value = list.filter { channel ->
                if (channel.isArchived) {
                    false
                } else {
                    val members = channel.members
                    members.isEmpty() || uid.isBlank() || members.containsKey(uid)
                }
            }
        }
    }

    fun loadChannels(companyId: String) {
        if (companyId.isBlank()) {
            _channels.postValue(emptyList())
            return
        }
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
        Log.d("ChannelViewModel", "createPrivateChannel called with companyId=$companyId, channelId=$channelId, adminUid=$adminUid, memberUid=$memberUid")
        if (companyId.isEmpty() || channelId.isEmpty() || adminUid.isEmpty() || memberUid.isEmpty()) {
            Log.e("ChannelViewModel", "Invalid parameters: companyId=$companyId, channelId=$channelId, adminUid=$adminUid, memberUid=$memberUid")
            _privateChannelCreated.postValue(
                PrivateChannelCreationResult(
                    created = false,
                    errorMessage = "Datos incompletos para crear canal privado"
                )
            )
            return
        }
        viewModelScope.launch {
            try {
                val success = repository.createPrivateChannel(
                    companyId,
                    channelId,
                    adminUid,
                    memberUid
                )
                if (success) {
                    Log.d("ChannelViewModel", "Private channel created successfully")
                    _privateChannelCreated.postValue(
                        PrivateChannelCreationResult(created = true, channelId = channelId)
                    )
                } else {
                    Log.e("ChannelViewModel", "Failed to create private channel")
                    _privateChannelCreated.postValue(
                        PrivateChannelCreationResult(
                            created = false,
                            errorMessage = "No se pudo guardar el canal privado"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("ChannelViewModel", "Error creating private channel: ${e.message}", e)
                _privateChannelCreated.postValue(
                    PrivateChannelCreationResult(
                        created = false,
                        errorMessage = "Error inesperado al crear el canal privado"
                    )
                )
            }
        }
    }
}
