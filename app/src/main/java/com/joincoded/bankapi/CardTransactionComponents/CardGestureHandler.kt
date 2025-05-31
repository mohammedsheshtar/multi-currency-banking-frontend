package com.joincoded.bankapi.CardTransactionComponents

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.launch

suspend fun PointerInputScope.CardGestureHandler(
    heldIndex: androidx.compose.runtime.MutableState<Int?>,
    lastHoveredIndex: androidx.compose.runtime.MutableState<Int?>,
    cardBounds: Map<Int, ClosedFloatingPointRange<Float>>,
    vibrator: Vibrator?
) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val pointerY = event.changes.firstOrNull()?.position?.y ?: 0f
            val pointerX = event.changes.firstOrNull()?.position?.x ?: 0f
            val isPressed = event.changes.any { it.pressed }

            heldIndex.value = if (isPressed && pointerX > 350f) {
                val hovered = cardBounds.entries.firstOrNull { (_, bounds) -> pointerY in bounds }?.key
                if (hovered != null && hovered != lastHoveredIndex.value) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                    lastHoveredIndex.value = hovered
                }
                hovered
            } else null
        }
    }
}

@Composable
fun CardGestureHandler(
    modifier: Modifier = Modifier,
    onCardHover: () -> Unit = {},
    onCardClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onCardClick()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
            }
    ) {
        // Card content will be added here
    }
} 