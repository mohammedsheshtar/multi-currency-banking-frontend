package com.joincoded.bankapi.data.response

data class TransferLinkResponse(
    val linkId: String,
    val link: String,
    val amount: String,
    val currency: String,
    val senderName: String,
    val senderAccount: String,
    val recipientName: String,
    val recipientPhone: String,
    val expiresAt: String,
    val isUsed: Boolean = false
) 