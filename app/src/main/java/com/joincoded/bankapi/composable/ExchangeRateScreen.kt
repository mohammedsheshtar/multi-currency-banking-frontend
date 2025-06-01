package com.joincoded.bankapi.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.viewmodel.ExchangeRateViewModel
import com.joincoded.bankapi.viewmodel.ExchangeRateUiState
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically


@Composable
fun ExchangeRateScreen(
    viewModel: ExchangeRateViewModel = viewModel()
) {
    val fromCurrency by viewModel.fromCurrency.collectAsState()
    val toCurrency by viewModel.toCurrency.collectAsState()
    val exchangeRateUiState by viewModel.exchangeRateUiState.collectAsState()
    val conversionRate by viewModel.conversionRate.collectAsState()

    val animatedRate by animateFloatAsState(
        targetValue = conversionRate?.toFloatOrNull() ?: 0f,
        animationSpec = tween(durationMillis = 450),
        label = "AnimatedRate"
    )


    LaunchedEffect(Unit) {
        viewModel.setFromCurrency(viewModel.fromCurrency.value)
        viewModel.setToCurrency(viewModel.toCurrency.value)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            var rawFrom by remember { mutableStateOf(fromCurrency) }

            LaunchedEffect(rawFrom) {
                if (rawFrom.length == 3) {
                    delay(250) // debounce delay
                    viewModel.setFromCurrency(rawFrom.uppercase())
                }
            }

            OutlinedTextField(
                value = rawFrom.uppercase(),
                onValueChange = {
                    if (it.length <= 3) rawFrom = it.uppercase()
                },
                label = { Text("From") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(50)
            )

            var rawTo by remember { mutableStateOf(toCurrency) }

            LaunchedEffect(rawTo) {
                if (rawTo.length == 3) {
                    delay(250)
                    viewModel.setToCurrency(rawTo.uppercase())
                }
            }

            OutlinedTextField(
                value = rawTo.uppercase(),
                onValueChange = {
                    if (it.length <= 3) rawTo = it.uppercase()
                },
                label = { Text("To") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(50)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val fromNormalized = fromCurrency.uppercase()
        val toNormalized = toCurrency.uppercase()

        if (fromNormalized.length == 3 && toNormalized.length == 3) {
            when {
                fromNormalized == toNormalized -> {
                    Text(
                        text = "From and To currencies cannot be the same.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                !viewModel.isValidCurrency(fromNormalized) || !viewModel.isValidCurrency(toNormalized) -> {
                    Text(
                        text = "Invalid currency code. Please check your input.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    val showConversion = remember(fromNormalized, toNormalized, conversionRate) {
                        conversionRate != "?" &&
                                fromNormalized != toNormalized &&
                                viewModel.isValidCurrency(fromNormalized) &&
                                viewModel.isValidCurrency(toNormalized)
                    }

                    AnimatedVisibility(
                        visible = showConversion,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { +80 }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "1 $fromCurrency",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = "arrow",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format("%.3f", animatedRate) + " $toCurrency",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (exchangeRateUiState) {
            is ExchangeRateUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            is ExchangeRateUiState.Error -> {
                Text(
                    text = (exchangeRateUiState as ExchangeRateUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            is ExchangeRateUiState.Success -> {
                val rates = (exchangeRateUiState as ExchangeRateUiState.Success).rates

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    items(rates.take(30)) { rate ->
                        val fromFlag = viewModel.getFlagEmoji(rate.from)
                        val toFlag = viewModel.getFlagEmoji(rate.to)
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
                                Text(
                                    text = "$fromFlag ${rate.from} → $toFlag ${rate.to}",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = rate.rate.toPlainString(),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
