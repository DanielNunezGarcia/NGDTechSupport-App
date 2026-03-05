package com.example.ngdtechsupport.ui.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ngdtechsupport.data.CompanyRepository
import com.example.ngdtechsupport.model.BusinessModel
import kotlinx.coroutines.launch

class AppDetailViewModel : ViewModel() {

    private val repository = CompanyRepository()

    val business = MutableLiveData<BusinessModel>()

    fun loadBusiness(companyId: String, businessId: String) {

        viewModelScope.launch {

            val result = repository.getBusiness(companyId, businessId)

            result?.let {
                business.postValue(it)
            }
        }
    }
}