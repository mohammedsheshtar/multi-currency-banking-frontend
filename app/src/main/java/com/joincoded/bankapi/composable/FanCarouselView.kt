package com.joincoded.bankapi.composable


import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.joincoded.bankapi.ViewModel.WalletViewModel
import com.joincoded.bankapi.SVG.CardTransferBoldIcon
import com.joincoded.bankapi.SVG.CreditCardCloseIcon
import com.joincoded.bankapi.SVG.TransferUsersIcon
import com.joincoded.bankapi.SVG.HandHoldingDollarIcon
import com.joincoded.bankapi.SVG.AddCardRoundedIcon
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.ServiceAction
import com.joincoded.bankapi.data.TransactionItem
import com.joincoded.bankapi.data.request.TransferRequest
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import java.time.format.DateTimeFormatter
import java.util.UUID
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.unit.sp

@Composable
fun FanCarouselView(
    cards: List<PaymentCard>,
    modifier: Modifier = Modifier,
    walletViewModel: WalletViewModel
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Vibrator::class.java)
    
    // Observe the cards state from WalletViewModel
    val walletCardsState = walletViewModel.cards.collectAsStateWithLifecycle<List<CardState>>(initialValue = emptyList())
    val walletCards = walletCardsState.value
    
    // Update cardStates when walletCards changes
    val cardStates = remember(walletCards) { 
        mutableStateListOf<CardState>().apply { 
            clear()
            addAll(walletCards)
        }
    }
    
    val heldIndex = remember { mutableStateOf<Int?>(null) }
    val lastHoveredIndex = remember { mutableStateOf<Int?>(null) }
    val cardBounds = remember { mutableStateMapOf<Int, ClosedFloatingPointRange<Float>>() }
    val draggedXMap = remember { mutableStateMapOf<Int, Animatable<Float, AnimationVector1D>>() }
    var focusedCard by remember { mutableStateOf<CardState?>(null) }
    var showFocusedView by remember { mutableStateOf(false) }
    var showTransferScreen by remember { mutableStateOf(false) }
    var showTransferToOthersScreen by remember { mutableStateOf(false) }
    var showPayMeScreen by remember { mutableStateOf(false) }
    var showCloseAccountModal by remember { mutableStateOf(false) }
    var selectedCardForClose by remember { mutableStateOf<PaymentCard?>(null) }
    var showCreateAccountScreen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Update focusedCard when walletCards changes
    LaunchedEffect(walletCards) {
        Log.d("FanCarouselView", """
            🔄 Wallet cards updated:
            - Current focused card: ${focusedCard?.card?.accountNumber}
            - Available cards: ${walletCards.map { "${it.card.accountNumber} (${it.card.accountNumber})" }}
        """.trimIndent())
        
        focusedCard?.let { currentFocused ->
            // Find the updated version of the focused card
            val updatedFocusedCard = walletCards.find { it.card.accountNumber == currentFocused.card.accountNumber }
            if (updatedFocusedCard != null) {
                Log.d("FanCarouselView", "✅ Found updated version of focused card: ${updatedFocusedCard.card.accountNumber}")
                focusedCard = updatedFocusedCard
                // Refresh transactions when card is updated
                walletViewModel.fetchTransactionHistory(updatedFocusedCard.card.accountNumber, forceRefresh = true)
            } else {
                Log.e("FanCarouselView", "❌ Could not find updated version of focused card: ${currentFocused.card.accountNumber}")
            }
        }
    }

    LaunchedEffect(focusedCard) {
        Log.d("FanCarouselView", """
            🎯 Focused card changed:
            - New focused card: ${focusedCard?.card?.accountNumber}
            - Account number: ${focusedCard?.card?.accountNumber}
            - Card type: ${focusedCard?.card?.type}
        """.trimIndent())
        
        if (focusedCard != null) {
            showFocusedView = true
            // Force refresh transactions when focusing a new card
            walletViewModel.fetchTransactionHistory(focusedCard!!.card.accountNumber, forceRefresh = true)
        }
    }

    // Add effect to refresh transactions after transfers
    LaunchedEffect(showTransferScreen, showTransferToOthersScreen, showPayMeScreen) {
        if (!showTransferScreen && !showTransferToOthersScreen && !showPayMeScreen && focusedCard != null) {
            // Refresh transactions when returning from any transfer screen
            Log.d("FanCarouselView", "🔄 Refreshing transactions after transfer screen")
            walletViewModel.fetchTransactionHistory(focusedCard!!.card.accountNumber, forceRefresh = true)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(focusedCard) {
                if (focusedCard == null) {
                    CardGestureHandler(
                        heldIndex, lastHoveredIndex, cardBounds, vibrator
                    )
                }
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        if (showCreateAccountScreen) {
            CreateAccountScreen(
                onBack = { showCreateAccountScreen = false },
                walletViewModel = walletViewModel
            )
        } else if (focusedCard == null) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Card stack in its own Box to maintain alignment
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    CardStack(
                        cardStates = cardStates,
                        heldIndex = heldIndex,
                        cardBounds = cardBounds,
                        draggedXMap = draggedXMap,
                        focusedCard = focusedCard,
                        setFocusedCard = { focusedCard = it },
                        coroutineScope = coroutineScope,
                        cameraDistancePx = 16f,
                        cards = walletCards.map { it.card },
                        vibrator = vibrator
                    )
                }

                // Create Account icon in top left corner
                AddCardRoundedIcon(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .size(32.dp)
                        .clickable { showCreateAccountScreen = true },
                    color = Color(0xFFB297E7)
                )
            }

            heldIndex.value?.let { idx ->
                cardStates.getOrNull(idx)?.card?.let { card ->
                    Box(Modifier.align(Alignment.TopStart).padding(16.dp)) {
                        Text("${card.balance} ${card.currency}", color = Color.White)
                    }
                }
            }
        }

        if (showTransferToOthersScreen && focusedCard != null) {
            TransferToOthersScreen(
                fromCard = focusedCard!!.card,
                onBack = {
                    showTransferToOthersScreen = false
                    showFocusedView = true
                },
                walletViewModel = walletViewModel
            )
        } else if (showPayMeScreen && focusedCard != null) {
            PayMeScreen(
                fromCard = focusedCard!!.card,
                onBack = {
                    showPayMeScreen = false
                    showFocusedView = true
                },
                walletViewModel = walletViewModel
            )
        } else if (showTransferScreen && focusedCard != null) {
            val fromCard = focusedCard!!.card
            var transferAmount by remember { mutableStateOf("") }
            var showError by remember { mutableStateOf<String?>(null) }

            if (showTransferScreen) {
                TransferScreen(
                    cards = walletCards.map { it.card },
                    amount = transferAmount,
                    onAmountChanged = { 
                        // Only allow numbers and decimal point
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            transferAmount = it
                            showError = null
                        }
                    },
                    onBack = {
                        // When returning from transfer screen, show the focused view
                        showTransferScreen = false
                        showFocusedView = true
                    },
                    onTransfer = { password ->
                        // The actual transfer is now handled by the TransferScreen using walletViewModel
                        // Don't hide the screen immediately, let the success dialog handle it
                        transferAmount = "" // Clear the amount
                    },
                    errorMessage = showError,
                    walletViewModel = walletViewModel,
                    initialFromCard = focusedCard?.card
                )
            }
        } else {
            AnimatedVisibility(
                visible = showFocusedView,
                enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)),
                exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)),
                modifier = Modifier.fillMaxSize()
            ) {
                focusedCard?.let {
                    Box(modifier = Modifier.fillMaxSize()) {
                        FanCarouselFocusedView(
                            card = it.card,
                            services = listOf(
                                ServiceAction(
                                    icon = { CardTransferBoldIcon(modifier = Modifier.size(64.dp)) },
                                    label = "Transfer",
                                    onClick = { showTransferScreen = true }
                                ),
                                ServiceAction(
                                    icon = { TransferUsersIcon() },
                                    label = "Transfer to Others",
                                    onClick = { 
                                        showTransferToOthersScreen = true
                                        showFocusedView = false
                                    }
                                ),
                                ServiceAction(
                                    icon = { HandHoldingDollarIcon() },
                                    label = "Pay Me",
                                    onClick = { 
                                        showPayMeScreen = true
                                        showFocusedView = false
                                    }
                                ),
                                ServiceAction(
                                    icon = { CreditCardCloseIcon() },
                                    label = "Close Account",
                                    onClick = {
                                        selectedCardForClose = it.card
                                        showCloseAccountModal = true
                                    }
                                )
                            ),
                            transactions = emptyList(),
                            onClose = {
                                showFocusedView = false
                                focusedCard = null
                            },
                            onSwipeOut = {
                                showFocusedView = false
                                focusedCard = null
                            },
                            onTransferClick = {
                                showTransferScreen = true
                            },
                            walletViewModel = walletViewModel
                        )
                    }
                }
            }
        }
    }

    selectedCardForClose?.let { card ->
        CloseAccountModal(
            card = card,
            showModal = showCloseAccountModal,
            onDismiss = {
                showCloseAccountModal = false
                selectedCardForClose = null
            },
            walletViewModel = walletViewModel
        )
    }
}
