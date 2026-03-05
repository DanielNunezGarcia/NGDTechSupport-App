package com.example.ngdtechsupport.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngdtechsupport.data.CompanyRepository
import kotlinx.coroutines.launch

class AppDetailViewModel : ViewModel() {

    private val repository = CompanyRepository()

    private val _uiState = MutableLiveData(AppDetailUiState())
    val uiState: LiveData<AppDetailUiState> = _uiState

    fun loadBusiness(
        companyId: String,
        businessId: String
    ) {

        _uiState.value = AppDetailUiState(isLoading = true)

        viewModelScope.launch {

            try {

                val business = repository.getBusiness(
                    companyId,
                    businessId
                )

                _uiState.value = AppDetailUiState(
                    business = business
                )

            } catch (e: Exception) {

                _uiState.value = AppDetailUiState(
                    hasError = true,
                    errorMessage = e.message
                )
            }
        }
    }
}