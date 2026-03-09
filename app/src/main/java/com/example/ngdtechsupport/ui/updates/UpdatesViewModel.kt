package com.example.ngdtechsupport.ui.updates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngdtechsupport.model.UpdateModel
import com.example.ngdtechsupport.data.UpdatesRepository
import kotlinx.coroutines.launch

class UpdatesViewModel : ViewModel() {

    private val repository = UpdatesRepository()

    private val _updates = MutableLiveData<List<UpdateModel>>()
    val updates: LiveData<List<UpdateModel>> = _updates

    fun listenUpdates(
        companyId: String,
        businessId: String
    ) {

        repository.listenUpdates(companyId, businessId) {

            _updates.value = it

        }
    }

    fun createUpdate(
        companyId: String,
        businessId: String,
        title: String,
        description: String,
        version: String
    ) {

        viewModelScope.launch {

            repository.createUpdate(
                companyId,
                businessId,
                title,
                description,
                version
            )

        }
    }
}