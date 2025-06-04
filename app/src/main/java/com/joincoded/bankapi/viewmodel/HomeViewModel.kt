package com.joincoded.bankapi.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.User
import com.joincoded.bankapi.data.request.AuthenticationRequest
import com.joincoded.bankapi.data.response.ListAccountResponse
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import com.joincoded.bankapi.network.RetrofitHelper
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val apiService = RetrofitHelper.getInstance().create(com.joincoded.bankapi.network.BankApiService::class.java)

    var token: String? by mutableStateOf(null)
    var userName: String? by mutableStateOf(null)

    var accounts by mutableStateOf<List<ListAccountResponse>>(emptyList())
        private set

    var transactions = mutableStateListOf<TransactionHistoryResponse>()

    var errorMessage by mutableStateOf<String?>(null)

    init {
        loginAndFetch()
    }

    fun loginAndFetch() {
        viewModelScope.launch {
            try {
                println("🔁 Attempting login...")
                val response = RetrofitHelper.AuthenticationApi.login(
                    AuthenticationRequest("JAWADa1123", "12345assdFG")
                )
                if (response.isSuccessful) {
                    token = "Bearer ${response.body()?.token}"
                    println("✅ Login successful. Token: $token")

                    val kycResponse = RetrofitHelper.KycApi.getMyKYC(token)
                    if (kycResponse.isSuccessful) {
                        userName = kycResponse.body()?.firstName
                        println("🙋 First Name: $userName")
                    }

                    val responseAccount = RetrofitHelper.AccountApi.listUserAccounts(token)
                    accounts = responseAccount.body() ?: emptyList()
                    println("📦 Accounts: $accounts")

                    accounts.firstOrNull()?.let { account ->
                        getTransactions(account.id)
                    }
                } else {
                    errorMessage = "Login failed: ${response.message()}"
                    println("❌ Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                errorMessage = "Login error: ${e.message}"
                println("❗ Login error: ${e.message}")
            }
        }
    }

    fun getAccounts() {
        viewModelScope.launch {
            try {
                val response = RetrofitHelper.AccountApi.listUserAccounts(token)
                if (response.isSuccessful) {
                    accounts = response.body() ?: emptyList()
                    errorMessage = null
                    println("📦 Accounts: $accounts")
                } else {
                    errorMessage = "Failed to fetch accounts: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "getAccounts error: ${e.message}"
            }
        }
    }

    fun getAllTransactionsForUser() {
        viewModelScope.launch {
            try {
                val response = RetrofitHelper.TransactionApi.getAllTransactionHistory(token)
                if (response.isSuccessful) {
                    transactions.clear()
                    response.body()?.let {
                        transactions.addAll(it.sortedByDescending { txn -> txn.timeStamp })
                    }
                    println("📄 All Transactions from new endpoint: $transactions")
                } else {
                    println("❌ Failed to fetch all transactions: ${response.message()}")
                }
            } catch (e: Exception) {
                println("❗ getAllTransactionsForUser error: ${e.message}")
            }
        }
    }


    fun getTransactions(accountId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitHelper.TransactionApi.getTransactionHistory(token, accountId)
                if (response.isSuccessful) {
                    transactions.clear()
                    response.body()?.let { transactions.addAll(it) }
                    println("📄 Transactions: $transactions")
                }
            } catch (e: Exception) {
                println("❗ Exception: ${e.message}")
            }
        }
    }
}