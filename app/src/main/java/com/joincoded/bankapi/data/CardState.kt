package com.joincoded.bankapi.data


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class CardState(
    val card: PaymentCard,
    var isFlipped: MutableState<Boolean> = mutableStateOf(false)
)