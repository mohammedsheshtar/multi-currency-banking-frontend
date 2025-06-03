package com.joincoded.bankapi.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.User
import com.joincoded.bankapi.data.response.ListAccountResponse
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import com.joincoded.bankapi.network.BankApiService
import com.joincoded.bankapi.network.RetrofitHelper
import kotlinx.coroutines.launch

class BankViewModel : ViewModel() {
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
                    User("Zainab3812", "1n23415MM67", "", null)
                )
                if (response.isSuccessful) {
                    token = "Bearer ${response.body()?.token}"
                    println("✅ Login successful. Token: $token")

                    // Fetch KYC info to get first name
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

    fun getAllTransactionsForUserAccounts() {
        viewModelScope.launch {
            try {
                val allTransactions = mutableListOf<TransactionHistoryResponse>()
                accounts.forEach { account ->
                    val response = RetrofitHelper.TransactionApi.getTransactionHistory(token, account.id)
                    if (response.isSuccessful) {
                        response.body()?.let {
                            allTransactions.addAll(it)
                        }
                    } else {
                        println("❌ Failed to fetch transactions for ${account.accountNumber}: ${response.message()}")
                    }
                }
                transactions.clear()
                transactions.addAll(allTransactions.sortedByDescending { it.timeStamp })
                println("📄 All Transactions: $transactions")
            } catch (e: Exception) {
                println("❗ getAllTransactions error: ${e.message}")
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