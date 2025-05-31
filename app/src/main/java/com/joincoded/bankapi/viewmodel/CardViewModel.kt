package com.joincoded.bankapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.Transaction
import com.joincoded.bankapi.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CardViewModel(
    private val cardRepository: CardRepository
) : ViewModel() {
    private val _cards = MutableStateFlow<List<PaymentCard>>(emptyList())
    val cards: StateFlow<List<PaymentCard>> = _cards.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _cards.value = cardRepository.getCards()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load cards"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTransactions(cardId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _transactions.value = cardRepository.getCardTransactions(
                    cardId = cardId,
                    startDate = java.time.LocalDate.now().minusMonths(1),
                    endDate = java.time.LocalDate.now()
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load transactions"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun closeAccount(cardId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                cardRepository.closeCard(cardId, "User requested account closure")
                onSuccess()
                loadCards() // Reload cards after closure
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to close account"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 