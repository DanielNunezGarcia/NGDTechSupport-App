package com.example.ngdtechsupport.ui.activity

import com.example.ngdtechsupport.model.BusinessModel

data class AppDetailUiState(

    val isLoading: Boolean = false,
    val business: BusinessModel? = null,

    val hasError: Boolean = false,
    val errorMessage: String? = null
)