package com.joincoded.bankapi.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.TransferUser
import com.joincoded.bankapi.repository.TransferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

data class TransferState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTransferComplete: Boolean = false,
    val amountError: String? = null
)

class TransferViewModel(
    private val transferRepository: TransferRepository
) : ViewModel() {
    private val _searchedUser = MutableStateFlow<TransferUser?>(null)
    val searchedUser: StateFlow<TransferUser?> = _searchedUser.asStateFlow()

    private val _state = MutableStateFlow(TransferState())
    val state: StateFlow<TransferState> = _state.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isTransferSuccess = MutableStateFlow(false)
    val isTransferSuccess: StateFlow<Boolean> = _isTransferSuccess.asStateFlow()

    fun searchUserByPhone(phoneNumber: String) {
        if (phoneNumber.length != 10) {
            _error.value = "Please enter a valid 10-digit phone number"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val user = transferRepository.searchUserByPhone(phoneNumber)
                _searchedUser.value = user
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to search user"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun transferToUser(fromCard: PaymentCard, toUser: TransferUser, amount: BigDecimal) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                transferRepository.transferToUser(fromCard, toUser, amount)
                _isTransferSuccess.value = true
                _searchedUser.value = null // Clear the searched user after successful transfer
            } catch (e: Exception) {
                _error.value = e.message ?: "Transfer failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun transferToCard(fromCard: PaymentCard, toCard: PaymentCard, amount: BigDecimal) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                transferRepository.transferToCard(fromCard, toCard, amount)
                _isTransferSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Transfer failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyPassword(password: String): Boolean {
        // TODO: Implement password verification logic
        return true
    }

    fun clearTransferSuccess() {
        _isTransferSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSearchedUser() {
        _searchedUser.value = null
    }

    fun resetState() {
        _state.value = TransferState()
    }
} 