package com.joincoded.bankapi.data.request

data class PaymentLinkRequest(
    val accountNumber: String,
    val amount: Double,
    val currencyCode: String,
    val description: String
) 