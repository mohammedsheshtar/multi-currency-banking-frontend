package com.joincoded.bankapi.CardTransactionComponents

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.Transaction
import com.joincoded.bankapi.data.TransactionFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FanCarouselView(
    cards: List<PaymentCard>,
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    onTransfer: (PaymentCard) -> Unit,
    onTransferToUser: (PaymentCard) -> Unit,
    onCloseAccount: (PaymentCard) -> Unit,
    onFilterTransactions: (TransactionFilter) -> Unit
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Vibrator::class.java)
    val coroutineScope = rememberCoroutineScope()
    
    val cardStates = remember {
        cards.map { CardState(card = it) }
    }
    
    val heldIndex = remember { mutableStateOf<Int?>(null) }
    val lastHoveredIndex = remember { mutableStateOf<Int?>(null) }
    val cardBounds = remember { mutableMapOf<Int, ClosedFloatingPointRange<Float>>() }
    val draggedXMap = remember { mutableMapOf<Int, Animatable<Float, AnimationVector1D>>() }
    val focusedCard = remember { mutableStateOf<CardState?>(null) }
    
    val cameraDistancePx = 8 * context.resources.displayMetrics.density * 12

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Card Stack
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
            vibrator = vibrator
        )

        // Balance display when hovering
        heldIndex.value?.let { idx ->
            cardStates.getOrNull(idx)?.card?.let { card ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(
                            color = Color(0xFF1A1A1D).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${card.currency} ${card.balance}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Focused View
        focusedCard.value?.let { card ->
            FocusedCardOverlay(
                focusedCard = card,
                onClose = { focusedCard.value = null },
                onTransfer = { onTransfer(card.card) },
                onTransferToUser = { onTransferToUser(card.card) },
                onCloseAccount = { onCloseAccount(card.card) },
                transactions = transactions.filter { it.cardId == card.card.id },
                onFilterTransactions = onFilterTransactions
            )
        }
    }
} 