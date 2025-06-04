package com.joincoded.bankapi.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.request.AuthenticationRequest
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel : ViewModel() {

    private val _token = MutableStateFlow<String?>(null)
    val token = _token.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitHelper.AuthenticationApi
                    .login(AuthenticationRequest(username, password))

                if (response.isSuccessful) {
                    val tokenValue = response.body()?.token
                    _token.value = tokenValue
                    _errorMessage.value = null
                    _isLoggedIn.value = true

                    // Save token using TokenManager
                    appContext?.let { TokenManager.saveToken(it, tokenValue) }
                } else {
                    _errorMessage.value = "Invalid credentials"
                    _isLoggedIn.value = false
                }
            } catch (e: HttpException) {
                _errorMessage.value = "Server error: ${e.code()}"
                _isLoggedIn.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                _isLoggedIn.value = false
            }
        }
    }

    fun logout() {
        _token.value = null
        _isLoggedIn.value = false
        appContext?.let { TokenManager.clearToken(it) }
    }
}
