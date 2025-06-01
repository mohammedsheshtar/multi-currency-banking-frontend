package com.joincoded.bankapi.composable


import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.MutableState
import androidx.compose.ui.input.pointer.PointerInputScope


suspend fun PointerInputScope.CardGestureHandler(
    heldIndex: MutableState<Int?>,
    lastHoveredIndex: MutableState<Int?>,
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
