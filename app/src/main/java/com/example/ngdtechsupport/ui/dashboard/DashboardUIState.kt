package com.example.ngdtechsupport.ui.dashboard

import com.example.ngdtechsupport.model.AppModel

// Representa todos los posibles estados de la pantalla de Dashboard
data class DashboardUiState(
    val isLoading: Boolean = false,
    val apps: List<AppModel> = emptyList(),
    val userRole: String = ""
)
