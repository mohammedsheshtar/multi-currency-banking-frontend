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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.multicurrency_card.components.FanCarouselFocusedView

import com.joincoded.bankapi.SVG.CardTransferBoldIcon
import com.joincoded.bankapi.SVG.CreditCardCloseIcon
import com.joincoded.bankapi.SVG.TransferUsersIcon
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.ServiceAction
import com.joincoded.bankapi.data.TransactionItem
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FanCarouselView(
    cards: List<PaymentCard>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Vibrator::class.java)
    val cardStates = remember { mutableStateListOf<CardState>().apply { addAll(cards.map { CardState(it) }) } }
    val heldIndex = remember { mutableStateOf<Int?>(null) }
    val lastHoveredIndex = remember { mutableStateOf<Int?>(null) }
    val cardBounds = remember { mutableStateMapOf<Int, ClosedFloatingPointRange<Float>>() }
    val draggedXMap = remember { mutableStateMapOf<Int, Animatable<Float, AnimationVector1D>>() }
    var focusedCard by remember { mutableStateOf<CardState?>(null) }
    var showFocusedView by remember { mutableStateOf(false) }
    var showTransferScreen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var transactions by remember { mutableStateOf<List<TransactionItem>>(emptyList()) }
    val token = remember { TokenManager.getToken(context) }

    LaunchedEffect(focusedCard) {
        if (focusedCard != null) {
            showFocusedView = true
            val response = RetrofitHelper.TransactionApi.getTransactionHistory(token, focusedCard!!.card.id.toInt())
            if (response.isSuccessful) {
                val result = response.body() as? List<TransactionItem>
                if (result != null) transactions = result.filter { it.cardId == focusedCard!!.card.id }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(focusedCard) {
                if (focusedCard == null) {
                    CardGestureHandler(
                        heldIndex, lastHoveredIndex, cardBounds, vibrator
                    )
                }
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        if (focusedCard == null) {
            CardStack(
                cardStates = cardStates,
                heldIndex = heldIndex,
                cardBounds = cardBounds,
                draggedXMap = draggedXMap,
                focusedCard = focusedCard,
                setFocusedCard = { focusedCard = it },
                coroutineScope = coroutineScope,
                cameraDistancePx = 16f,
                cards = cards,
                vibrator = vibrator
            )
            heldIndex.value?.let { idx ->
                cardStates.getOrNull(idx)?.card?.let { card ->
                    Box(Modifier.align(Alignment.TopStart).padding(16.dp)) {
                        Text("${card.balance} ${card.currency}", color = Color.White)
                    }
                }
            }
        }

        if (showTransferScreen && focusedCard != null) {
            val fromCard = focusedCard!!.card
            val toCard = cards.firstOrNull { it != fromCard } ?: fromCard
            TransferScreen(
                cards = cards,
                amount = "",
                currencies = listOf("KWD", "USD", "EUR"),
                selectedCurrency = "KWD",
                onAmountChanged = {},
                onCurrencySelected = {},
                onBack = {
                    showTransferScreen = false
                    showFocusedView = true
                }
            )
        } else {
            AnimatedVisibility(
                visible = showFocusedView,
                enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)),
                exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)),
                modifier = Modifier.fillMaxSize()
            ) {
                focusedCard?.let {
                    FanCarouselFocusedView(
                        card = it.card,
                        services = listOf(
                            ServiceAction({ CardTransferBoldIcon(modifier = Modifier.size(64.dp)) }, "Transfer"),
                            ServiceAction({ TransferUsersIcon() }, "Transfer to Others"),
                            ServiceAction({ CreditCardCloseIcon() }, "Close Account")
                        ),
                        transactions = transactions,
                        onClose = {
                            showFocusedView = false
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(400)
                                focusedCard = null
                            }
                        },
                        onSwipeOut = {
                            showFocusedView = false
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(400)
                                focusedCard = null
                            }
                        },
                        onTransferClick = {
                            showTransferScreen = true
                            showFocusedView = false
                        }
                    )
                }
            }
        }
    }
}
