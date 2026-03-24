package com.example.ngdtechsupport.ui.admin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AiConfigViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    sealed class UiState {
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _companyId = MutableLiveData<String>()
    val companyId: LiveData<String> = _companyId

    private val _aiEnabled = MutableLiveData<Boolean>(false)
    val aiEnabled: LiveData<Boolean> = _aiEnabled

    private val _greetingMessages = MutableLiveData<List<String>>(emptyList())
    val greetingMessages: LiveData<List<String>> = _greetingMessages

    private val _quickReplies = MutableLiveData<List<String>>(emptyList())
    val quickReplies: LiveData<List<String>> = _quickReplies

    private val _escalationKeywords = MutableLiveData<List<String>>(emptyList())
    val escalationKeywords: LiveData<List<String>> = _escalationKeywords

    private val _fallbackMessage = MutableLiveData<String>("")
    val fallbackMessage: LiveData<String> = _fallbackMessage

    private val _autoTransferEnabled = MutableLiveData<Boolean>(false)
    val autoTransferEnabled: LiveData<Boolean> = _autoTransferEnabled

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    fun loadConfig(companyId: String) {
        if (companyId.isBlank()) {
            _uiState.value = UiState.Error("Company ID no puede estar vacío")
            return
        }

        _companyId.value = companyId
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val docRef = db.collection("companies")
                    .document(companyId)
                    .collection("ai_config")
                    .document("settings")
                
                Log.d("AiConfigViewModel", "Loading config from: companies/$companyId/ai_config/settings")
                
                val document = docRef.get().await()

                if (document.exists()) {
                    _aiEnabled.value = document.getBoolean("aiEnabled") ?: false
                    _greetingMessages.value = (document.get("greetingMessages") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    _quickReplies.value = (document.get("quickReplies") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    _escalationKeywords.value = (document.get("escalationKeywords") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    _fallbackMessage.value = document.getString("fallbackMessage") ?: ""
                    _autoTransferEnabled.value = document.getBoolean("autoTransferEnabled") ?: false
                    Log.d("AiConfigViewModel", "Config loaded successfully")
                } else {
                    Log.d("AiConfigViewModel", "Config document does not exist, using defaults")
                }
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                Log.e("AiConfigViewModel", "Error loading config", e)
                _uiState.value = UiState.Error(e.message ?: "Error al cargar configuración")
            }
        }
    }

    fun saveConfig(companyId: String) {
        if (companyId.isBlank()) {
            _uiState.value = UiState.Error("Company ID no puede estar vacío")
            return
        }

        _companyId.value = companyId
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val configData = hashMapOf(
                    "aiEnabled" to (_aiEnabled.value ?: false),
                    "autoGreeting" to true,
                    "greetingMessages" to (_greetingMessages.value ?: emptyList<String>()),
                    "quickReplies" to (_quickReplies.value ?: emptyList<String>()),
                    "escalationKeywords" to (_escalationKeywords.value ?: emptyList<String>()),
                    "fallbackMessage" to (_fallbackMessage.value ?: ""),
                    "autoTransferEnabled" to (_autoTransferEnabled.value ?: false)
                )

                Log.d("AiConfigViewModel", "Saving config to: companies/$companyId/ai_config/settings")

                db.collection("companies")
                    .document(companyId)
                    .collection("ai_config")
                    .document("settings")
                    .set(configData)
                    .await()

                Log.d("AiConfigViewModel", "Config saved successfully")
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                Log.e("AiConfigViewModel", "Error saving config", e)
                _uiState.value = UiState.Error(e.message ?: "Error al guardar configuración")
            }
        }
    }

    fun setAiEnabled(enabled: Boolean) {
        _aiEnabled.value = enabled
    }

    fun setGreetingMessages(messages: List<String>) {
        _greetingMessages.value = messages
    }

    fun setQuickReplies(replies: List<String>) {
        _quickReplies.value = replies
    }

    fun setEscalationKeywords(keywords: List<String>) {
        _escalationKeywords.value = keywords
    }

    fun setFallbackMessage(message: String) {
        _fallbackMessage.value = message
    }

    fun setAutoTransferEnabled(enabled: Boolean) {
        _autoTransferEnabled.value = enabled
    }
}
