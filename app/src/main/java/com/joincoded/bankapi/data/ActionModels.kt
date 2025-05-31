package com.joincoded.bankapi.data

import androidx.compose.runtime.Composable

data class ServiceAction(
    val icon: @Composable () -> Unit,
    val label: String
)

data class TransactionItem(
    val title: String,
    val date: String,
    val amount: String
) 