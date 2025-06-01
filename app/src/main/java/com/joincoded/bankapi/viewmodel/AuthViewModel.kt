package com.joincoded.bankapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.User
import com.joincoded.bankapi.data.request.AuthenticationRequest
import com.joincoded.bankapi.data.request.CreateUserDTO
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.network.AuthenticationApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authService = RetrofitHelper.getInstance().create(AuthenticationApiService::class.java)
    private val bankService = RetrofitHelper.getInstance().create(com.joincoded.bankapi.network.BankApiService::class.java)

    private val _authMessage = MutableStateFlow<String>("")
    val authMessage: StateFlow<String> get() = _authMessage

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = authService.login(AuthenticationRequest(username, password))
                if (response.isSuccessful) {
                    _authMessage.value = "Login successful!"
                    // You can also extract the token from response.body()?.token
                } else {
                    _authMessage.value = "Login failed: ${response.message()}"
                }
            } catch (e: Exception) {
                _authMessage.value = "Login error: ${e.localizedMessage}"
            }
        }
    }

    fun register(username: String, password: String, image: String = "") {
        viewModelScope.launch {
            try {
                val response = bankService.signup(User(username, password, image, null)) // User or your DTO
                if (response.isSuccessful) {
                    _authMessage.value = "Registration successful!"
                } else {
                    _authMessage.value = "Registration failed: ${response.message()}"
                }
            } catch (e: Exception) {
                _authMessage.value = "Registration error: ${e.localizedMessage}"
            }
        }
    }

}
