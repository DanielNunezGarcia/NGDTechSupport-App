package com.example.ngdtechsupport.ui.auth

// Estados posibles de la pantalla de login
sealed class LoginUiState {
    object Idle : LoginUiState()          // Pantalla inicial, sin hacer login aún
    object Loading : LoginUiState()       // Intentando hacer login
    object Success : LoginUiState()       // Login correcto
    data class Error(val message: String) : LoginUiState() // Error de validación o de Firebase
}