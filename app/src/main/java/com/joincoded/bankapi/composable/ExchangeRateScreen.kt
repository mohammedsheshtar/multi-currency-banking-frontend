package com.joincoded.bankapi.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.data.response.ConversionRateResponse
import com.joincoded.bankapi.viewmodel.ExchangeRateViewModel
import com.joincoded.bankapi.viewmodel.ExchangeRateUiState
import com.joincoded.bankapi.ui.theme.*
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import androidx.navigation.NavController
import com.joincoded.bankapi.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateScreen(
    navController: NavController,
    walletViewModel: WalletViewModel
) {
    val context = LocalContext.current
    val viewModel: ExchangeRateViewModel = viewModel()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Initialize the view model with context
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
        viewModel.refreshData()
    }
    
    val fromCurrency by viewModel.fromCurrency.collectAsState()
    val toCurrency by viewModel.toCurrency.collectAsState()
    val exchangeRateUiState by viewModel.exchangeRateUiState.collectAsState()
    val conversionRate by viewModel.conversionRate.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val rates = when (val state = exchangeRateUiState) {
        is ExchangeRateUiState.Success -> state.rates
        else -> emptyList()
    }
    val pageSize = 50
    val totalPages = (rates.size + pageSize - 1) / pageSize
    var currentPage by remember { mutableStateOf(1) }
    var rawAmount by remember { mutableStateOf(amount) }

    val listState = rememberLazyListState()

    val animatedRate by animateFloatAsState(
        targetValue = if (amount.isBlank()) 0f else conversionRate?.toFloatOrNull() ?: 0f,
        animationSpec = tween(durationMillis = 350),
        label = "AnimatedRate"
    )

    LaunchedEffect(rawAmount) {
        delay(400)
        viewModel.setAmount(rawAmount)
    }

    LaunchedEffect(Unit) {
        viewModel.setFromCurrency(fromCurrency)
        viewModel.setToCurrency(toCurrency)
    }

    val numberFormatter = DecimalFormat("#,###.###")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Exchange Rates",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextLight
                )

                IconButton(
                    onClick = { viewModel.refreshData() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(CardDark, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Accent
                    )
                }
            }

            // Currency inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var rawFrom by remember { mutableStateOf(fromCurrency) }
                var rawTo by remember { mutableStateOf(toCurrency) }

                LaunchedEffect(rawFrom) {
                    if (rawFrom.length == 3) {
                        delay(250)
                        viewModel.setFromCurrency(rawFrom.uppercase())
                    }
                }

                LaunchedEffect(rawTo) {
                    if (rawTo.length == 3) {
                        delay(250)
                        viewModel.setToCurrency(rawTo.uppercase())
                    }
                }

                OutlinedTextField(
                    value = rawFrom.uppercase(),
                    onValueChange = { if (it.length <= 3) rawFrom = it.uppercase() },
                    label = { Text("From", color = TextLight.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = TextLight.copy(alpha = 0.3f),
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        cursorColor = Accent,
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark
                    )
                )

                OutlinedTextField(
                    value = rawTo.uppercase(),
                    onValueChange = { if (it.length <= 3) rawTo = it.uppercase() },
                    label = { Text("To", color = TextLight.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = TextLight.copy(alpha = 0.3f),
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        cursorColor = Accent,
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark
                    )
                )
            }

            // Amount input
            OutlinedTextField(
                value = rawAmount,
                onValueChange = {
                    if (it.length <= 8 && it.matches(Regex("^\\d*\\.?\\d*\$")))
                        rawAmount = it
                },
                label = { Text("Amount", color = TextLight.copy(alpha = 0.7f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = TextLight.copy(alpha = 0.3f),
                    focusedTextColor = TextLight,
                    unfocusedTextColor = TextLight,
                    cursorColor = Accent,
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark
                )
            )

            val fromNormalized = fromCurrency.uppercase()
            val toNormalized = toCurrency.uppercase()
            val isAmountValid = amount.isNotBlank() && amount.toFloatOrNull() != null

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
                        val showConversion = remember(fromNormalized, toNormalized, conversionRate, isAmountValid) {
                            conversionRate != "?" &&
                                    fromNormalized != toNormalized &&
                                    isAmountValid &&
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
                                    text = "${numberFormatter.format(amount.toDoubleOrNull() ?: 0.0)} $fromCurrency",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Accent
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = "arrow",
                                    tint = Accent
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${numberFormatter.format(animatedRate)} $toCurrency",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Accent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Exchange rate list
            when (exchangeRateUiState) {
                is ExchangeRateUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Accent)
                    }
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

                    val sortedRates = rates
                        .sortedWith(
                            compareByDescending<ConversionRateResponse> {
                                it.from.equals(fromCurrency, ignoreCase = true)
                            }.thenByDescending {
                                it.to.equals(toCurrency, ignoreCase = true)
                            }
                        )

                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        val pagedRates = sortedRates
                            .drop((currentPage - 1) * pageSize)
                            .take(pageSize)

                        items(pagedRates) { rate ->
                            val fromFlag = viewModel.getFlagEmoji(rate.from)
                            val toFlag = viewModel.getFlagEmoji(rate.to)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CardDark),
                                shape = RoundedCornerShape(16.dp)
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
                                        color = TextLight,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = numberFormatter.format(rate.rate),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Accent
                                    )
                                }
                            }
                        }

                        if (totalPages > 1) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val dynamicPages = buildList {
                                        add(1)
                                        if (currentPage > 3) add(-1)
                                        for (i in (currentPage - 1)..(currentPage + 1)) {
                                            if (i in 2 until totalPages) add(i)
                                        }
                                        if (currentPage < totalPages - 2) add(-1)
                                        if (totalPages > 1) add(totalPages)
                                    }.filter { it != 1 && it <= totalPages }

                                    for (page in dynamicPages) {
                                        if (page == -1) {
                                            Text(
                                                "...",
                                                color = TextLight.copy(alpha = 0.5f),
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                        } else {
                                            Button(
                                                onClick = { currentPage = page },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (page == currentPage)
                                                        Accent
                                                    else
                                                        CardDark
                                                ),
                                                shape = RoundedCornerShape(50),
                                                contentPadding = PaddingValues(
                                                    horizontal = 16.dp,
                                                    vertical = 8.dp
                                                ),
                                                modifier = Modifier.defaultMinSize(minWidth = 48.dp)
                                            ) {
                                                Text(
                                                    text = "$page",
                                                    color = if (page == currentPage)
                                                        DarkBackground
                                                    else
                                                        TextLight,
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

        errorMessage?.let { error ->
            LaunchedEffect(error) {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Short
                )
                errorMessage = null
            }
        }
    }
}