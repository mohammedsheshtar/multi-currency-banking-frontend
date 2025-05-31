package com.joincoded.bankapi.data

data class PaymentCard(
    val id: String,
    val number: String,
    val name: String,
    val expMonth: String,
    val expYear: String,
    val cvv: String,
    val type: String,
    val background: Int,
    val balance: Double,
    val currency: String
) 