package com.joincoded.bankapi.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.TransactionItem
import com.joincoded.bankapi.data.response.ListAccountResponse
import com.joincoded.bankapi.network.AccountApiService
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.network.TransactionApiService
import com.joincoded.bankapi.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class HomeViewModel(
    private val authViewModel: AuthViewModel,
    private val context: Context
) : ViewModel() {
    class Factory(
        private val authViewModel: AuthViewModel,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(authViewModel, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val accountApiService: AccountApiService = RetrofitHelper.AccountApi
    private val transactionApiService: TransactionApiService = RetrofitHelper.TransactionApi

    // Initialize with empty string
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _accounts = MutableStateFlow<List<ListAccountResponse>>(emptyList())
    val accounts: StateFlow<List<ListAccountResponse>> = _accounts.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionItem>>(emptyList())
    val transactions: StateFlow<List<TransactionItem>> = _transactions.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        Log.d("HomeViewModel", "Initializing HomeViewModel")
        initializeUsernameCollection()
        loadData()
    }

    private fun initializeUsernameCollection() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Starting to collect username from AuthViewModel")
                // First, get the current value
                val currentUsername: String = authViewModel.username.value
                Log.d("HomeViewModel", "Current username from AuthViewModel: $currentUsername")
                _userName.value = currentUsername

                // Then collect updates with explicit type
                authViewModel.username.collectLatest { username: String ->
                    Log.d("HomeViewModel", "Received username update: $username")
                    if (username.isNotBlank()) {
                        _userName.value = username
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error collecting username", e)
                _error.value = "Error collecting username: ${e.message}"
            }
        }
    }
    private fun loadData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val token = TokenManager.getToken(context)
                if (token.isNullOrBlank()) {
                    Log.e("HomeViewModel", "No token found")
                    _error.value = "Authentication required"
                    return@launch
                }

                Log.d("HomeViewModel", "Loading data with token: ${token.take(20)}...")

                // Fetch accounts
                val accountsResponse = accountApiService.listUserAccounts(token)
                if (!accountsResponse.isSuccessful) {
                    Log.e("HomeViewModel", "Failed to fetch accounts: ${accountsResponse.code()}")
                    _error.value = "Failed to fetch accounts: ${accountsResponse.code()}"
                    return@launch
                }

                val accountsList = accountsResponse.body() ?: emptyList()
                _accounts.value = accountsList
                Log.d("HomeViewModel", "Fetched ${accountsList.size} accounts")

                // Fetch transactions for each account
                val allTransactions = mutableListOf<TransactionItem>()
                for (account in accountsList) {
                    try {
                        val transactionsResponse = transactionApiService.getTransactionHistory(
                            token,
                            account.id.toString()
                        )

                        if (transactionsResponse.isSuccessful) {
                            val rawBody = transactionsResponse.body()
                            // Convert the non-JSON format to proper JSON using regex
                            val jsonString = rawBody.toString()
                                .replace(Regex("\\{([^}]*)\\}")) { matchResult ->
                                    val content = matchResult.groupValues[1]
                                    val formattedContent = content.split(",")
                                        .joinToString(",") { pair ->
                                            val (key, value) = pair.trim().split("=", limit = 2)
                                            val formattedValue = if (value.trim().matches(Regex("^[0-9.]+$"))) {
                                                value.trim()
                                            } else {
                                                "\"${value.trim()}\""
                                            }
                                            "\"${key.trim()}\":$formattedValue"
                                        }
                                    "{$formattedContent}"
                                }
                                .replace("\\[", "[")
                                .replace("\\]", "]")
                                .replace("\\{", "{")
                                .replace("\\}", "}")

                            val type = com.google.gson.reflect.TypeToken.getParameterized(
                                List::class.java,
                                com.joincoded.bankapi.data.response.TransactionHistoryResponse::class.java
                            ).type

                            val history = try {
                                RetrofitHelper.gson.fromJson<List<com.joincoded.bankapi.data.response.TransactionHistoryResponse>>(
                                    jsonString,
                                    type
                                ) ?: emptyList()
                            } catch (e: Exception) {
                                Log.e("HomeViewModel", "Error parsing transactions for account ${account.accountNumber}", e)
                                emptyList()
                            }

                            val accountTransactions = history.map {
                                val displayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
                                val formattedDate = it.timeStamp.format(displayFormatter).toString()

                                TransactionItem(
                                    id = UUID.randomUUID().toString(),
                                    title = it.transactionType,
                                    date = formattedDate,
                                    amount = if (it.transactionType.lowercase() == "withdraw") "-${it.amount}" else "+${it.amount}",
                                    cardId = it.accountNumber
                                )
                            }
                            allTransactions.addAll(accountTransactions)
                        } else {
                            Log.e("HomeViewModel", "Failed to fetch transactions for account ${account.accountNumber}: ${transactionsResponse.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Error fetching transactions for account ${account.accountNumber}", e)
                    }
                }

                // Sort transactions by date (most recent first)
                _transactions.value = allTransactions.sortedByDescending {
                    LocalDateTime.parse(it.date, DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))
                }

                Log.d("HomeViewModel", "Loaded ${_transactions.value.size} transactions")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading data", e)
                _error.value = "Error loading data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        Log.d("HomeViewModel", "Refreshing data")
        loadData()
    }

    fun getAccountBalance(accountNumber: String): Double {
        return try {
            _accounts.value.find { it.accountNumber == accountNumber }?.balance?.toDouble() ?: 0.0
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error getting account balance for $accountNumber", e)
            0.0
        }
    }

    fun getAccountCurrency(accountNumber: String): String {
        return try {
            _accounts.value.find { it.accountNumber == accountNumber }?.symbol ?: ""
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error getting account currency for $accountNumber", e)
            ""
        }
    }

    fun getTotalBalance(): Double {
        return try {
            _accounts.value.sumOf { it.balance.toDouble() }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error calculating total balance", e)
            0.0
        }
    }

    fun getAccountByNumber(accountNumber: String): ListAccountResponse? {
        return try {
            _accounts.value.find { it.accountNumber == accountNumber }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error finding account $accountNumber", e)
            null
        }
    }

    fun getRecentTransactions(limit: Int = 5): List<TransactionItem> {
        return try {
            _transactions.value.take(limit)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error getting recent transactions", e)
            emptyList()
        }
    }

    fun getTransactionsForAccount(accountNumber: String): List<TransactionItem> {
        return try {
            _transactions.value.filter { it.cardId == accountNumber }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error getting transactions for account $accountNumber", e)
            emptyList()
        }
    }
}