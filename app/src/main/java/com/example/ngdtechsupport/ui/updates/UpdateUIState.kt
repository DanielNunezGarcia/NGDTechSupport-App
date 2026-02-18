package com.example.ngdtechsupport.ui.updates

import com.example.ngdtechsupport.model.UpdateModel

data class UpdatesUiState(
    val isLoading: Boolean = false,
    val updates: List<UpdateModel> = emptyList(),
    val errorMessage: String? = null
)