package com.joincoded.bankapi.data

import androidx.compose.runtime.Composable

data class ServiceAction(
    val icon: @Composable () -> Unit,
    val label: String,
    val onClick: () -> Unit = {}  // Default empty lambda for backward compatibility
) 