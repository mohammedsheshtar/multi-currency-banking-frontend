package com.joincoded.bankapi.viewmodel

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
import com.joincoded.bankapi.network.KycApiService
import com.joincoded.bankapi.utils.Constants
import com.joincoded.bankapi.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.EOFException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.UUID
import kotlin.math.log
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.joincoded.bankapi.composable.availableCardColors
import com.joincoded.bankapi.data.response.CreateAccountResponse
import com.joincoded.bankapi.utils.CardColorManager
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.joincoded.bankapi.data.AmountChange
import com.joincoded.bankapi.network.BankApiService
import com.joincoded.bankapi.network.BankRepository
import kotlinx.coroutines.flow.asStateFlow

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
                return WalletViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val accountApiService = RetrofitHelper.AccountApi
    private val transactionApiService = RetrofitHelper.TransactionApi
    private val kycApiService = RetrofitHelper.KycApi
    private var context: Context? = null
    private var cardColorManager: CardColorManager? = null
    private var _token: String? = null

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

    private val _kycData = MutableStateFlow<com.joincoded.bankapi.data.response.KYCResponse?>(null)
    val kycData: StateFlow<com.joincoded.bankapi.data.response.KYCResponse?> get() = _kycData

    private val bankRepository = BankRepository(
        accountApi = RetrofitHelper.AccountApi,
        transactionApi = RetrofitHelper.TransactionApi,
        tokenProvider = { TokenManager.getToken(getApplication<Application>().applicationContext) ?: "" }
    )

    private val _accounts = MutableStateFlow<List<ListAccountResponse>>(emptyList())
    val accounts: StateFlow<List<ListAccountResponse>> = _accounts.asStateFlow()

    init {
        context = application.applicationContext
        cardColorManager = CardColorManager.getInstance(application.applicationContext)
    }

    fun setToken(token: String) {
        Log.d("WalletViewModel", "Setting token: ${token.take(20)}...")
        _token = token
        // Save token to TokenManager
        context?.let { TokenManager.saveToken(it, token) }
        // Refresh data when token is set
        refreshData()
    }

    private fun refreshData() {
        Log.d("WalletViewModel", "Refreshing data with token: ${_token?.take(20)}...")
        viewModelScope.launch {
            try {
                // First fetch KYC data and wait for it to complete
                var kycRetryCount = 0
                val maxKycRetries = 3
                
                while (kycRetryCount < maxKycRetries) {
                    try {
                        fetchKYCData()
                        // Wait a bit to ensure KYC data is processed
                        kotlinx.coroutines.delay(500)
                        if (_kycData.value != null) {
                            Log.d("WalletViewModel", "✅ KYC data fetched successfully: ${_kycData.value?.let { "${it.firstName} ${it.lastName}" }}")
                            break
                        }
                        kycRetryCount++
                        if (kycRetryCount < maxKycRetries) {
                            Log.d("WalletViewModel", "Retrying KYC fetch (attempt ${kycRetryCount + 1}/$maxKycRetries)")
                            kotlinx.coroutines.delay(1000) // Wait before retry
                        }
                    } catch (e: Exception) {
                        Log.e("WalletViewModel", "Error fetching KYC data (attempt ${kycRetryCount + 1}): ${e.message}")
                        kycRetryCount++
                        if (kycRetryCount < maxKycRetries) {
                            kotlinx.coroutines.delay(1000) // Wait before retry
                        }
                    }
                }
                
                if (_kycData.value == null) {
                    Log.w("WalletViewModel", "⚠️ Could not fetch KYC data after $maxKycRetries attempts")
                }
                
                // Then fetch user cards
                fetchUserCards()
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Error refreshing data: ${e.message}")
                _error.value = "Error refreshing data: ${e.message}"
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

                Log.d("WalletViewModel", """
                    🔍 Fetching user cards:
                    - Token: ${storedToken.take(20)}...
                    - Current KYC data: ${_kycData.value?.let { "${it.firstName} ${it.lastName}" } ?: "null"}
                """.trimIndent())

                val response = accountApiService.listUserAccounts(storedToken)
                
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
                
                // Transform accounts to cards with KYC name
                val newCards = accounts.mapNotNull { account ->
                    try {
                        if (account.id == null) {
                            Log.e("WalletViewModel", "❌ Account ID is null for account: ${account.accountNumber}")
                            return@mapNotNull null
                        }
                        
                        val fullName = _kycData.value?.let { "${it.firstName} ${it.lastName}" }?.trim()
                        if (fullName.isNullOrBlank()) {
                            Log.w("WalletViewModel", "⚠️ No KYC name available for account ${account.accountNumber}, using account type")
                        }
                        
                        // Get the card color from local storage
                        val savedCardColor = cardColorManager?.getCardColor(account.accountNumber)
                        Log.d("WalletViewModel", """
                            🎴 Creating card for account ${account.accountNumber}:
                            - Account Type: ${account.accountType}
                            - KYC Name: $fullName
                            - Will use name: ${fullName ?: account.accountType}
                            - Saved Card Color: $savedCardColor
                            - Available Colors: ${availableCardColors.map { it.name }}
                        """.trimIndent())

                        val card = PaymentCard(
                            accountId = account.id ?: 0L,
                            accountNumber = account.accountNumber,
                            balance = account.balance.toDouble(),
                            currency = account.symbol,
                            name = fullName ?: account.accountType,
                            cardNumber = account.accountNumber.takeLast(4).padStart(16, '*'),
                            expMonth = java.time.LocalDate.now().monthValue.toString().padStart(2, '0'),
                            expYear = (java.time.LocalDate.now().year + 5).toString(),
                            cvv = "***",
                            type = account.accountType,
                            background = savedCardColor ?: "default"
                        )
                        CardState(card)
                    } catch (e: Exception) {
                        Log.e("WalletViewModel", "❌ Error creating card for account: ${account.accountNumber}", e)
                        null
                    }
                }

                _cards.value = newCards
                Log.d("WalletViewModel", "✅ Updated cards list with ${newCards.size} cards")
            } catch (e: Exception) {
                Log.e("WalletViewModel", "❌ Error fetching cards", e)
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
                        // Format the LocalDateTime to a String
                        val displayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
                        val formattedDate = it.timeStamp.format(displayFormatter).toString()
                        
                        TransactionItem(
                            id = UUID.randomUUID().toString(),
                            title = it.transactionType,
                            date = formattedDate,
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

                // Using the correct method name 'transfer' from TransactionApiService
                val response = transactionApiService.transfer(
                    token = storedToken,
                    accountNumber = fromCard.accountNumber,
                    request = transferRequest
                )
                
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

    /*
    // Commented out as these methods are not used in the app
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

                val request = AmountChange(
                    amount = amount.toBigDecimal(),
                    countryCode = getCurrencyCode(currency)
                )

                Log.d("WalletViewModel", """
                    🔄 Initiating deposit:
                    - Account: ${card.accountNumber}
                    - Amount: $amount
                    - Currency: $currency
                """.trimIndent())

                val response = RetrofitHelper.BankApi.deposit(
                    token = storedToken,
                    accountNumber = card.accountNumber,
                    amountChange = request
                )

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WalletViewModel", "❌ Deposit failed: $errorBody")
                    onError("Deposit failed: ${response.code()} - $errorBody")
                    return@launch
                }

                Log.d("WalletViewModel", "✅ Deposit successful")
                refreshTransactionsAndCards()
                onSuccess()
            } catch (e: Exception) {
                Log.e("WalletViewModel", "❌ Deposit error", e)
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

                val request = AmountChange(
                    amount = amount.toBigDecimal(),
                    countryCode = getCurrencyCode(currency)
                )

                Log.d("WalletViewModel", """
                    🔄 Initiating withdrawal:
                    - Account: ${card.accountNumber}
                    - Amount: $amount
                    - Currency: $currency
                """.trimIndent())

                val response = RetrofitHelper.BankApi.withdraw(
                    token = storedToken,
                    accountNumber = card.accountNumber,
                    amountChange = request
                )

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WalletViewModel", "❌ Withdrawal failed: $errorBody")
                    onError("Withdrawal failed: ${response.code()} - $errorBody")
                    return@launch
                }

                Log.d("WalletViewModel", "✅ Withdrawal successful")
                refreshTransactionsAndCards()
                onSuccess()
            } catch (e: Exception) {
                Log.e("WalletViewModel", "❌ Withdrawal error", e)
                onError("Withdrawal failed: ${e.message}")
            }
        }
    }
    */

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
                    // Clear the saved card color
                    cardColorManager?.clearCardColor(accountNumber)
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
                    // Clear the saved card color
                    cardColorManager?.clearCardColor(accountNumber)
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
        cardColor: String = "default",
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
                    - Card Color: $cardColor
                    - Available Colors: ${availableCardColors.map { it.name }}
                """.trimIndent())

                val request = CreateAccountRequest(
                    initialBalance = initialBalance.toDouble(),
                    countryCode = countryCode,
                    accountType = accountType,
                    cardColor = cardColor
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

                // Get the account number from the response
                val responseBody = response.body()
                if (responseBody is CreateAccountResponse) {
                    // Save the card color locally
                    cardColorManager?.saveCardColor(responseBody.accountNumber, cardColor)
                    Log.d("WalletViewModel", "Saved card color ${cardColor} for account ${responseBody.accountNumber}")
                }

                Log.d("WalletViewModel", """
                    ✅ Account created successfully:
                    - Initial Balance: $initialBalance
                    - Country Code: $countryCode
                    - Account Type: $accountType
                    - Card Color: $cardColor
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

    fun fetchKYCData() {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken(context ?: return@launch)
                if (token.isNullOrBlank()) {
                    Log.e("WalletViewModel", "❌ No token found for KYC fetch")
                    return@launch
                }

                Log.d("WalletViewModel", """
                    🔍 Fetching KYC data...
                    - Token: ${token.take(20)}...
                    - Current KYC data: ${_kycData.value?.let { "${it.firstName} ${it.lastName}" } ?: "null"}
                """.trimIndent())

                val response = kycApiService.getMyKYC(token)
                val parsedBody = response.body()
                
                Log.d("WalletViewModel", """
                    📡 KYC API Response:
                    - Code: ${response.code()}
                    - Message: ${response.message()}
                    - Is Successful: ${response.isSuccessful}
                    - Response Body: $parsedBody
                """.trimIndent())
                
                if (response.isSuccessful) {
                    if (parsedBody != null) {
                        _kycData.value = parsedBody
                        Log.d("WalletViewModel", """
                            ✅ KYC data fetched successfully:
                            - First Name: ${parsedBody.firstName}
                            - Last Name: ${parsedBody.lastName}
                            - Full Name: ${parsedBody.firstName} ${parsedBody.lastName}
                            - Date of Birth: ${parsedBody.dateOfBirth}
                            - Current KYC State: ${_kycData.value?.let { "${it.firstName} ${it.lastName}" }}
                        """.trimIndent())
                        // Refresh cards to update names
                        fetchUserCards()
                    } else {
                        Log.e("WalletViewModel", "❌ KYC response body is null")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WalletViewModel", """
                        ❌ Failed to fetch KYC data:
                        - Code: ${response.code()}
                        - Message: ${response.message()}
                        - Error Body: $errorBody
                    """.trimIndent())
                }
            } catch (e: Exception) {
                Log.e("WalletViewModel", """
                    ❌ Error fetching KYC data:
                    - Error: ${e.message}
                    - Stack trace: ${e.stackTraceToString()}
                """.trimIndent())
            }
        }
    }

    fun updateCardColor(accountNumber: String, newColor: String) {
        viewModelScope.launch {
            try {
                // Update the card color in the local storage
                cardColorManager?.saveCardColor(accountNumber, newColor)
                
                // Update the card in the local state
                _cards.value = _cards.value.map { cardState ->
                    if (cardState.card.accountNumber == accountNumber) {
                        CardState(
                            card = cardState.card.copy(
                                background = newColor
                            ),
                            isFlipped = cardState.isFlipped
                        )
                    } else {
                        cardState
                    }
                }
                
                Log.d("WalletViewModel", "Card color updated for account $accountNumber to $newColor")
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Error updating card color: ${e.message}")
            }
        }
    }

    fun loginAndLoadWallet(context: Context) {
        // ... existing code ...
    }
}
