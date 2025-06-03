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
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.PaymentCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min
import android.util.Log
import com.joincoded.bankapi.composable.availableCardColors

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

    var isFirstLoad by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(100) // Small delay to ensure smooth animation
        isFirstLoad = false
    }

    val sortedVisibleCards = visibleStates.mapIndexed { index, state ->
        val centerIndex = 2
        val relativePos = index - centerIndex
        Triple(index, state, relativePos)
    }.sortedBy { abs(it.third) }

    sortedVisibleCards.forEach { (index, state, relativePos) ->
        val entryAnimation = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            if (isFirstLoad) {
                delay(index * 100L) // Stagger the animations
                entryAnimation.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                        visibilityThreshold = null
                    )
                )
            }
        }

        val angle by animateFloatAsState(if (state == focusedCard) 0f else -relativePos * 18f)
        val offsetY by animateDpAsState(if (state == focusedCard) 0.dp else (relativePos * 64).dp)
        val rotationYAnim by animateFloatAsState(
            if (state.isFlipped.value && focusedCard == state) 180f else 0f,
            animationSpec = spring()
        )

        val entryOffsetX by animateDpAsState(
            targetValue = 0.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
                visibilityThreshold = null
            ),
            label = "entryOffsetX"
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

        val gradient = remember(state.card.background) {
            Log.d("CardStack", """
                Card Color Selection:
                - Card Number: ${state.card.accountNumber}
                - Background: ${state.card.background}
                - Index: $index
            """.trimIndent())

            // Find the selected color in availableCardColors
            val selectedColor = availableCardColors.find { it.name == state.card.background }?.gradient
            if (selectedColor != null) {
                Log.d("CardStack", "Using selected color: ${state.card.background}")
                selectedColor
            } else {
                Log.d("CardStack", "No specific color selected, using index-based color")
                // Use index-based colors as fallback
                when (index % availableCardColors.size) {
                    0 -> availableCardColors[0].gradient
                    1 -> availableCardColors[1].gradient
                    2 -> availableCardColors[2].gradient
                    3 -> availableCardColors[3].gradient
                    else -> availableCardColors[4].gradient
                }
            }
        }

        Box(
            modifier = Modifier
                .offset(
                    x = if (focusedCard == state) 0.dp else {
                        val baseOffset = 100.dp + (abs(relativePos) * 30).dp
                        if (isFirstLoad) {
                            baseOffset + (1f - entryAnimation.value) * 500.dp
                        } else {
                            baseOffset
                        }
                    },
                    y = 0.dp
                )
                .graphicsLayer {
                    rotationZ = angle
                    translationX = animatedOffsetX.toPx() + draggedX.value
                    translationY = offsetY.toPx()
                    scaleX = if (isFirstLoad) {
                        finalScale * (0.8f + (entryAnimation.value * 0.2f))
                    } else {
                        finalScale
                    }
                    scaleY = if (isFirstLoad) {
                        finalScale * (0.8f + (entryAnimation.value * 0.2f))
                    } else {
                        finalScale
                    }
                    rotationY = rotationYAnim
                    alpha = if (isFirstLoad) {
                        entryAnimation.value * (if (state == focusedCard) 1f else 1f - min(1f, abs(draggedX.value) / 150f))
                    } else {
                        if (state == focusedCard) 1f else 1f - min(1f, abs(draggedX.value) / 150f)
                    }
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
                backgroundGradient = gradient,
                isFocused = state == focusedCard
            )
        }
    }
}
