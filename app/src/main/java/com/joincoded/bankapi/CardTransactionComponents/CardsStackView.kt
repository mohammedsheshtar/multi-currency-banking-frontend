package com.joincoded.bankapi.CardTransactionComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.joincoded.bankapi.data.PaymentCard

@Composable
fun CardsStackView(cards: List<PaymentCard>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(end = 16.dp), // align to right
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(-100.dp) // cards overlap vertically
    ) {
        cards.forEachIndexed { index, card ->
            PaymentCardView(
                card = card,
                modifier = Modifier
                    .offset(y = (index * 12).dp) // small staggered effect
                    .zIndex(index.toFloat())
            )
        }
    }
} 