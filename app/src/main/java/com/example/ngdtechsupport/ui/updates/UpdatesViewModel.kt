package com.example.ngdtechsupport.ui.updates

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngdtechsupport.data.CompanyRepository
import com.example.ngdtechsupport.model.UpdateModel
import kotlinx.coroutines.launch

class UpdatesViewModel : ViewModel() {

    private val repository = CompanyRepository()

    val updates = MutableLiveData<List<UpdateModel>>()

    fun loadUpdates(companyId: String, businessId: String) {

        viewModelScope.launch {

            val result = repository.getUpdates(companyId, businessId)

            updates.postValue(result)
        }
    }
}