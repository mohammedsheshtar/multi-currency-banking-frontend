package com.joincoded.bankapi.data

import androidx.compose.runtime.mutableStateOf

data class CardState(
    val card: PaymentCard,
    val isFlipped: Boolean = false
) 