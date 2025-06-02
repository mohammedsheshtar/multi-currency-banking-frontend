package com.joincoded.bankapi.data

// ServiceAction is now defined in ServiceAction.kt
// This file only contains TransactionItem

data class TransactionItem(
    val id: String,
    val title: String,
    val date: String,
    val amount: String,
    val cardId: String,
)
