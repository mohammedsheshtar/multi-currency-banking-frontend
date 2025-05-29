package com.joincoded.bankapi.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExchangeRateViewModel : ViewModel() {

    private val _fromCurrency = MutableStateFlow("USD")
    val fromCurrency: StateFlow<String> = _fromCurrency.asStateFlow()

    private val _toCurrency = MutableStateFlow("EUR")
    val toCurrency: StateFlow<String> = _toCurrency.asStateFlow()

    // Example setter functions
    fun setFromCurrency(newCurrency: String) {
        _fromCurrency.value = newCurrency
    }

    fun setToCurrency(newCurrency: String) {
        _toCurrency.value = newCurrency
    }

    // Optional: expose mock list of conversion rates
    val mockRates = listOf(
        Triple("USD", "EUR", "0.92"),
        Triple("USD", "GBP", "0.78"),
        Triple("EUR", "JPY", "165.22"),
        Triple("GBP", "CAD", "1.71"),
        Triple("USD", "CAD", "1.36"),
        Triple("JPY", "CNY", "0.047")
    )
}
