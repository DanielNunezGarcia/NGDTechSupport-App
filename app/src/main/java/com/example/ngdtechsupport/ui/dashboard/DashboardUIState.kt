package com.example.ngdtechsupport.ui.dashboard

import com.example.ngdtechsupport.model.AppModel

// Estado del Dashboard con toda la información
data class DashboardUiState(
    val isLoading: Boolean = false,
    val apps: List<AppModel> = emptyList(),
    val userRole: String = "",
    val userName: String = "",
    val companyName: String = "",
    val errorMessage: String? = null
) {
    // Propiedades computadas para facilitar el uso
    val hasError: Boolean get() = errorMessage != null
    val isEmpty: Boolean get() = !isLoading && apps.isEmpty() && errorMessage == null
    val isSuccess: Boolean get() = !isLoading && apps.isNotEmpty() && errorMessage == null
}