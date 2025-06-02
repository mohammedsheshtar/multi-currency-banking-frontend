package com.joincoded.bankapi.ViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.TransactionItem
import com.joincoded.bankapi.data.request.AuthenticationRequest
import com.joincoded.bankapi.data.request.CreateAccountRequest
import com.joincoded.bankapi.data.request.DepositRequest
import com.joincoded.bankapi.data.request.TransferRequest
import com.joincoded.bankapi.data.request.WithdrawRequest
import com.joincoded.bankapi.data.response.ListAccountResponse
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import com.joincoded.bankapi.data.response.TransferResponse
import com.joincoded.bankapi.network.AccountApiService
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.network.TransactionApiService
import com.joincoded.bankapi.utils.Constants
import com.joincoded.bankapi.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.EOFException
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.log
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

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

    private val _isLoadingTransactions = MutableStateFlow(false)
    val isLoadingTransactions: StateFlow<Boolean> get() = _isLoadingTransactions

    private val _transactionError = MutableStateFlow<String?>(null)
    val transactionError: StateFlow<String?> get() = _transactionError

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
    fun loginAndLoadWallet(context: Context) {
        this.context = context.applicationContext
        // Clear any existing token first
        TokenManager.clearToken(context)
        Log.d("WalletViewModel", "Cleared any existing token")
        
        viewModelScope.launch {
            try {
                Log.d("WalletViewModel", "Attempting login with username: bbb77")
                val response = RetrofitHelper.AuthenticationApi.login(
                    AuthenticationRequest("bbb77", "Bb@12345")
                )
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    Log.d("WalletViewModel", "Login successful, received token: $token")
                    
                    if (!token.isNullOrBlank()) {
                        // Compare with Postman token
                        val postmanToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJiYmI3NyIsImlhdCI6MTc0ODgwODU3NiwiZXhwIjoxNzQ4ODEyMTc2fQ.fqgARYeU9Fq776-jReD9Nk495x8GGp-EZhphhhx7fQ0"
                        Log.d("WalletViewModel", "Token comparison:")
                        Log.d("WalletViewModel", "App token:    $token")
                        Log.d("WalletViewModel", "Postman token: $postmanToken")
                        Log.d("WalletViewModel", "Tokens match: ${token == postmanToken}")
                        
                        TokenManager.saveToken(context, token)
                        Log.d("WalletViewModel", "Token saved successfully: ${token.take(20)}...")
                        fetchUserCards() // Now we call fetch after token is stored
                    } else {
                        _error.value = "Token is null or empty"
                        Log.e("WalletViewModel", "Token is null or empty from login response")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = "Login failed: ${response.code()} - $errorBody"
                    Log.e("WalletViewModel", "Login failed: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                _error.value = "Login exception: ${e.message}"
                Log.e("WalletViewModel", "Login exception", e)
            }
        }
    }

    private fun getGradientForCurrency(currency: String): Brush {
        return when (currency) {
            "USD" -> Brush.verticalGradient(listOf(Color(0xFF423F4F), Color(0xFF2A282A)))
            "KWD" -> Brush.verticalGradient(listOf(Color(0xFF352F3F), Color(0xFF000000)))
            "EUR" -> Brush.verticalGradient(listOf(Color(0xFF1D1B2C), Color(0xFF7B699B)))
            "AED" -> Brush.verticalGradient(listOf(Color(0xFF070709), Color(0xFF9E85CB)))
            else -> Brush.verticalGradient(listOf(Color(0xFF423F4F), Color(0xFF2A282A)))
        }
    }

    fun fetchUserCards() {
        val context = context ?: return
        viewModelScope.launch {
            try {
                val storedToken = TokenManager.getToken(context)
                if (storedToken.isNullOrBlank()) {
                    Log.e("WalletViewModel", "❌ No token found")
                    return@launch
                }

                Log.d("WalletViewModel", "🔍 Fetching user cards with token: ${storedToken.take(10)}...")
                val response = accountApiService.listUserAccounts(storedToken)
                
                Log.d("WalletViewModel", """
                    📡 Accounts API Response:
                    - Code: ${response.code()}
                    - Message: ${response.message()}
                    - Raw Body: ${response.body()}
                    - Error Body: ${response.errorBody()?.string()}
                """.trimIndent())

                if (!response.isSuccessful) {
                    Log.e("WalletViewModel", """
                        ❌ Failed to fetch accounts:
                        - Code: ${response.code()}
                        - Message: ${response.message()}
                        - Error Body: ${response.errorBody()?.string()}
                    """.trimIndent())
                    return@launch
                }

                val accounts = response.body() as? List<ListAccountResponse> ?: emptyList()
                Log.d("WalletViewModel", "📊 Found ${accounts.size} accounts")
                
                // Log detailed account information
                accounts.forEach { account ->
                    try {
                        Log.d("WalletViewModel", """
                            📝 Account Details:
                            - Account Number: ${account.accountNumber}
                            - Account ID: ${account.id}
                            - Type: ${account.accountType}
                            - Balance: ${account.balance}
                            - Currency: ${account.symbol}
                            - Country Code: ${account.countryCode}
                            - Created At: ${account.createdAt}
                        """.trimIndent())
                    } catch (e: Exception) {
                        Log.e("WalletViewModel", """
                            ❌ Error parsing account details:
                            - Account: $account
                            - Error: ${e.message}
                            - Stack trace: ${e.stackTraceToString()}
                        """.trimIndent())
                    }
                }

                // Filter accounts with balance > 0
                val accountsWithBalance = accounts.filter { account -> 
                    try {
                        Log.d("WalletViewModel", "Checking account ${account.accountNumber} with balance ${account.balance}")
                        account.balance > BigDecimal.ZERO 
                    } catch (e: Exception) {
                        Log.e("WalletViewModel", """
                            ❌ Error checking account balance:
                            - Account: $account
                            - Error: ${e.message}
                        """.trimIndent())
                        false
                    }
                }
                Log.d("WalletViewModel", "💰 Found ${accountsWithBalance.size} accounts with balance > 0")

                if (accountsWithBalance.isEmpty()) {
                    Log.d("WalletViewModel", "⚠️ No accounts with balance found")
                    _cards.value = emptyList()
                    return@launch
                }

                // Transform accounts to cards
                val newCards = accountsWithBalance.mapNotNull { account ->
                    try {
                        if (account.id == null) {
                            Log.e("WalletViewModel", "❌ Account ID is null for account: ${account.accountNumber}")
                            return@mapNotNull null
                        }
                        
                        val card = PaymentCard(
                            accountId = account.id ?: 0L,
                            accountNumber = account.accountNumber,
                            balance = account.balance.toDouble(),
                            currency = account.symbol,
                            name = account.accountType,
                            cardNumber = account.accountNumber.takeLast(4).padStart(16, '*'),
                            expMonth = java.time.LocalDate.now().monthValue.toString().padStart(2, '0'),
                            expYear = (java.time.LocalDate.now().year + 5).toString(),
                            cvv = "***",
                            type = account.accountType,
                            background = when(account.countryCode) {
                                "USD" -> "usd"
                                "KWD" -> "kwd"
                                "EUR" -> "eur"
                                "AED" -> "aed"
                                else -> "default"
                            }
                        )
                        Log.d("WalletViewModel", """
                            ✅ Created card:
                            - Account ID: ${account.id}
                            - Account Number: ${account.accountNumber}
                            - Type: ${account.accountType}
                            - Currency: ${account.symbol}
                        """.trimIndent())
                        CardState(card = card)
                    } catch (e: Exception) {
                        Log.e("WalletViewModel", """
                            ❌ Error creating card for account:
                            - Account: $account
                            - Error: ${e.message}
                            - Stack trace: ${e.stackTraceToString()}
                        """.trimIndent())
                        null
                    }
                }

                Log.d("WalletViewModel", "✅ Created ${newCards.size} cards")
                _cards.value = newCards

            } catch (e: Exception) {
                Log.e("WalletViewModel", """
                    ❌ Error fetching cards:
                    - Error: ${e.message}
                    - Stack trace: ${e.stackTraceToString()}
                """.trimIndent())
            }
        }
    }

    fun selectCard(card: PaymentCard) {
        Log.d("WalletViewModel", """
            🔍 selectCard called:
            - Account Number: ${card.accountNumber}
            - Card Type: ${card.type}
            - Current cards: ${_cards.value.map { "${it.card.accountNumber} (${it.card.accountNumber})" }}
        """.trimIndent())
        
        val cardState = _cards.value.find { it.card.accountNumber == card.accountNumber }
        if (cardState == null) {
            Log.e("WalletViewModel", "❌ Card not found in current cards list!")
            return
        }
        
        Log.d("WalletViewModel", "✅ Found matching card state")
        _selectedCard.value = cardState
        fetchTransactionHistory(card.accountNumber)
    }

    fun fetchTransactionHistory(accountNumber: String, forceRefresh: Boolean = false) {
        val context = context ?: return
        viewModelScope.launch {
            try {
                _isLoadingTransactions.value = true
                _transactionError.value = null
                
                Log.d("WalletViewModel", """
                    🔄 fetchTransactionHistory called:
                    - Account Number: $accountNumber
                    - Force Refresh: $forceRefresh
                    - Current selected card: ${_selectedCard.value?.card?.accountNumber}
                    - Current cards: ${_cards.value.map { "Number: ${it.card.accountNumber}" }}
                """.trimIndent())
                
                val storedToken = TokenManager.getToken(context)
                if (storedToken.isNullOrBlank()) {
                    Log.e("WalletViewModel", "❌ No token found in storage")
                    _transactionError.value = "Authentication required - no token found"
                    return@launch
                }

                // Get the current cards to verify the account exists and get its ID
                val currentCards = _cards.value
                val card = currentCards.find { it.card.accountNumber == accountNumber }
                if (card == null) {
                    Log.e("WalletViewModel", """
                        ❌ Account not found in current cards:
                        - Looking for: $accountNumber
                        - Available cards: ${currentCards.map { "Number: ${it.card.accountNumber}" }}
                    """.trimIndent())
                    _transactionError.value = "Account not found in current cards"
                    return@launch
                }

                // Use the account number for the API call
                Log.d("WalletViewModel", """
                    ✅ Account found in current cards:
                    - Account Number: $accountNumber
                    $card
                    - Making API request to: /api/v1/accounts/transactions/$accountNumber
                """.trimIndent())
                
                val response = transactionApiService.getTransactionHistory(storedToken, card.card.accountId.toString())

                Log.d("WalletViewModel", """
                    📡 API Response:
                    - Code: ${response.code()}
                    - Message: ${response.message()}
                    - Raw Body: ${response.body()}
                    - Error Body: ${response.errorBody()?.string()}
                """.trimIndent())
                
                if (response.isSuccessful) {
                    val rawBody = response.body()
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
                        TransactionHistoryResponse::class.java
                    ).type
                    
                    val history = try {
                        RetrofitHelper.gson.fromJson<List<TransactionHistoryResponse>>(jsonString, type) ?: emptyList()
                    } catch (e: Exception) {
                        Log.e("WalletViewModel", """
                            ❌ Error parsing transaction history:
                            - Raw body: $rawBody
                            - Converted JSON: $jsonString
                            - Error: ${e.message}
                            - Stack trace: ${e.stackTraceToString()}
                        """.trimIndent())
                        emptyList()
                    }

                    Log.d("WalletViewModel", """
                        📊 Processing transactions:
                        - Raw transactions: ${history.size}
                        - First transaction: ${history.firstOrNull()}
                        - Last transaction: ${history.lastOrNull()}
                    """.trimIndent())

                    _transactions.value = history.map {
                        TransactionItem(
                            id = UUID.randomUUID().toString(),
                            title = it.transactionType,
                            date = it.timeStamp,
                            amount = if (it.transactionType.lowercase() == "withdraw") "-${it.amount}" else "+${it.amount}",
                            cardId = it.accountNumber
                        )
                    }.filter { it.cardId == accountNumber }

                    Log.d("WalletViewModel", """
                        ✅ Transaction processing complete:
                        - Filtered transactions: ${_transactions.value.size}
                        - First transaction: ${_transactions.value.firstOrNull()}
                        - Last transaction: ${_transactions.value.lastOrNull()}
                    """.trimIndent())
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WalletViewModel", """
                        ❌ Failed to fetch transactions:
                        - Account Number: $accountNumber
                        - Error Code: ${response.code()}
                        - Error Body: $errorBody
                        - Current cards: ${currentCards.map { "Number: ${it.card.accountNumber}" }}
                    """.trimIndent())

                    // If we get "Account not found" error, refresh the cards list
                    if (errorBody?.contains("Account not found") == true) {
                        Log.d("WalletViewModel", """
                            🔄 Account not found in backend:
                            - Account Number: $accountNumber
                            - Refreshing cards list to get updated account information
                        """.trimIndent())
                        fetchUserCards() // Refresh the cards list
                        _transactionError.value = "Account not found - refreshing account list"
                        
                        // If the account is no longer in the refreshed list, clear the selected card
                        if (!_cards.value.any { it.card.accountNumber == accountNumber }) {
                            Log.d("WalletViewModel", "Account no longer exists, clearing selected card")
                            clearFocusedCard()
                        }
                    } else {
                        _transactionError.value = "Failed to fetch transactions: ${response.code()} - $errorBody"
                    }
                }
            } catch (e: Exception) {
                Log.e("WalletViewModel", """
                    ❌ Error fetching transactions:
                    - Account: $accountNumber
                    - Error: ${e.message}
                    - Stack trace: ${e.stackTraceToString()}
                """.trimIndent())
                _transactionError.value = "Error fetching transactions: ${e.message}"
            } finally {
                _isLoadingTransactions.value = false
            }
        }
    }

    fun clearFocusedCard() {
        _selectedCard.value = null
        _transactions.value = emptyList()
    }

    private fun getCurrencyCode(symbol: String): String {
        return when (symbol) {
            "$" -> "USD"
            "د.ك" -> "KWD"
            "€" -> "EUR"
            "د.إ" -> "AED"
            else -> symbol  // Fallback to the symbol if not recognized
        }
    }

    private fun refreshTransactionsAndCards() {
        viewModelScope.launch {
            fetchUserCards() // This will update card balances
            _selectedCard.value?.let { cardState ->
                fetchTransactionHistory(cardState.card.accountNumber, true)
            }
        }
    }

    fun transfer(
        fromCard: PaymentCard,
        toCard: PaymentCard,
        amount: String,
        currency: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val context = context ?: return
        viewModelScope.launch {
            try {
                val storedToken = TokenManager.getToken(context)
                if (storedToken.isNullOrBlank()) {
                    onError("Authentication required - no token found")
                    return@launch
                }

                // Log transfer details including currency conversion info
                Log.d("WalletViewModel", """
                    🔄 Initiating transfer:
                    - From Account: ${fromCard.accountNumber} (${fromCard.currency})
                    - To Account: ${toCard.accountNumber} (${toCard.currency})
                    - Amount: $amount $currency
                    - Will Convert: ${fromCard.currency != toCard.currency}
                """.trimIndent())

                val transferRequest = TransferRequest(
                    sourceAccount = fromCard.accountNumber,
                    destinationAccount = toCard.accountNumber,
                    amount = amount,
                    countryCode = getCurrencyCode(currency)  // Send source currency code
                )

                val response = transactionApiService.transferAccounts(storedToken, transferRequest)
                
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WalletViewModel", """
                        ❌ Transfer failed:
                        - Code: ${response.code()}
                        - Error: $errorBody
                        - From: ${fromCard.currency}
                        - To: ${toCard.currency}
                        - Amount: $amount
                    """.trimIndent())
                    onError("Transfer failed: ${response.code()} - $errorBody")
                    return@launch
                }

                // Log successful transfer with conversion details
                val transferResponse = response.body() as? TransferResponse
                Log.d("WalletViewModel", """
                    ✅ Transfer successful:
                    - From: ${fromCard.currency}
                    - To: ${toCard.currency}
                    - Amount: $amount
                    - Was Converted: ${transferResponse?.isSourceConverted ?: false}
                    - Source Amount Withdrawn: ${transferResponse?.sourceAmountWithdrawn}
                    - Transfer Fee: ${transferResponse?.transferFee}
                    - New Balance: ${transferResponse?.sourceNewBalance}
                """.trimIndent())

                refreshTransactionsAndCards()
                onSuccess()
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Transfer failed with exception", e)
                onError("Transfer failed: ${e.message}")
            }
        }
    }

    fun deposit(
        card: PaymentCard,
        amount: String,
        currency: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val context = context ?: return
        viewModelScope.launch {
            try {
                val storedToken = TokenManager.getToken(context)
                if (storedToken.isNullOrBlank()) {
                    onError("Authentication required - no token found")
                    return@launch
                }

                val depositRequest = DepositRequest(
                    accountNumber = card.accountNumber,
                    countryCode = getCurrencyCode(currency),
                    amount = BigDecimal(amount)
                )

                val response = transactionApiService.depositAccount(storedToken, depositRequest)
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    onError("Deposit failed: ${response.code()} - $errorBody")
                    return@launch
                }

                refreshTransactionsAndCards()
                onSuccess()
            } catch (e: Exception) {
                onError("Deposit failed: ${e.message}")
            }
        }
    }

    fun withdraw(
        card: PaymentCard,
        amount: String,
        currency: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val context = context ?: return
        viewModelScope.launch {
            try {
                val storedToken = TokenManager.getToken(context)
                if (storedToken.isNullOrBlank()) {
                    onError("Authentication required - no token found")
                    return@launch
                }

                val withdrawRequest = WithdrawRequest(
                    accountNumber = card.accountNumber,
                    countryCode = getCurrencyCode(currency),
                    amount = BigDecimal(amount)
                )

                val response = transactionApiService.withdrawAccount(storedToken, withdrawRequest)
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    onError("Withdrawal failed: ${response.code()} - $errorBody")
                    return@launch
                }

                refreshTransactionsAndCards()
                onSuccess()
            } catch (e: Exception) {
                onError("Withdrawal failed: ${e.message}")
            }
        }
    }

    fun closeAccount(accountNumber: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val context = context ?: return
        viewModelScope.launch {
            try {
                val storedToken = TokenManager.getToken(context)
                if (storedToken.isNullOrBlank()) {
                    onError("Authentication required - no token found")
                    return@launch
                }

                Log.d("WalletViewModel", "🔒 Attempting to close account: $accountNumber")
                val response = accountApiService.closeAccount(storedToken, accountNumber)
                
                if (response.isSuccessful) {
                    // Treat both empty response and successful response as success
                    Log.d("WalletViewModel", "✅ Account closed successfully: $accountNumber")
                    onSuccess()
                    // Refresh the cards list after successful closure
                    fetchUserCards()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WalletViewModel", "❌ Account closure failed: $errorBody")
                    onError("Failed to close account: ${errorBody ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                // Only treat as error if it's not a successful empty response
                if (e is EOFException && e.message?.contains("End of input") == true) {
                    Log.d("WalletViewModel", "✅ Account closed successfully (empty response): $accountNumber")
                    onSuccess()
                    // Refresh the cards list after successful closure
                    fetchUserCards()
                } else {
                    Log.e("WalletViewModel", "Account closure failed with exception", e)
                    onError("Failed to close account: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }

    fun createAccount(
        initialBalance: String,
        countryCode: String,
        accountType: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val context = context ?: return
        viewModelScope.launch {
            try {
                val storedToken = TokenManager.getToken(context)
                if (storedToken.isNullOrBlank()) {
                    onError("Authentication required - no token found")
                    return@launch
                }

                Log.d("WalletViewModel", """
                    🔄 Creating new account:
                    - Initial Balance: $initialBalance
                    - Country Code: $countryCode
                    - Account Type: $accountType
                """.trimIndent())

                val request = CreateAccountRequest(
                    initialBalance = initialBalance.toDouble(),
                    countryCode = countryCode,
                    accountType = accountType
                )

                val response = accountApiService.createAccount(storedToken, request)
                
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WalletViewModel", """
                        ❌ Account creation failed:
                        - Code: ${response.code()}
                        - Error: $errorBody
                    """.trimIndent())
                    onError("Account creation failed: ${response.code()} - $errorBody")
                    return@launch
                }

                Log.d("WalletViewModel", """
                    ✅ Account created successfully:
                    - Initial Balance: $initialBalance
                    - Country Code: $countryCode
                    - Account Type: $accountType
                """.trimIndent())

                // Refresh the cards list to show the new account
                fetchUserCards()
                onSuccess()
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Account creation failed with exception", e)
                onError("Account creation failed: ${e.message}")
            }
        }
    }
}
