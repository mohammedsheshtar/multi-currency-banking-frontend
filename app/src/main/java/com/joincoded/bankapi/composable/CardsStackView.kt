package com.joincoded.bankapi.composable

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.joincoded.bankapi.data.PaymentCard

@Composable
fun CardsStackView(
    cards: List<PaymentCard>,
    modifier: Modifier = Modifier,
    overlapDp: Dp = (-100).dp,
    staggerDp: Dp = 12.dp
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(end = 16.dp), // Align stack to the right
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(overlapDp) // Overlapping layout
    ) {
        cards.forEachIndexed { index, card ->
            PaymentCardView(
                card = card,
                modifier = Modifier
                    .offset(y = (index * staggerDp.value).dp) // Small vertical nudge
                    .zIndex(index.toFloat()) // Ensure top card is on top
            )
        }
    }
}
