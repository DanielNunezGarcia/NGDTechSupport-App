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
    val error = MutableLiveData<String?>()

    fun loadBusiness(companyId: String, businessId: String) {
        error.value = null

        viewModelScope.launch {
            try {
                val result = repository.getBusiness(companyId, businessId)

                if (result != null) {
                    business.postValue(result)
                } else {
                    error.postValue("Negocio no encontrado")
                }
            } catch (e: Exception) {
                error.postValue(e.message ?: "Error al cargar el negocio")
            }
        }
    }
}
