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
import android.util.Log

// ViewModel del Dashboard: contiene la lógica de carga de apps y el rol del usuario
class DashboardViewModel(
    private val appRepository: AppRepository = FirebaseAppRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    companion object {
        private const val DEFAULT_COMPANY_ID = "NGDStudios"
        private const val DEFAULT_BUSINESS_ID = "restaurante_madrid"
    }

    private val companyRepository = CompanyRepository()
    private val auth = FirebaseAuth.getInstance()

    // Estado único con toda la información
    private val _uiState = MutableLiveData<DashboardUiState>(
        DashboardUiState(isLoading = false)
    )
    val uiState: LiveData<DashboardUiState> = _uiState

    fun loadAppsForCurrentUser() {
        val uid = auth.currentUser?.uid
        Log.d("DashboardViewModel", "loadAppsForCurrentUser called with uid: $uid")

        if (uid == null) {
            Log.e("DashboardViewModel", "User not authenticated")
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
                Log.d("DashboardViewModel", "User obtained: $user")
                
                if (user == null) {
                    Log.e("DashboardViewModel", "User is null for uid: $uid")
                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        errorMessage = "Usuario no encontrado. UID: $uid. Verifica que el documento exista en la colección 'users' o 'admin'."
                    )
                    return@launch
                }

                val role = user.role.uppercase()
                val userName = user.name
                val companyName = user.company
                val companyId = normalizeCompanyId(user.companyId, user.company)
                val businessId = normalizeBusinessId(user.businessId)
                Log.d("DashboardViewModel", "User role: $role, companyId: $companyId, businessId: $businessId")

                // 2) Según el rol, cargamos diferentes datos usando loadBusinessesForUser
                val apps = loadBusinessesForUser(user.copy(companyId = companyId, businessId = businessId))
                Log.d("DashboardViewModel", "Loaded ${apps.size} apps for user")

                // 3) Actualizar estado de UI con toda la información
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    apps = apps,
                    userRole = role,
                    userName = userName,
                    companyName = companyName,
                    companyId = companyId,
                    businessId = businessId,
                    errorMessage = null
                )
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Exception in loadAppsForCurrentUser", e)
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
        val companyId = normalizeCompanyId(user.companyId, user.company)
        val businessId = normalizeBusinessId(user.businessId)
        val uid = auth.currentUser?.uid ?: ""
        Log.d("DashboardViewModel", "loadBusinessesForUser - role: $role, companyId: $companyId, businessId: $businessId")

        return when (role) {
            "ADMIN" -> {
                // ADMIN → puede ver todos los negocios de su compañía
                val businesses = companyRepository.getBusinesses(companyId).filter { it.isActive }
                businesses.map { business ->
                    business.toAppModel(uid = uid, companyId = companyId)
                }
            }
            "SOPORTE", "CLIENT" -> {
                // SOPORTE y CLIENT → ven solo su negocio específico
                Log.d("DashboardViewModel", "SOPORTE/CLIENT branch - companyId: $companyId, businessId: $businessId")
                val resolvedBusiness = findBusinessForClient(companyId, businessId)
                if (resolvedBusiness != null) {
                    listOf(resolvedBusiness.toAppModel(uid = uid, companyId = companyId))
                } else {
                    Log.e(
                        "DashboardViewModel",
                        "No business found for companyId=$companyId and businessId=$businessId"
                    )
                    emptyList()
                }
            }
            else -> {
                // Por defecto: apps del usuario
                appRepository.getAppsForUser(uid)
            }
        }
    }

    private suspend fun findBusinessForClient(companyId: String, requestedBusinessId: String): BusinessModel? {
        val normalizedRequested = normalizeBusinessKey(requestedBusinessId)

        val directBusiness = companyRepository.getBusiness(companyId, requestedBusinessId)
        if (directBusiness != null) return directBusiness

        if (requestedBusinessId != DEFAULT_BUSINESS_ID) {
            val defaultBusiness = companyRepository.getBusiness(companyId, DEFAULT_BUSINESS_ID)
            if (defaultBusiness != null) return defaultBusiness
        }

        val allBusinesses = companyRepository.getBusinesses(companyId)
        if (allBusinesses.isEmpty()) return null

        val normalizedDefault = normalizeBusinessKey(DEFAULT_BUSINESS_ID)

        return allBusinesses.firstOrNull {
            normalizeBusinessKey(it.id) == normalizedRequested
        } ?: allBusinesses.firstOrNull {
            normalizeBusinessKey(it.name) == normalizedRequested
        } ?: allBusinesses.firstOrNull {
            normalizeBusinessKey(it.id) == normalizedDefault
        } ?: allBusinesses.firstOrNull {
            normalizeBusinessKey(it.name) == normalizedDefault
        }
    }

    private fun BusinessModel.toAppModel(uid: String, companyId: String): com.example.ngdtechsupport.model.AppModel {
        return com.example.ngdtechsupport.model.AppModel(
            id = id,
            name = name,
            clientId = uid,
            status = status,
            progress = progress,
            version = version,
            supportType = supportType,
            lastUpdate = lastUpdate,
            companyId = companyId
        )
    }

    private fun normalizeCompanyId(companyId: String, fallbackCompanyName: String): String {
        return companyId
            .trim()
            .ifBlank { fallbackCompanyName.trim() }
            .ifBlank { DEFAULT_COMPANY_ID }
    }

    private fun normalizeBusinessId(businessId: String): String {
        val trimmed = businessId.trim()
        if (trimmed.isBlank()) return DEFAULT_BUSINESS_ID
        if (trimmed.equals(DEFAULT_BUSINESS_ID, ignoreCase = true)) return DEFAULT_BUSINESS_ID
        if (trimmed.equals("Restaurante Madrid", ignoreCase = true)) return DEFAULT_BUSINESS_ID
        return if (trimmed.contains(" ")) {
            trimmed.lowercase().replace(" ", "_")
        } else {
            trimmed
        }
    }

    private fun normalizeBusinessKey(raw: String): String {
        return raw.trim().lowercase().replace(" ", "_")
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
