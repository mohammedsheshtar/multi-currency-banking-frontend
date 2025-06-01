package com.joincoded.bankapi.ViewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.request.AuthenticationRequest
import com.joincoded.bankapi.data.request.CreateAccount
import com.joincoded.bankapi.data.request.CreateUserDTO
import com.joincoded.bankapi.data.request.DepositRequest
import com.joincoded.bankapi.data.request.TransferRequest
import com.joincoded.bankapi.data.request.WithdrawRequest
import com.joincoded.bankapi.data.response.CreateAccountResponse
import com.joincoded.bankapi.data.response.TokenResponse
import com.joincoded.bankapi.network.RetrofitHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BankViewModel : ViewModel() {
    private val authenticationApi = RetrofitHelper.AuthenticationApi
    private val userApi = RetrofitHelper.UserApi
    private val accountApiService = RetrofitHelper.AccountApi
    private val transactionApiService = RetrofitHelper.TransactionApi
    private var context: Context? = null

    var token: TokenResponse? by mutableStateOf(null)
    private var _token = mutableStateOf<String?>(null)

    private val _createAccountResponse = MutableStateFlow<CreateAccountResponse?>(null)
    val createAccountResponse: StateFlow<CreateAccountResponse?> = _createAccountResponse

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Commenting out automatic login
    // init {
    //     login()
    // }

    // Changed to public so it can be called manually
    fun login() {
        viewModelScope.launch {
            try {
                val response = authenticationApi.login(
                    AuthenticationRequest("bbb77", "Bb@12345")
                )
                if (response.isSuccessful) {
                    _token.value = "Bearer ${response.body()?.token}"
                } else {
                    _errorMessage.value = "Login failed: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Login exception: ${e.message}"
            }
        }
    }

    fun signup(username: String, password: String, image: String = "") {
        viewModelScope.launch {
            try {
                val response = userApi.registerUser(
                    CreateUserDTO(username, password)
                )
                if (response.isSuccessful) {
                    // After successful registration, attempt to login
                    login()
                } else {
                    _errorMessage.value = "Signup failed: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Signup error: ${e.message}"
            }
        }
    }

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    fun createAccount(request: CreateAccount) {
        viewModelScope.launch {
            try {
                val response = accountApiService.createAccount(null, request)
                if (response.isSuccessful) {
                    _createAccountResponse.value = response.body() as? CreateAccountResponse
                } else {
                    _errorMessage.value = "Failed to create account: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error creating account: ${e.message}"
            }
        }
    }

    fun deposit(request: DepositRequest) {
        viewModelScope.launch {
            try {
                val response = transactionApiService.depositAccount(null, request)
                if (!response.isSuccessful) {
                    _errorMessage.value = "Failed to deposit: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error depositing: ${e.message}"
            }
        }
    }

    fun withdraw(request: WithdrawRequest) {
        viewModelScope.launch {
            try {
                val response = transactionApiService.withdrawAccount(null, request)
                if (!response.isSuccessful) {
                    _errorMessage.value = "Failed to withdraw: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error withdrawing: ${e.message}"
            }
        }
    }

    fun transfer(request: TransferRequest) {
        viewModelScope.launch {
            try {
                val response = transactionApiService.transferAccounts(null, request)
                if (!response.isSuccessful) {
                    _errorMessage.value = "Failed to transfer: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error transferring: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
//private fun loginAndFetch() {
//    viewModelScope.launch {
//        _exchangeRateUiState.value = ExchangeRateUiState.Loading
//        try {
//            val response = RetrofitHelper.AuthenticationApi.login(
//                AuthenticationRequest("Zainab3812", "1n23415MM67")
//            )
//            if (response.isSuccessful) {
//                token = "Bearer ${response.body()?.token}"
//                fetchCurrencies()
//                fetchExchangeRates()
//            } else {
//                _exchangeRateUiState.value = ExchangeRateUiState.Error("Login failed: ${response.message()}")
//            }
//        } catch (e: Exception) {
//            _exchangeRateUiState.value = ExchangeRateUiState.Error("Exception: ${e.message}")
//        }
//    }
//}