package com.joincoded.bankapi.data.response

data class PaymentLinkResponse(
    val linkId: String,
    val amount: Double,
    val currency: String,
    val expiresAt: String,
    val description: String?,
    val paymentUrl: String
) 