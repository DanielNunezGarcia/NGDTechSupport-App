package com.example.ngdtechsupport.ui.dashboard

import com.example.ngdtechsupport.model.AppModel

// Representa todos los posibles estados de la pantalla de Dashboard
sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val apps: List<AppModel>) : DashboardUiState()
    object Empty : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}