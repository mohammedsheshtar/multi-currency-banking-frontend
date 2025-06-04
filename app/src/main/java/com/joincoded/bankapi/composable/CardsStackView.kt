package com.joincoded.bankapi.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import com.joincoded.bankapi.data.PaymentCard

@Composable
fun CardsStackView(
    cards: List<PaymentCard>,
    modifier: Modifier = Modifier,
    overlapDp: Dp = (-100).dp,
    staggerDp: Dp = 12.dp
) {
    var isFirstLoad by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        isFirstLoad = false
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(end = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(overlapDp)
    ) {
        cards.forEachIndexed { index, card ->
            val entryAnimation = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                if (isFirstLoad) {
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

            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                    visibilityThreshold = null
                ),
                label = "scale"
            )

            val alpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300),
                label = "alpha"
            )

            PaymentCardView(
                card = card,
                modifier = Modifier
                    .offset(
                        x = if (isFirstLoad) (1f - entryAnimation.value) * 500.dp else 0.dp,
                        y = (index * staggerDp.value).dp
                    )
                    .scale(if (isFirstLoad) 0.8f + (entryAnimation.value * 0.2f) else scale)
                    .alpha(if (isFirstLoad) entryAnimation.value else alpha)
                    .zIndex(index.toFloat())
            )
        }
    }
}
