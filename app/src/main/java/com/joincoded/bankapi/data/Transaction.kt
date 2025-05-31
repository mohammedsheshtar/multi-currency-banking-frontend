package com.joincoded.bankapi.data

import java.math.BigDecimal
import java.time.LocalDateTime

data class Transaction(
    val id: String,
    val cardId: String,
    val type: TransactionType,
    val amount: BigDecimal,
    val description: String,
    val timestamp: LocalDateTime,
    val status: TransactionStatus,
    val recipientId: String? = null,
    val recipientName: String? = null
)

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
} 