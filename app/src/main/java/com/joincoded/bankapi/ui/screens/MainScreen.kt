package com.joincoded.bankapi.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.joincoded.bankapi.CardTransactionComponents.CardStack
import com.joincoded.bankapi.CardTransactionComponents.FocusedCardOverlay
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.Transaction
import com.joincoded.bankapi.data.TransactionFilter
import com.joincoded.bankapi.navigation.WaveBottomNavBar
import com.joincoded.bankapi.navigation.NavBarItem
import com.joincoded.bankapi.viewmodel.CardViewModel
import com.joincoded.bankapi.viewmodel.CardViewModelFactory
import com.joincoded.bankapi.repository.CardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.os.Vibrator
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color

@Composable
fun MainScreen(
    navController: NavController,
    onNavigateToProfile: () -> Unit,
    repository: CardRepository,
    modifier: Modifier = Modifier
) {
    val cardViewModel: CardViewModel = viewModel(
        factory = CardViewModelFactory(repository)
    )
    val cards by cardViewModel.cards.collectAsState()
    val transactions by cardViewModel.transactions.collectAsState()
    val isLoading by cardViewModel.isLoading.collectAsState()
    val error by cardViewModel.error.collectAsState()

    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val cameraDistancePx = with(density) { 8.dp.toPx() }

    var cardStates by remember { mutableStateOf(cards.map { CardState(card = it) }) }
    val heldIndex = remember { mutableStateOf<Int?>(null) }
    var cardBounds by remember { mutableStateOf(mutableMapOf<Int, ClosedFloatingPointRange<Float>>()) }
    var draggedXMap by remember { mutableStateOf(mutableMapOf<Int, Animatable<Float, AnimationVector1D>>()) }
    var focusedCard by remember { mutableStateOf<CardState?>(null) }
    var selectedFilter by remember { mutableStateOf<TransactionFilter?>(null) }

    var currentNavIndex by remember { mutableStateOf(0) }
    val navItems = listOf(
        NavBarItem("CurrencyExchange", Icons.Default.Person, Color(0xFFA086CE)),
        NavBarItem("BankCards", Icons.Default.Person, Color(0xFFA086CE)),
        NavBarItem("ShopFilled", Icons.Default.Person, Color(0xFFA086CE)),
        NavBarItem("Profile", Icons.Default.Person, Color(0xFFA086CE))
    )

    LaunchedEffect(cards) {
        cardStates = cards.map { CardState(card = it) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Card Stack in a Box to take remaining space
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                CardStack(
                    cards = cards,
                    cardStates = cardStates,
                    heldIndex = heldIndex,
                    cardBounds = cardBounds,
                    draggedXMap = draggedXMap,
                    focusedCard = focusedCard,
                    setFocusedCard = { focusedCard = it },
                    coroutineScope = coroutineScope,
                    cameraDistancePx = cameraDistancePx,
                    vibrator = vibrator
                )
            }

            // Bottom Navigation
            WaveBottomNavBar(
                items = navItems,
                currentIndex = currentNavIndex,
                onItemSelected = { index ->
                    currentNavIndex = index
                    when (index) {
                        3 -> onNavigateToProfile()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Focused Card Overlay
        focusedCard?.let { cardState ->
            FocusedCardOverlay(
                focusedCard = cardState,
                onClose = { focusedCard = null },
                onTransfer = {
                    // Handle transfer
                },
                onTransferToUser = {
                    // Handle transfer to user
                },
                onCloseAccount = {
                    cardViewModel.closeAccount(cardState.card.id) {
                        focusedCard = null
                    }
                },
                transactions = transactions,
                onFilterTransactions = { filter ->
                    selectedFilter = filter
                    // Apply filter to transactions
                }
            )
        }

        // Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Error Message
        error?.let { errorMessage ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(errorMessage)
            }
        }
    }
} 