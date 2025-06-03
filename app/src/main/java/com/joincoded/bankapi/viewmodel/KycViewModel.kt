package com.joincoded.bankapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.request.KYCRequest
import com.joincoded.bankapi.data.response.KYCResponse
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.network.KycApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class KycViewModel : ViewModel() {
    private val _kycData = MutableStateFlow<KYCResponse?>(null)
    val kycData: StateFlow<KYCResponse?> get() = _kycData

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> get() = _errorMessage

    private val kycApiService = RetrofitHelper.getInstance().create(KycApiService::class.java)

    fun fetchKYC(token: String) {
        viewModelScope.launch {
            try {
                val response = kycApiService.getMyKYC("Bearer $token")
                if (response.isSuccessful) {
                    _kycData.value = response.body()
                } else {
                    _errorMessage.value = "Failed to load KYC"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun updateKYC(token: String, firstName: String, lastName: String, country: String, phone: String, address: String, salary: Double) {
        viewModelScope.launch {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val kycRequest = KYCRequest(
                    firstName = firstName,
                    lastName = lastName,
                    country = country,
                    phoneNumber = phone,
                    homeAddress = address,
                    salary = salary.toBigDecimal(),
                    civilId = _kycData.value?.civilId ?: "",
                    dateOfBirth = _kycData.value?.dateOfBirth?.let { LocalDate.parse(it, formatter) } ?: LocalDate.now()
                )
                val response = kycApiService.addOrUpdateMyKYC("Bearer $token", kycRequest)
                if (response.isSuccessful) {
                    fetchKYC(token)
                    _errorMessage.value = "KYC updated successfully."
                } else {
                    _errorMessage.value = "Failed to update KYC"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating KYC: ${e.localizedMessage}"
            }
        }
    }


    fun logout() {
        _kycData.value = null
        _errorMessage.value = ""
    }
}
