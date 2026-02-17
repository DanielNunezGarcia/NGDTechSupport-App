package com.example.ngdtechsupport.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngdtechsupport.data.AppRepository
import com.example.ngdtechsupport.data.FirebaseAppRepository
import com.example.ngdtechsupport.data.UserRepository
import com.example.ngdtechsupport.data.CompanyRepository
import com.example.ngdtechsupport.model.BusinessModel
import com.example.ngdtechsupport.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// ViewModel del Dashboard: contiene la lógica de carga de apps y el rol del usuario
class DashboardViewModel(
    private val appRepository: AppRepository = FirebaseAppRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val companyRepository = CompanyRepository()
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
                
                if (user == null) {
                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        errorMessage = "Usuario no encontrado. UID: $uid. Verifica que el documento exista en la colección 'users' o 'admin'."
                    )
                    return@launch
                }

                val role = user.role.uppercase()
                val userName = user.name
                val companyName = user.company
                val companyId = user.companyId
                val businessId = user.businessId

                // 2) Según el rol, cargamos diferentes datos usando loadBusinessesForUser
                val apps = loadBusinessesForUser(user)

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

    /**
     * Carga los negocios/apps según el rol del usuario
     * Convierte BusinessModel a AppModel cuando es necesario
     */
    private suspend fun loadBusinessesForUser(user: UserModel): List<com.example.ngdtechsupport.model.AppModel> {
        val role = user.role.uppercase()
        val companyId = user.companyId
        val businessId = user.businessId
        val uid = auth.currentUser?.uid ?: ""

        return when (role) {
            "ADMIN" -> {
                // ADMIN → puede ver todos los negocios de su compañía
                if (companyId.isNotEmpty()) {
                    // Convertir BusinessModel a AppModel
                    val businesses = companyRepository.getBusinesses(companyId)
                    businesses.map { business ->
                        com.example.ngdtechsupport.model.AppModel(
                            id = business.id,
                            name = business.name,
                            clientId = uid,
                            status = business.status,
                            lastUpdate = business.lastUpdate
                        )
                    }
                } else {
                    // Fallback: todas las apps si no tiene companyId
                    appRepository.getAllApps()
                }
            }
            "SOPORTE", "CLIENT" -> {
                // SOPORTE y CLIENT → ven solo su negocio específico
                if (companyId.isNotEmpty() && businessId.isNotEmpty()) {
                    // Intentar obtener el negocio específico desde CompanyRepository
                    val businesses = companyRepository.getBusinesses(companyId)
                    val singleBusiness = businesses.find { it.id == businessId }
                    
                    if (singleBusiness != null) {
                        // Convertir BusinessModel a AppModel
                        listOf(
                            com.example.ngdtechsupport.model.AppModel(
                                id = singleBusiness.id,
                                name = singleBusiness.name,
                                clientId = uid,
                                status = singleBusiness.status,
                                lastUpdate = singleBusiness.lastUpdate
                            )
                        )
                    } else {
                        // Fallback: usar AppRepository
                        appRepository.getSingleBusiness(companyId, businessId)
                    }
                } else {
                    // Fallback: apps del usuario si no tiene companyId/businessId
                    appRepository.getAppsForUser(uid)
                }
            }
            else -> {
                // Por defecto: apps del usuario
                appRepository.getAppsForUser(uid)
            }
        }
    }

    // Función auxiliar para verificar permisos
    fun canCreateBusinesses(): Boolean {
        val currentState = _uiState.value
        return currentState?.userRole == "ADMIN"
    }

    fun canPublishUpdates(): Boolean {
        val currentState = _uiState.value
        return currentState?.userRole == "ADMIN"
    }

    fun canRespondChats(): Boolean {
        val currentState = _uiState.value
        return currentState?.userRole == "SOPORTE" || currentState?.userRole == "ADMIN"
    }

    fun canViewAndChat(): Boolean {
        val currentState = _uiState.value
        return currentState?.userRole == "CLIENT" || 
               currentState?.userRole == "SOPORTE" || 
               currentState?.userRole == "ADMIN"
    }
}