package com.example.ngdtechsupport.ui.updates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngdtechsupport.data.UpdatesRepository
import kotlinx.coroutines.launch

class UpdatesViewModel(
    private val repository: UpdatesRepository = UpdatesRepository()
) : ViewModel() {

    private val _uiState = MutableLiveData(UpdatesUiState())
    val uiState: LiveData<UpdatesUiState> = _uiState

    fun loadUpdates(companyId: String, businessId: String) {

        _uiState.value = UpdatesUiState(isLoading = true)

        viewModelScope.launch {
            try {
                val updates = repository.getUpdates(companyId, businessId)

                _uiState.value = UpdatesUiState(
                    isLoading = false,
                    updates = updates
                )

            } catch (e: Exception) {
                _uiState.value = UpdatesUiState(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}