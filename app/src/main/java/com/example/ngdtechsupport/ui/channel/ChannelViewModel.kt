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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class ChannelViewModel : ViewModel() {

    companion object {
        private const val CHANNELS_PAGE_SIZE = 20
    }

    data class PrivateChannelCreationResult(
        val created: Boolean,
        val channelId: String = "",
        val errorMessage: String = ""
    )

    private val repository = ChannelRepository()

    private val _channels = MutableLiveData<List<ChannelModel>>(emptyList())
    val channels: LiveData<List<ChannelModel>> = _channels

    private val _isInitialLoading = MutableLiveData(false)
    val isInitialLoading: LiveData<Boolean> = _isInitialLoading

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _canLoadMore = MutableLiveData(false)
    val canLoadMore: LiveData<Boolean> = _canLoadMore

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    val visibleChannels = MediatorLiveData<List<ChannelModel>>()

    private val _channelClickEvent = MutableLiveData<ChannelModel?>()
    val channelClickEvent: LiveData<ChannelModel?> = _channelClickEvent

    private val _privateChannelCreated = MutableLiveData<PrivateChannelCreationResult?>()
    val privateChannelCreated: LiveData<PrivateChannelCreationResult?> = _privateChannelCreated

    private var currentCompanyId: String = ""
    private var lastVisibleChannel: DocumentSnapshot? = null

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    private var listenerRegistration: ListenerRegistration? = null

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

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun loadChannels(companyId: String) {
        if (companyId.isBlank()) {
            _channels.postValue(emptyList())
            _canLoadMore.postValue(false)
            return
        }

        currentCompanyId = companyId
        lastVisibleChannel = null
        _error.value = null
        _canLoadMore.value = true
        _isInitialLoading.value = true
        loadChannelsPage(reset = true)
    }

    fun loadMoreChannels() {
        val companyId = currentCompanyId
        if (companyId.isBlank()) return
        if (_isInitialLoading.value == true || _isLoadingMore.value == true) return
        if (_canLoadMore.value != true) return

        _isLoadingMore.value = true
        loadChannelsPage(reset = false)
    }

    fun clearError() {
        _error.value = null
    }

    fun listenChannels(companyId: String) {
        listenerRegistration?.remove()
        listenerRegistration = repository.listenChannels(companyId) { channels ->
            _channels.postValue(channels)
        }
    }

    fun pauseListeners() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun resumeListeners(companyId: String) {
        if (listenerRegistration == null) {
            listenChannels(companyId)
        }
    }

    private fun loadChannelsPage(reset: Boolean) {
        val companyId = currentCompanyId
        if (companyId.isBlank()) {
            _isInitialLoading.postValue(false)
            _isLoadingMore.postValue(false)
            _canLoadMore.postValue(false)
            return
        }

        viewModelScope.launch {
            try {
                val page = repository.getChannelsPage(
                    companyId = companyId,
                    pageSize = CHANNELS_PAGE_SIZE,
                    lastVisible = if (reset) null else lastVisibleChannel
                )

                val current = if (reset) {
                    emptyList()
                } else {
                    _channels.value.orEmpty()
                }

                val merged = if (reset) {
                    page.channels
                } else {
                    val existingIds = current.map { it.id }.toHashSet()
                    current + page.channels.filterNot { existingIds.contains(it.id) }
                }

                lastVisibleChannel = page.lastVisible
                _channels.postValue(merged)
                _canLoadMore.postValue(page.hasMore && page.lastVisible != null)
                _error.postValue(null)
            } catch (e: Exception) {
                Log.e("ChannelViewModel", "Error loading channels page", e)
                _error.postValue("No se pudieron cargar los canales. ${e.message.orEmpty()}")
                _canLoadMore.postValue(false)
            } finally {
                _isInitialLoading.postValue(false)
                _isLoadingMore.postValue(false)
            }
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
