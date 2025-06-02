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
class KycViewModel : ViewModel() {
    private val _kycData = MutableStateFlow<KYCResponse?>(null)
    val kycData: StateFlow<KYCResponse?> get() = _kycData

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> get() = _errorMessage

    private val kycApiService = RetrofitHelper.getInstance().create(KycApiService::class.java)

    fun fetchKYC(token: String) {
        viewModelScope.launch {
            try {
                println("DEBUG: Fetching KYC with token: $token")
                val response = kycApiService.getMyKYC("Bearer $token")
                println("DEBUG: Response code=${response.code()}")
                if (response.isSuccessful) {
                    val kyc = response.body()
                    println("DEBUG: KYC data=$kyc")
                    _kycData.value = kyc
                } else {
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    println("DEBUG: Error body=$errorMsg")
                    _errorMessage.value = "Failed to load KYC: $errorMsg"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
                println("DEBUG: Exception ${e.localizedMessage}")
            }
        }
    }
}
