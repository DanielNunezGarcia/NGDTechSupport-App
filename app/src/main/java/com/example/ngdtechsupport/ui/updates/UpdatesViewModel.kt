package com.example.ngdtechsupport.ui.updates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngdtechsupport.model.UpdateModel
import com.example.ngdtechsupport.data.UpdatesRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class UpdatesViewModel : ViewModel() {

    private val repository = UpdatesRepository()
    private var listenerRegistration: ListenerRegistration? = null

    private val _updates = MutableLiveData<List<UpdateModel>>(emptyList())
    val updates: LiveData<List<UpdateModel>> = _updates

    fun listenUpdates(
        companyId: String,
        businessId: String,
    ) {
        listenerRegistration?.remove()
        listenerRegistration = repository.listenUpdates(companyId, businessId) {
            _updates.value = it
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun createUpdate(
        companyId: String,
        businessId: String,
        title: String,
        description: String,
        version: String,
        type: String,
        adminId: String
    ) {

        viewModelScope.launch {

            repository.createUpdate(
                companyId,
                businessId,
                title,
                description,
                type,
                version,
                adminId
            )
        }
    }

    fun markUpdatesRead(userId: String) {
        android.util.Log.d("UpdatesViewModel", "markUpdatesRead called for userId: $userId")
        viewModelScope.launch {
            try {
                repository.markUpdatesAsRead(userId)
            } catch (e: Exception) {
                android.util.Log.e("UpdatesViewModel", "Error marking updates as read", e)
            }
        }
    }
}