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

    private val auth = FirebaseAuth.getInstance()

    // Estado único con toda la información
    private val _uiState = MutableLiveData<DashboardUiState>(
        DashboardUiState(isLoading = false)
    )
    val uiState: LiveData<DashboardUiState> = _uiState

    fun loadAppsForCurrentUser() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            _uiState.value = DashboardUiState(
                isLoading = false,
                errorMessage = "Usuario no autenticado"
            )
            return
        }

        // Estado de carga
        _uiState.value = DashboardUiState(isLoading = true)

        // Cargamos usuario + apps en una sola corrutina
        viewModelScope.launch {
            try {
                // 1) Obtener el usuario y su información
                val user = userRepository.getUser(uid)
                val role = user?.role ?: "user"
                val userName = user?.name ?: ""
                val companyName = user?.company ?: ""

                // 2) Según el rol, cargamos todas las apps o solo las del usuario
                val apps = if (role.equals("ADMIN", ignoreCase = true)) {
                    appRepository.getAllApps()
                } else {
                    appRepository.getAppsForUser(uid)
                }

                // 3) Actualizar estado de UI con toda la información
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    apps = apps,
                    userRole = role,
                    userName = userName,
                    companyName = companyName,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    errorMessage = "Error al cargar las aplicaciones: ${e.message}"
                )
            }
        }
    }
}