package com.joincoded.bankapi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Exchange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.CardTransactionComponents.CardStack
import com.joincoded.bankapi.TransferComponents.TransferScreen
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.viewmodel.CardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CardViewModel = viewModel(),
    onNavigateToExchangeRate: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showTransferScreen by remember { mutableStateOf(false) }
    var selectedCard by remember { mutableStateOf<PaymentCard?>(null) }
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    
    // Card Stack Animation States
    val cardStates = remember { mutableStateListOf<CardState>() }
    val heldIndex = remember { mutableStateOf<Int?>(null) }
    val cardBounds = remember { mutableStateMapOf<Int, ClosedFloatingPointRange<Float>>() }
    val draggedXMap = remember { mutableStateMapOf<Int, Animatable<Float, AnimationVector1D>>() }
    val focusedCard = remember { mutableStateOf<CardState?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val cameraDistancePx = with(LocalDensity.current) { 8.dp.toPx() }
    
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "AUD")

    // Initialize card states when cards change
    LaunchedEffect(cards) {
        cardStates.clear()
        cardStates.addAll(cards.map { CardState(card = it) })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cards") },
                actions = {
//                    IconButton(onClick = onNavigateToExchangeRate) {
//                        Icon(Icons.Default.Exchange, contentDescription = "Exchange Rates")
//                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                showTransferScreen && selectedCard != null -> {
                    TransferScreen(
                        cards = cards,
                        amount = amount,
                        currencies = currencies,
                        selectedCurrency = selectedCurrency,
                        onAmountChanged = { amount = it },
                        onCurrencySelected = { selectedCurrency = it },
                        onBack = { showTransferScreen = false }
                    )
                }
                else -> {
                    CardStack(
                        cardStates = cardStates,
                        heldIndex = heldIndex,
                        cardBounds = cardBounds,
                        draggedXMap = draggedXMap,
                        focusedCard = focusedCard.value,
                        setFocusedCard = { focusedCard.value = it },
                        coroutineScope = coroutineScope,
                        cameraDistancePx = cameraDistancePx,
                        cards = cards,
                        vibrator = null
                    )
                }
            }
        }
    }
} 