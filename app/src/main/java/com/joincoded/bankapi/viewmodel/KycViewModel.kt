package com.joincoded.bankapi.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.request.KYCRequest
import com.joincoded.bankapi.data.response.KYCResponse
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.network.KycApiService
import com.joincoded.bankapi.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class KycUiState {
    data object Loading : KycUiState()
    data class Success(val data: KYCResponse?) : KycUiState()
    data class Error(val message: String) : KycUiState()
}

class KycViewModel : ViewModel() {
    private var context: Context? = null
    private var isInitialized = false

    private val _kycUiState = MutableStateFlow<KycUiState>(KycUiState.Loading)
    val kycUiState: StateFlow<KycUiState> = _kycUiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val api = RetrofitHelper.KycApi

    fun isInitialized(): Boolean = isInitialized && context != null

    fun initialize(context: Context) {
        this.context = context.applicationContext
        // Reset state when initializing
        _kycUiState.value = KycUiState.Loading
        _errorMessage.value = null
        _isLoading.value = false
        _isEditing.value = false
        
        // Verify token is available during initialization
        val appContext = context.applicationContext
        val storedToken = TokenManager.getToken(appContext)
        if (storedToken.isNullOrBlank()) {
            _errorMessage.value = "No authentication token available"
            _kycUiState.value = KycUiState.Error("Authentication required")
            isInitialized = false
            return
        }
        
        isInitialized = true
        Log.d("KycViewModel", "Initialized successfully with token available")
    }

    fun fetchKycData() {
        if (!isInitialized()) {
            _errorMessage.value = "KYC service not properly initialized"
            _kycUiState.value = KycUiState.Error("Service not initialized")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _kycUiState.value = KycUiState.Loading

                val appContext = context ?: throw Exception("Context not initialized")
                val storedToken = TokenManager.getToken(appContext)
                if (storedToken.isNullOrBlank()) {
                    throw Exception("No authentication token available")
                }

                Log.d("KycViewModel", "Fetching KYC data with token: ${storedToken.take(10)}...")
                val response = api.getMyKYC("Bearer $storedToken")
                
                if (response.isSuccessful) {
                    val kycData = response.body()
                    if (kycData != null) {
                        _kycUiState.value = KycUiState.Success(kycData)
                        Log.d("KycViewModel", "Successfully fetched KYC data: ${kycData.firstName} ${kycData.lastName}")
                    } else {
                        throw Exception("No KYC data received")
                    }
                } else {
                    val errorMsg = "Failed to fetch KYC data: ${response.code()}"
                    _kycUiState.value = KycUiState.Error(errorMsg)
                    _errorMessage.value = errorMsg
                    Log.e("KycViewModel", "API error: $errorMsg")
                }
            } catch (e: Exception) {
                val errorMsg = "Error fetching KYC data: ${e.message}"
                _kycUiState.value = KycUiState.Error(errorMsg)
                _errorMessage.value = errorMsg
                Log.e("KycViewModel", "Error fetching KYC data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateKYC(
        firstName: String,
        lastName: String,
        country: String,
        phoneNumber: String,
        homeAddress: String,
        salary: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val appContext = context ?: throw Exception("Context not initialized")
                val token = TokenManager.getToken(appContext) ?: throw Exception("No token found")
                val originalKycData = (kycUiState as? KycUiState.Success)?.data ?: throw Exception("No KYC data available")

                val dateOfBirth = originalKycData.dateOfBirth

                val kycRequest = KYCRequest(
                    firstName = firstName,
                    lastName = lastName,
                    dateOfBirth = dateOfBirth.toString(),
                    civilId = originalKycData.civilId,
                    country = country,
                    phoneNumber = phoneNumber,
                    homeAddress = homeAddress,
                    salary = salary.toBigDecimal()
                )

                val response = api.addOrUpdateMyKYC("Bearer $token", kycRequest)
                if (response.isSuccessful) {
                    _kycUiState.value = KycUiState.Success(response.body() as? KYCResponse ?: originalKycData)
                    _errorMessage.value = null
                    _isEditing.value = false
                } else {
                    _errorMessage.value = "Error updating KYC: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating KYC: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleEditing() {
        _isEditing.value = !_isEditing.value
        if (!_isEditing.value) {
            // If we're turning off editing mode, refresh the data
            fetchKycData()
        }
    }

    fun logout() {
        _kycUiState.value = KycUiState.Loading
        _errorMessage.value = null
        _isEditing.value = false
        _isLoading.value = false
    }
}
