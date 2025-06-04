package com.joincoded.bankapi.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.request.AuthenticationRequest
import com.joincoded.bankapi.data.request.CreateUserDTO
import com.joincoded.bankapi.data.request.RegisterRequest
import com.joincoded.bankapi.data.response.AuthenticationResponse
import com.joincoded.bankapi.data.response.RegisterResponse
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import org.json.JSONObject
import java.util.Base64

class AuthViewModel : ViewModel() {
    private val authApiService = RetrofitHelper.AuthApi
    private val userApiService = RetrofitHelper.UserApi
    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _username = MutableStateFlow<String>("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val savedToken = TokenManager.getToken(context)
        if (savedToken != null) {
            _token.value = savedToken
            _isLoggedIn.value = true
            extractUsernameFromToken(savedToken)
        }
    }

    private fun extractUsernameFromToken(token: String) {
        try {
            val parts = token.split(".")
            if (parts.size != 3) return

            val payload = parts[1]
            val decodedPayload = Base64.getUrlDecoder().decode(payload)
            val jsonString = String(decodedPayload, Charsets.UTF_8)
            val jsonObject = JSONObject(jsonString)

            val extractedUsername = jsonObject.optString("sub", "")
            Log.d("AuthViewModel", "Extracted username from token: $extractedUsername")
            _username.value = extractedUsername
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error extracting username from token", e)
        }
    }

    fun login(
        username: String,
        password: String,
        context: Context,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                Log.d("AuthViewModel", "Attempting login for username: $username")

                val request = AuthenticationRequest(username, password)
                val response = authApiService.login(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        // Save token
                        TokenManager.saveToken(context, authResponse.token)
                        _token.value = authResponse.token
                        _isLoggedIn.value = true

                        // Set username
                        _username.value = username
                        Log.d("AuthViewModel", "Login successful, username set to: $username")

                        onSuccess()
                    } else {
                        val error = "Login failed: Empty response"
                        _errorMessage.value = error
                        onError(error)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val error = "Login failed: ${response.code()} - $errorBody"
                    _errorMessage.value = error
                    onError(error)
                }
            } catch (e: Exception) {
                val error = "Login failed: ${e.message}"
                _errorMessage.value = error
                onError(error)
            }
        }
    }

    fun register(
        username: String,
        password: String,
        firstName: String,
        lastName: String,
        dateOfBirth: String,
        context: Context,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                Log.d("AuthViewModel", "Attempting registration for username: $username")

                val request = CreateUserDTO(
                    username = username,
                    password = password
                )

                val response = userApiService.registerUser(request)

                if (response.isSuccessful) {
                    // After successful registration, attempt to login
                    login(username, password, context, onSuccess, onError)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val error = "Registration failed: ${response.code()} - $errorBody"
                    _errorMessage.value = error
                    onError(error)
                }
            } catch (e: Exception) {
                val error = "Registration failed: ${e.message}"
                _errorMessage.value = error
                onError(error)
            }
        }
    }

    fun logout(context: Context) {
        TokenManager.clearToken(context)
        _token.value = null
        _username.value = ""
        _isLoggedIn.value = false
        _errorMessage.value = null
        Log.d("AuthViewModel", "Logged out, cleared username and token")
    }

    fun setUsername(newUsername: String) {
        _username.value = newUsername
        Log.d("AuthViewModel", "Username manually set to: $newUsername")
    }
}