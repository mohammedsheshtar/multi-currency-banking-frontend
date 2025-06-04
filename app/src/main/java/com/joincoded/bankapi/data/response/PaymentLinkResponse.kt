package com.joincoded.bankapi.data.response

data class PaymentLinkResponse(
    val paymentLink: String,
    val amount: Double,
    val currency: String,
    val expiresAt: String,
    val description: String?
) 