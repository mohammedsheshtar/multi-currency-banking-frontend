package com.joincoded.bankapi.ViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.TransactionItem
import com.joincoded.bankapi.data.request.AuthenticationRequest
import com.joincoded.bankapi.data.response.ListAccountResponse
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import com.joincoded.bankapi.network.AccountApiService
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.network.TransactionApiService
import com.joincoded.bankapi.utils.Constants
import com.joincoded.bankapi.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.log

class WalletViewModel : ViewModel() {
    private val accountApiService = RetrofitHelper.AccountApi
    private val transactionApiService = RetrofitHelper.TransactionApi
    private var context: Context? = null


    private val _cards = MutableStateFlow<List<CardState>>(emptyList())
    val cards: StateFlow<List<CardState>> get() = _cards

    private val _selectedCard = MutableStateFlow<CardState?>(null)
    val selectedCard: StateFlow<CardState?> get() = _selectedCard

    private val _transactions = MutableStateFlow<List<TransactionItem>>(emptyList())
    val transactions: StateFlow<List<TransactionItem>> get() = _transactions

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
    fun loginAndLoadWallet(context: Context) {
        this.context = context.applicationContext
        viewModelScope.launch {
            try {
                val response = RetrofitHelper.AuthenticationApi.login(
                    AuthenticationRequest("bbb77", "Bb@12345")
                )
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (!token.isNullOrBlank()) {
                        TokenManager.saveToken(context, token)
                        Log.d("WalletViewModel", "Token saved successfully: ${token.take(20)}...")
                        fetchUserCards() // Now we call fetch after token is stored
                    } else {
                        _error.value = "Token is null or empty"
                        Log.e("WalletViewModel", "Token is null or empty from login response")
                    }
                } else {
                    _error.value = "Login failed: ${response.message()}"
                    Log.e("WalletViewModel", "Login failed: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = "Login exception: ${e.message}"
                Log.e("WalletViewModel", "Login exception", e)
            }
        }
    }

    fun fetchUserCards() {
        Log.d("WalletViewModel", "✅ fetchUserCards() called")
        val context = context ?: return
        
        viewModelScope.launch {
            try {
                val storedToken = TokenManager.getToken(context)
                Log.d("WalletViewModel", "Retrieved stored token: ${storedToken?.take(20)}...")
                
                if (storedToken.isNullOrBlank()) {
                    _error.value = "Authentication required - no token found"
                    Log.e("WalletViewModel", "No token found in storage")
                    return@launch
                }

                val response = accountApiService.listUserAccounts("Bearer $storedToken")
                Log.d("WalletViewModel", "Account fetch response: ${response.code()} ${response.message()}")
                
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WalletViewModel", "API error: $errorBody")
                    _error.value = "Failed to fetch accounts: ${response.code()} - $errorBody"
                    return@launch
                }

                @Suppress("UNCHECKED_CAST")
                val accounts = (response.body() as? List<ListAccountResponse>) ?: run {
                    _error.value = "Failed to parse account data"

                    return@launch
                }

                val cardStates = accounts.map { account ->
                    PaymentCard(
                        id = account.accountNumber,
                        balance = account.balance.toDouble(),
                        currency = account.symbol,
                        name = account.accountType,
                        cardNumber = account.accountNumber.takeLast(4).padStart(16, '*'),
                        expMonth = "12",
                        expYear = account.createdAt.year.toString(),
                        cvv = "***",
                        type = account.accountType,
                        background = account.countryCode
                    ).let { CardState(it) }
                }

                _cards.value = cardStates
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun selectCard(card: PaymentCard) {
        val cardState = _cards.value.find { it.card.id == card.id }
        _selectedCard.value = cardState
        fetchTransactionHistory(card.id)
    }

    private fun fetchTransactionHistory(accountId: String) {
        val context = context ?: return
        viewModelScope.launch {
            val response = transactionApiService.getTransactionHistory(null, accountId.toIntOrNull() ?: return@launch)
            if (response.isSuccessful) {
                val history = response.body() as? List<TransactionHistoryResponse> ?: return@launch
                _transactions.value = history.map {
                    TransactionItem(
                        id = UUID.randomUUID().toString(),
                        title = it.transactionType,
                        date = it.timeStamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        amount = if (it.transactionType.lowercase() == "withdraw") "-${it.amount}" else "+${it.amount}",
                        cardId = it.accountNumber
                    )
                }
            }
        }
    }

    fun clearFocusedCard() {
        _selectedCard.value = null
        _transactions.value = emptyList()
    }
}
