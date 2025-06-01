package com.joincoded.bankapi.data


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf


data class PaymentCard(
    val id: String,
    val balance: Double,
    val currency: String,
    val name: String,
    val cardNumber: String,
    val expMonth: String,
    val expYear: String,
    val cvv: String,
    val type: String,
    val background: String,
)
//data class CardState(
//    val card: PaymentCard,
//    var isFlipped: MutableState<Boolean> = mutableStateOf(false)
//)