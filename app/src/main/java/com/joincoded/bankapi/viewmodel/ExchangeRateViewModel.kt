package com.joincoded.bankapi.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.response.ConversionRateResponse
import com.joincoded.bankapi.data.response.CurrencyResponse
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.RoundingMode

sealed class ExchangeRateUiState {
    data object Loading : ExchangeRateUiState()
    data class Success(val rates: List<ConversionRateResponse>) : ExchangeRateUiState()
    data class Error(val message: String) : ExchangeRateUiState()
}

class ExchangeRateViewModel : ViewModel() {
    private var context: Context? = null

    private val _fromCurrency = MutableStateFlow("")
    val fromCurrency: StateFlow<String> = _fromCurrency.asStateFlow()

    private val _toCurrency = MutableStateFlow("")
    val toCurrency: StateFlow<String> = _toCurrency.asStateFlow()

    private val _exchangeRateUiState = MutableStateFlow<ExchangeRateUiState>(ExchangeRateUiState.Loading)
    val exchangeRateUiState = _exchangeRateUiState.asStateFlow()

    private val _currencies = MutableStateFlow<List<CurrencyResponse>>(emptyList())
    val currencies: StateFlow<List<CurrencyResponse>> = _currencies.asStateFlow()

    private val _conversionRate = MutableStateFlow<String?>("?")
    val conversionRate: StateFlow<String?> = _conversionRate.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    fun refreshData() {
        fetchCurrencies()
        fetchExchangeRates()
    }

    fun setFromCurrency(value: String) {
        _fromCurrency.value = value
        validateCurrency()
    }

    fun setToCurrency(value: String) {
        _toCurrency.value = value
        validateCurrency()
    }

    fun setAmount(value: String) {
        _amount.value = value
        validateCurrency()
    }

    private fun fetchCurrencies() {
        viewModelScope.launch {
            try {
                val storedToken = TokenManager.getToken(context ?: return@launch)
                if (storedToken.isNullOrBlank()) {
                    _exchangeRateUiState.value = ExchangeRateUiState.Error("No authentication token available")
                    return@launch
                }

                val response = RetrofitHelper.CurrenciesApi.getAllCurrencies(storedToken)
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null) {
                        _currencies.value = result
                        Log.d("ExchangeRateViewModel", "Fetched ${result.size} currencies")
                    }
                } else {
                    _exchangeRateUiState.value = ExchangeRateUiState.Error("Failed to fetch currencies: ${response.code()}")
                    Log.e("ExchangeRateViewModel", "Failed to fetch currencies: ${response.code()}")
                }
            } catch (e: Exception) {
                _exchangeRateUiState.value = ExchangeRateUiState.Error("Error fetching currencies: ${e.message}")
                Log.e("ExchangeRateViewModel", "Error fetching currencies", e)
            }
        }
    }

    private fun fetchExchangeRates() {
        viewModelScope.launch {
            try {
                val storedToken = TokenManager.getToken(context ?: return@launch)
                if (storedToken.isNullOrBlank()) {
                    _exchangeRateUiState.value = ExchangeRateUiState.Error("No authentication token available")
                    return@launch
                }

                val response = RetrofitHelper.ConversionRateApi.getAllRates(storedToken)
                if (response.isSuccessful) {
                    val rates = response.body()
                    if (rates != null) {
                        _exchangeRateUiState.value = ExchangeRateUiState.Success(rates)
                        validateCurrency()
                        Log.d("ExchangeRateViewModel", "Fetched ${rates.size} conversion rates")
                    }
                } else {
                    _exchangeRateUiState.value = ExchangeRateUiState.Error("Failed to fetch rates: ${response.code()}")
                    Log.e("ExchangeRateViewModel", "Failed to fetch rates: ${response.code()}")
                }
            } catch (e: Exception) {
                _exchangeRateUiState.value = ExchangeRateUiState.Error("Error fetching rates: ${e.message}")
                Log.e("ExchangeRateViewModel", "Error fetching rates", e)
            }
        }
    }

    private fun calculateRate() {
        val from = _fromCurrency.value.uppercase()
        val to = _toCurrency.value.uppercase()

        //trying to see if the rate is on the list to avoid calling API
        val localRates = (_exchangeRateUiState.value as? ExchangeRateUiState.Success)?.rates
        val matched = localRates?.find { it.from == from && it.to == to }

        val amountValue = _amount.value.toFloatOrNull() ?: return

        if (matched != null) {
            _conversionRate.value = (matched.rate * amountValue.toBigDecimal()).setScale(3, RoundingMode.HALF_UP).toPlainString()
            return
        }

        // fallback to backend
        viewModelScope.launch {
            try {
                val storedToken = TokenManager.getToken(context ?: return@launch)
                if (storedToken.isNullOrBlank()) {
                    _conversionRate.value = "?"
                    return@launch
                }

                val response = RetrofitHelper.ConversionRateApi.getConversionRate(storedToken, from, to)
                if (response.isSuccessful) {
                    val body = response.body() as? Map<*, *>
                    val rate = (body?.get("rate") as? Double)?.toBigDecimal()
                    val finalAmount = rate?.times(amountValue.toBigDecimal())
                    _conversionRate.value = finalAmount?.setScale(3, RoundingMode.HALF_UP)?.toPlainString() ?: "?"
                } else {
                    _conversionRate.value = "?"
                }
            } catch (e: Exception) {
                _conversionRate.value = "?"
            }
        }
    }

    fun getFlagEmoji(currencyCode: String?): String {
        if (currencyCode.isNullOrBlank()) return "🌐"

        val countryCode = currencyCode.take(2).uppercase()

        return if (countryCode.length == 2) {
            val first = Character.codePointAt(countryCode, 0) - 'A'.code + 0x1F1E6
            val second = Character.codePointAt(countryCode, 1) - 'A'.code + 0x1F1E6
            String(Character.toChars(first)) + String(Character.toChars(second))
        } else {
            "🌐"
        }
    }

    private fun validateCurrency() {
        val from = _fromCurrency.value.uppercase()
        val to = _toCurrency.value.uppercase()

        if (from.length != 3 || to.length != 3 || from == to) {
            _conversionRate.value = "?"
            return
        }

        calculateRate()
    }

    fun isValidCurrency(code: String): Boolean {
        return currencies.value.any { it.countryCode.equals(code, ignoreCase = true) }
    }
}