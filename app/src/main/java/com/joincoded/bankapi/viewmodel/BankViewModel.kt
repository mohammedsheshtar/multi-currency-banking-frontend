package com.joincoded.bankapi.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.AmountChange
import com.joincoded.bankapi.data.User
import com.joincoded.bankapi.data.response.ListAccountResponse
import com.joincoded.bankapi.data.response.TokenResponse
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import com.joincoded.bankapi.network.BankApiService
import com.joincoded.bankapi.network.RetrofitHelper
import kotlinx.coroutines.launch

class BankViewModel : ViewModel() {
    var token: String? by mutableStateOf(null)
    var accounts by mutableStateOf<List<ListAccountResponse>>(emptyList())
        private set
    var transactions by mutableStateOf<List<TransactionHistoryResponse>>(emptyList())
        private set
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loginAndFetch()
    }

    fun loginAndFetch() {
        viewModelScope.launch {
            try {
                val response = RetrofitHelper.AuthenticationApi.login(
                    User("Zainab1=122", "1n23415MM67", "", null)
                )
                if (response.isSuccessful) {
                    token = "Bearer ${response.body()?.token}"
                    val responseAccount = RetrofitHelper.AccountApi.listUserAccounts(token)
                    if (responseAccount.isSuccessful) {
                        accounts = responseAccount.body() ?: emptyList()
                        if (accounts.isNotEmpty()) {
                            val accountId = accounts.first().accountNumber.toIntOrNull()
                            if (accountId != null) {
                                getTransactions(accountId)
                            }
                        }
                    }
                } else {
                    errorMessage = "Login failed: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Login error: ${e.message}"
            }
        }
    }

    fun getTransactions(accountId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitHelper.TransactionApi.getTransactionHistory(token, accountId)
                if (response.isSuccessful) {
                    transactions = response.body() ?: emptyList()
                } else {
                    errorMessage = "Failed to fetch transactions: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "getTransactions error: ${e.message}"
            }
        }
    }
}

//class BankViewModel : ViewModel() {
//    private val apiService = RetrofitHelper.getInstance().create(BankApiService::class.java)
//    var token: String? by mutableStateOf(null)
//
//    var accounts by mutableStateOf<List<ListAccountResponse>>(emptyList())
//        private set
//
//    var transactions by mutableStateOf<List<TransactionHistoryResponse>>(emptyList())
//        private set
//
//    var errorMessage by mutableStateOf<String?>(null)
//
//    init {
//        loginAndFetch()
//    }
//    fun loginAndFetch() {
//        viewModelScope.launch {
//            try {
//                println("🔁 Attempting login...")
//                val response = RetrofitHelper.AuthenticationApi.login(
//                    User("Zainab1=122", "1n23415MM67", "", null)
//                )
//                if (response.isSuccessful) {
//                    token = "Bearer ${response.body()?.token}"
//                    println("✅ Login successful. Token: $token")
//
//                    val responseAccount = RetrofitHelper.AccountApi.listUserAccounts(token)
//                    accounts = responseAccount.body() ?: emptyList()
//                    // getTransactions()
//                } else {
//                    errorMessage = "Login failed: ${response.message()}"
//                    println("❌ Login failed: ${response.message()}")
//                }
//            } catch (e: Exception) {
//                errorMessage = "Login error: ${e.message}"
//                println("❗ Login error: ${e.message}")
//            }
//        }
//    }
//
//    fun getAccounts() {
//        viewModelScope.launch {
//            try {
//                val response = RetrofitHelper.AccountApi.listUserAccounts(token)
//                if (response.isSuccessful) {
//                    accounts = response.body() ?: emptyList()
//                    println("📦 Accounts: $accounts")
//                } else {
//                    errorMessage = "Failed to fetch accounts: ${response.message()}"
//                }
//            } catch (e: Exception) {
//                errorMessage = "getAccounts error: ${e.message}"
//            }
//        }
//    }
//
//    fun getTransactions(accountId: Int? = null) {
//        viewModelScope.launch {
//            try {
//                val id = accountId ?: accounts.firstOrNull()?.accountNumber?.toIntOrNull() ?: return@launch
//                val response = RetrofitHelper.TransactionApi.getTransactionHistory(token, id)
//                if (response.isSuccessful) {
//                    transactions = response.body() ?: emptyList()
//                    println("📄 Transactions: $transactions")
//                } else {
//                    errorMessage = "Failed to fetch transactions: ${response.message()}"
//                }
//            } catch (e: Exception) {
//                errorMessage = "getTransactions error: ${e.message}"
//            }
//        }
//    }
//}
