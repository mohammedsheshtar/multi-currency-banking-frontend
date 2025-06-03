package com.joincoded.bankapi.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.response.ShopTransactionResponse
import com.joincoded.bankapi.network.RetrofitHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShopHistoryViewModel : ViewModel() {
    private val api = RetrofitHelper.ShopApi

    var token: String? by mutableStateOf(null)

    private val _transactions = MutableStateFlow<List<ShopTransactionResponse>>(emptyList())
    val transactions: StateFlow<List<ShopTransactionResponse>> = _transactions.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchHistory() {
        viewModelScope.launch {
            val authToken = token
            if (authToken.isNullOrBlank()) {
                _error.value = "Missing token"
                return@launch
            }

            try {
                println("📡 Sending token: $authToken")
                val response = api.getShopTransaction(authToken)
                if (response.isSuccessful) {
                    _transactions.value = response.body() ?: emptyList()
                    _error.value = null
                } else {
                    println("❌ API failure ${response.code()} - ${response.message()}")
                    _error.value = "Error ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                println("❗ Network error: ${e.message}")
                _error.value = "Network error: ${e.localizedMessage}"
            }
        }
    }

}
