package com.example.ngdtechsupport.ui.updates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngdtechsupport.model.UpdateModel
import com.example.ngdtechsupport.data.UpdatesRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import android.util.Log

class UpdatesViewModel : ViewModel() {

    companion object {
        private const val TAG = "UpdatesViewModel"
        private const val DEFAULT_COMPANY_ID = "NGDStudios"
        private const val DEFAULT_BUSINESS_ID = "restaurante_madrid"
    }

    private val repository = UpdatesRepository()
    private var listenerRegistration: ListenerRegistration? = null

    private val _updates = MutableLiveData<List<UpdateModel>>(emptyList())
    val updates: LiveData<List<UpdateModel>> = _updates

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun listenUpdates(
        companyId: String,
        businessId: String,
    ) {
        val safeCompanyId = normalizeCompanyId(companyId)
        val safeBusinessId = normalizeBusinessId(businessId)
        Log.d(TAG, "listenUpdates with companyId=$safeCompanyId, businessId=$safeBusinessId")

        listenerRegistration?.remove()
        listenerRegistration = repository.listenUpdates(
            companyId = safeCompanyId,
            businessId = safeBusinessId,
            onResult = {
                _error.postValue(null)
                _updates.postValue(it)
            },
            onError = { throwable ->
                Log.e(TAG, "listenUpdates failed: ${throwable.message}", throwable)
                _error.postValue("No se pudieron cargar las novedades. ${throwable.message.orEmpty()}")
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun pauseListeners() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun resumeListeners(companyId: String, businessId: String) {
        if (listenerRegistration == null) {
            listenUpdates(companyId, businessId)
        }
    }

    fun createUpdate(
        companyId: String,
        businessId: String,
        title: String,
        description: String,
        version: String,
        type: String,
        adminId: String,
        priority: Int = 1,
        color: String = "#2196F3",
        publishDate: Long = 0
    ) {
        viewModelScope.launch {
            try {
                repository.createUpdate(
                    companyId = normalizeCompanyId(companyId),
                    businessId = normalizeBusinessId(businessId),
                    title = title,
                    description = description,
                    type = type,
                    version = version,
                    adminId = adminId,
                    priority = priority,
                    color = color,
                    publishDate = publishDate
                )
                _error.postValue(null)
            } catch (e: Exception) {
                Log.e(TAG, "createUpdate failed: ${e.message}", e)
                _error.postValue("No se pudo publicar el update. ${e.message.orEmpty()}")
            }
        }
    }

    fun markUpdatesRead(userId: String) {
        Log.d(TAG, "markUpdatesRead called for userId: $userId")
        viewModelScope.launch {
            try {
                repository.markUpdatesAsRead(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error marking updates as read: ${e.message}", e)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun normalizeCompanyId(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return DEFAULT_COMPANY_ID
        if (trimmed.equals(DEFAULT_COMPANY_ID, ignoreCase = true)) return DEFAULT_COMPANY_ID
        return sanitizeFirestoreId(trimmed).ifBlank { DEFAULT_COMPANY_ID }
    }

    private fun normalizeBusinessId(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return DEFAULT_BUSINESS_ID
        if (trimmed.equals(DEFAULT_BUSINESS_ID, ignoreCase = true)) return DEFAULT_BUSINESS_ID
        if (trimmed.equals("Restaurante Madrid", ignoreCase = true)) return DEFAULT_BUSINESS_ID
        if (trimmed.equals("restaurante-madrid", ignoreCase = true)) return DEFAULT_BUSINESS_ID
        val normalized = if (trimmed.contains(" ")) {
            trimmed.lowercase().replace(" ", "_")
        } else {
            trimmed
        }
        return sanitizeFirestoreId(normalized).ifBlank { DEFAULT_BUSINESS_ID }
    }

    private fun sanitizeFirestoreId(raw: String): String {
        return raw
            .trim()
            .replace("/", "_")
            .replace("#", "_")
            .replace("?", "_")
    }
}
