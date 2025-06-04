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

class KycViewModel : ViewModel() {
    private val _kycData = MutableStateFlow<KYCResponse?>(null)
    val kycData: StateFlow<KYCResponse?> get() = _kycData

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> get() = _errorMessage

    private val api = RetrofitHelper.getInstance().create(KycApiService::class.java)

    fun fetchKYC(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getMyKYC("Bearer $token")
                if (response.isSuccessful) {
                    _kycData.value = response.body()
                } else {
                    _errorMessage.value = "Error fetching KYC: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching KYC: ${e.localizedMessage}"
            }
        }
    }

    fun updateKYC(
        token: String,
        firstName: String,
        lastName: String,
        country: String,
        phoneNumber: String,
        homeAddress: String,
        salary: Double
    ) {
        viewModelScope.launch {
            val originalKycData = _kycData.value
            println("DEBUG: Original KYC Data = $originalKycData")
            println("DEBUG: Original Date of Birth = ${originalKycData?.dateOfBirth}")

            if (originalKycData != null) {
                // ✅ Convert dateOfBirth from String to LocalDate
                val dateOfBirth = try {
                    LocalDate.parse(originalKycData.dateOfBirth)
                } catch (e: Exception) {
                    LocalDate.of(2000, 1, 1)  // Fallback date
                }

                val kycRequest = KYCRequest(
                    firstName = firstName,
                    lastName = lastName,
                    dateOfBirth = dateOfBirth.toString(),  // Valid LocalDate
                    civilId = originalKycData.civilId,
                    country = country,
                    phoneNumber = phoneNumber,
                    homeAddress = homeAddress,
                    salary = salary.toBigDecimal()
                )

                try {
                    val response = api.addOrUpdateMyKYC("Bearer $token", kycRequest)
                    if (response.isSuccessful) {
                        _kycData.value = response.body() ?: originalKycData
                    } else {
                        _errorMessage.value = "Error updating KYC: ${response.message()}"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error updating KYC: ${e.localizedMessage}"
                }
            } else {
                _errorMessage.value = "No original KYC data available."
            }
        }
    }


    fun logout() {
        _kycData.value = null
        _errorMessage.value = ""
    }
}
