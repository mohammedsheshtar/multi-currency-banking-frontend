package com.joincoded.bankapi.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.viewmodel.ExchangeRateViewModel

@Composable
fun ExchangeRateScreen(
    viewModel: ExchangeRateViewModel = viewModel()
) {
    val fromCurrency by viewModel.fromCurrency.collectAsState()
    val toCurrency by viewModel.toCurrency.collectAsState()
    val mockRates = viewModel.mockRates

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Currency Input Fields
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = fromCurrency,
                onValueChange = { viewModel.setFromCurrency(it) },
                label = { Text("From") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = toCurrency,
                onValueChange = { viewModel.setToCurrency(it) },
                label = { Text("To") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Conversion Result in Middle
        if (fromCurrency.isNotBlank() && toCurrency.isNotBlank()) {
            Text(
                text = "Conversion: 1 $fromCurrency → ? $toCurrency",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Centered LazyColumn with Padding Bottom
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            items(mockRates) { (from, to, rate) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val fromFlag = getFlagEmojiForCurrency(from)
                        val toFlag = getFlagEmojiForCurrency(to)

                        Text(
                            text = "$fromFlag $from → $toFlag $to",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = rate,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

fun getFlagEmojiForCurrency(currencyCode: String): String {
    val countryCode = when (currencyCode.uppercase()) {
        "USD" -> "US"
        "EUR" -> "EU"
        "KWD" -> "KW"
        "SAR" -> "SA"
        "JPY" -> "JP"
        "GBP" -> "GB"
        "CAD" -> "CA"
        "CNY" -> "CN"
        "INR" -> "IN"
        "AUD" -> "AU"
        else -> return "🌐" // fallback emoji
    }

    if (countryCode.length != 2) return "🌐"

    val first = Character.codePointAt(countryCode, 0) - 'A'.code + 0x1F1E6
    val second = Character.codePointAt(countryCode, 1) - 'A'.code + 0x1F1E6

    return String(Character.toChars(first)) + String(Character.toChars(second))
}

