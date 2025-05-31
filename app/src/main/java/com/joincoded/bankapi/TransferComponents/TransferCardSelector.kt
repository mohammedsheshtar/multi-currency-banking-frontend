package com.joincoded.bankapi.TransferComponents

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.joincoded.bankapi.data.PaymentCard

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TransferCardSelector(
    cards: List<PaymentCard>,
    modifier: Modifier = Modifier,
    onSwapCards: (PaymentCard, PaymentCard) -> Unit
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Vibrator::class.java)

    var fromCardIndex by remember { mutableStateOf(0) }
    var toCardIndex by remember { mutableStateOf(1.coerceAtMost(cards.lastIndex)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -40 && fromCardIndex < cards.lastIndex) {
                        fromCardIndex++
                        toCardIndex = (fromCardIndex + 1).coerceAtMost(cards.lastIndex)
                        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else if (dragAmount > 40 && fromCardIndex > 0) {
                        fromCardIndex--
                        toCardIndex = (fromCardIndex + 1).coerceAtMost(cards.lastIndex)
                        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = cards[fromCardIndex],
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() with slideOutHorizontally { -it } + fadeOut()
            },
            label = "Transfer Card Animation"
        ) { card ->
            PaymentCardView(
                card = card,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1.6f)
            )
        }

        // Swap Icon/Button
        IconButton(
            onClick = {
                onSwapCards(cards[fromCardIndex], cards[toCardIndex])
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            RoundTransferVerticalBoldIcon(
                modifier = Modifier.size(80.dp)
            )
        }

        // Balance Display
        Text(
            text = "${cards[fromCardIndex].balance} ${cards[fromCardIndex].currency}",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
    }
} 