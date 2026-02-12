package com.example.ngdtechsupport.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.ngdtechsupport.data.AppRepository
import com.example.ngdtechsupport.data.FirebaseAppRepository
import com.example.ngdtechsupport.data.UserRepository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// ViewModel del Dashboard: contiene la lógica de carga de apps y el rol del usuario
class DashboardViewModel(
    private val appRepository: AppRepository = FirebaseAppRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    // Estado de la lista de apps
    private val _uiState = MutableLiveData<DashboardUiState>()
    val uiState: LiveData<DashboardUiState> = _uiState

    // Rol del usuario actual (por ejemplo, "admin" o "user")
    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> = _userRole

    fun loadAppsForCurrentUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            _uiState.value = DashboardUiState.Error("Usuario no autenticado")
            return
        }

        // 1 - Cargamos el rol en paralelo
        viewModelScope.launch {
            val user = userRepository.getUser(uid)
            user?.role?.let { role ->
                _userRole.value = role
            }
        }

        // 2 - Cargamos las apps
        _uiState.value = DashboardUiState.Loading

        viewModelScope.launch {
            try {
                val apps = appRepository.getAppsForUser(uid)

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