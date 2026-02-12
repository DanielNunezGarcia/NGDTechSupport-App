package com.example.ngdtechsupport.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

// ViewModel para manejar la lógica de login
class LoginViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val uiState: LiveData<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Email y contraseña son obligatorios")
            return
        }

        _uiState.value = LoginUiState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _uiState.value = LoginUiState.Success
            }
            .addOnFailureListener { exception ->
                _uiState.value = LoginUiState.Error(
                    exception.message ?: "Error al iniciar sesión"
                )
            }
    }
}