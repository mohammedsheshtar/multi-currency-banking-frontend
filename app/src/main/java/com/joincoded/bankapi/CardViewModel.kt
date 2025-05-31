package com.joincoded.bankapi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CardViewModel(private val repository: CardRepository) : ViewModel() {

    private val _cards = MutableStateFlow<List<PaymentCard>>(emptyList())
    val cards: StateFlow<List<PaymentCard>> = _cards

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _transferResult = MutableStateFlow<Result<TransferResponse>>(Result.success(TransferResponse(true, "Transfer successful", "")))
    val transferResult: StateFlow<Result<TransferResponse>> = _transferResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.listUserAccounts()
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { cards ->
                    _cards.value = cards
                    _isLoading.value = false
                }
        }
    }

    fun loadTransactions(accountId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTransactionHistory(accountId)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { transactions ->
                    _transactions.value = transactions
                    _isLoading.value = false
                }
        }
    }

    fun closeAccount(accountId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.closeAccount(accountId)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { response ->
                    if (response.success) {
                        onSuccess()
                    } else {
                        _error.value = response.message
                    }
                    _isLoading.value = false
                }
        }
    }

    fun transferMoney(transferRequest: TransferRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.transferMoney(transferRequest)
                .catch { e ->
                    _transferResult.value = Result.failure(e)
                    _isLoading.value = false
                }
                .collect { response ->
                    _transferResult.value = Result.success(response)
                    _isLoading.value = false
                }
        }
    }
} 