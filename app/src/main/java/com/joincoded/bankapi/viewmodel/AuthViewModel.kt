package com.joincoded.bankapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.User
import com.joincoded.bankapi.data.request.AuthenticationRequest
import com.joincoded.bankapi.data.request.CreateUserDTO
import com.joincoded.bankapi.data.request.KYCRequest
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.network.AuthenticationApiService
import com.joincoded.bankapi.network.KycApiService
import com.joincoded.bankapi.network.UserApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authService =
        RetrofitHelper.getInstance().create(AuthenticationApiService::class.java)
    private val bankService = RetrofitHelper.getInstance()
        .create(com.joincoded.bankapi.network.BankApiService::class.java)
    private val userApiService = RetrofitHelper.getInstance().create(UserApiService::class.java)
    private val kycApi = RetrofitHelper.getInstance().create(KycApiService::class.java)

    private val _authMessage = MutableStateFlow<String>("")
    val authMessage: StateFlow<String> get() = _authMessage

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> get() = _token

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = authService.login(AuthenticationRequest(username, password))
                if (response.isSuccessful) {
                    _token.value = response.body()?.token
                    _authMessage.value = "Login successful!"
                    _isLoggedIn.value = true
                } else {
                    _authMessage.value = "Login failed: ${response.message()}"
                    _isLoggedIn.value = false
                }
            } catch (e: Exception) {
                _authMessage.value = "Login error: ${e.localizedMessage}"
                _isLoggedIn.value = false
            }
        }
    }

    fun registerWithKyc(userData: CreateUserDTO, kyc: KYCRequest) {
        viewModelScope.launch {
            try {
                // First register
                val registerResponse = userApiService.registerUser(userData)
                if (!registerResponse.isSuccessful) {
                    _authMessage.value = "Registration failed: ${registerResponse.message()}"
                    return@launch
                }

                // Then login to get token
                val loginResponse = authService.login(
                    AuthenticationRequest(userData.username, userData.password)
                )
                val token = loginResponse.body()?.token

                if (!loginResponse.isSuccessful || token.isNullOrEmpty()) {
                    _authMessage.value = "Login failed after registration."
                    return@launch
                }

                _token.value = token

                // Then submit KYC with token
                val kycResponse = kycApi.addOrUpdateMyKYC("Bearer $token", kyc)
                if (kycResponse.isSuccessful) {
                    _authMessage.value = "Registration and KYC completed successfully!"
                } else {
                    _authMessage.value = "KYC failed: ${kycResponse.message()}"
                }

            } catch (e: Exception) {
                _authMessage.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun setAuthMessage(message: String) {
        _authMessage.value = message
    }


}
