package com.joincoded.bankapi.composable


import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.PaymentCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

@Composable
fun CardStack(
    cardStates: List<CardState>,
    heldIndex: MutableState<Int?>,
    cardBounds: MutableMap<Int, ClosedFloatingPointRange<Float>>,
    draggedXMap: MutableMap<Int, Animatable<Float, AnimationVector1D>>,
    focusedCard: CardState?,
    setFocusedCard: (CardState?) -> Unit,
    coroutineScope: CoroutineScope,
    cameraDistancePx: Float,
    cards: List<PaymentCard>,
    vibrator: Vibrator?
) {
    val visibleStates = if (focusedCard != null) listOf(focusedCard) else cardStates

    val sortedVisibleCards = visibleStates.mapIndexed { index, state ->
        val centerIndex = 2
        val relativePos = index - centerIndex
        Triple(index, state, relativePos)
    }.sortedBy { abs(it.third) }

    sortedVisibleCards.forEach { (index, state, relativePos) ->
        val angle by animateFloatAsState(if (state == focusedCard) 0f else -relativePos * 18f)
        val offsetY by animateDpAsState(if (state == focusedCard) 0.dp else (relativePos * 64).dp)
        val rotationYAnim by animateFloatAsState(
            if (state.isFlipped.value && focusedCard == state) 180f else 0f,
            animationSpec = spring()
        )

        val isHeld = index == heldIndex.value
        val draggedX = draggedXMap.getOrPut(index) { Animatable(0f) }

        val finalScale by animateFloatAsState(
            when {
                focusedCard == state -> 0.95f
                isHeld -> 1.05f
                relativePos == 0 -> 1.02f
                else -> 1f
            },
            animationSpec = spring()
        )

        val isSwipingOut = remember { mutableStateOf(false) }

        val animatedOffsetX by animateDpAsState(
            targetValue = when {
                state == focusedCard -> 0.dp
                isSwipingOut.value -> (-500).dp
                else -> (abs(relativePos) * 30).dp
            },
            animationSpec = tween(durationMillis = 400)
        )

        LaunchedEffect(isHeld) {
            if (!isHeld && draggedX.isRunning) {
                draggedX.stop()
                draggedX.snapTo(0f)
            }
        }

        val isInteractive = focusedCard == null

        val gradient = when (cards.indexOf(state.card) % 5) {
            0 -> Brush.verticalGradient(listOf(Color(0xFF8E77BB), Color.Black))
            1 -> Brush.verticalGradient(listOf(Color(0xFF231E31), Color.DarkGray))
            2 -> Brush.verticalGradient(listOf(Color(0xFF1D162A), Color(0xFF9688B9)))
            3 -> Brush.verticalGradient(listOf(Color(0xFF1C1926), Color(0xFF191623)))
            else -> Brush.verticalGradient(listOf(Color.DarkGray, Color.Black))
        }

        Box(
            modifier = Modifier
                .offset(x = if (focusedCard == state) 0.dp else 100.dp)
                .graphicsLayer {
                    rotationZ = angle
                    translationX = animatedOffsetX.toPx() + draggedX.value
                    translationY = offsetY.toPx()
                    scaleX = finalScale
                    scaleY = finalScale
                    rotationY = rotationYAnim
                    alpha = if (state == focusedCard) 1f else 1f - min(1f, abs(draggedX.value) / 150f)
                    cameraDistance = cameraDistancePx
                }
                .shadow(
                    elevation = if (isHeld || state == focusedCard) 20.dp else 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = true
                )
                .onGloballyPositioned { coordinates ->
                    val pos = coordinates.positionInRoot()
                    val height = coordinates.size.height.toFloat()
                    cardBounds[index] = pos.y..(pos.y + height)
                }
                .zIndex(if (state == focusedCard) 999f else (100 - abs(relativePos)).toFloat())
                .pointerInput(focusedCard, state.card.cardNumber) {
                    detectTapGestures(
                        onTap = { if (isInteractive) setFocusedCard(state) },
                        onDoubleTap = {
                            if (focusedCard == state) {
                                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                setFocusedCard(null)
                            }
                        }
                    )
                }
                .pointerInput(state.card.cardNumber) {
                    if (isInteractive) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                coroutineScope.launch {
                                    draggedX.snapTo(draggedX.value + dragAmount)
                                }
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (draggedX.value < -250f) {
                                        isSwipingOut.value = true
                                        draggedX.snapTo(0f)
                                        delay(400)
                                        setFocusedCard(state)
                                        isSwipingOut.value = false
                                    } else {
                                        draggedX.animateTo(0f, tween(300))
                                    }
                                }
                            }
                        )
                    }
                }
        ) {
            PaymentCardView(
                card = state.card,
                modifier = Modifier.size(width = 400.dp, height = 240.dp),
                backgroundGradient = gradient
            )
        }
    }
}
