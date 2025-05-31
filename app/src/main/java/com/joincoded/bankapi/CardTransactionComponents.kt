package com.joincoded.bankapi

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.joincoded.bankapi.data.PaymentCard

// This file should only contain UI components
// API-related code has been moved to separate files:
// - CardApiService.kt
// - CardRepository.kt
// - CardViewModel.kt

@Composable
fun CardTransactionComponents(
    cards: List<PaymentCard>,
    modifier: Modifier = Modifier
) {
    // Your UI components here
} 