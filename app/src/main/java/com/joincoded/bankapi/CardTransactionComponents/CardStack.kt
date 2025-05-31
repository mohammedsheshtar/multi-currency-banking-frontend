package com.joincoded.bankapi.CardTransactionComponents

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
            if (state.isFlipped && focusedCard == state) 180f else 0f,
            animationSpec = spring()
        )

        val isHeld = index == heldIndex.value
        val draggedX = draggedXMap.getOrPut(index) { Animatable(0f) }
        val finalScale by animateFloatAsState(
            if (focusedCard == state) 0.95f else if (isHeld) 1.05f else if (relativePos == 0) 1.02f else 1f,
            animationSpec = spring()
        )

        val isSwipingOut = remember { mutableStateOf(false) }
        val animatedOffsetX by animateDpAsState(
            targetValue = when {
                state == focusedCard -> 0.dp
                isSwipingOut.value -> (-500).dp
                else -> (abs(relativePos) * 30).dp
            },
            animationSpec = tween(durationMillis = 400),
            label = "swipeOutOffset"
        )

        LaunchedEffect(isHeld) {
            if (!isHeld && draggedX.isRunning) {
                draggedX.stop()
                draggedX.snapTo(0f)
            }
        }

        val isInteractive = focusedCard == null

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
                .pointerInput(focusedCard, state.card.number) {
                    detectTapGestures(
                        onTap = { 
                            if (isInteractive) {
                                heldIndex.value = index
                                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                setFocusedCard(state)
                            }
                        },
                        onDoubleTap = {
                            if (focusedCard == state) {
                                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                setFocusedCard(null)
                                heldIndex.value = null
                            }
                        }
                    )
                }
                .pointerInput(state.card.number) {
                    if (isInteractive) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                coroutineScope.launch {
                                    heldIndex.value = index
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
                                        heldIndex.value = null
                                    }
                                }
                            }
                        )
                    }
                }
        ) {
            PaymentCardView(
                card = state.card,
                modifier = Modifier.size(width = 400.dp, height = 240.dp)
            )
        }
    }
} 