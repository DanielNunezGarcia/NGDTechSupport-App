package com.example.ngdtechsupport.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.ngdtechsupport.data.FirebaseAppRepository
import com.example.ngdtechsupport.data.AppRepository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// ViewModel del Dashboard: contiene la lógica de carga de apps
class DashboardViewModel(
    private val repo: AppRepository = FirebaseAppRepository()
) : ViewModel() {

    private val _uiState = MutableLiveData<DashboardUiState>()
    val uiState: LiveData<DashboardUiState> = _uiState

    fun loadAppsForCurrentUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            _uiState.value = DashboardUiState.Error("Usuario no autenticado")
            return
        }

        // Estado de carga
        _uiState.value = DashboardUiState.Loading

        // Llamada asíncrona al repositorio usando viewModelScope
        viewModelScope.launch {
            try {
                val apps = repo.getAppsForUser(uid)

                _uiState.value = if (apps.isNotEmpty()) {
                    DashboardUiState.Success(apps)
                } else {
                    DashboardUiState.Empty
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error("Error al cargar las aplicaciones")
            }
        }
    }
}