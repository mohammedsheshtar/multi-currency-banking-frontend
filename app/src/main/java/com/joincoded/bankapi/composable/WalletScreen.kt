package com.joincoded.bankapi.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.joincoded.bankapi.viewmodel.WalletViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun WalletScreen(
    navController: NavController,
    walletViewModel: WalletViewModel
) {
    val cards by walletViewModel.cards.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        walletViewModel.fetchUserCards()
    }

    FanCarouselView(
        cards = cards.map { it.card },
        walletViewModel = walletViewModel
    )
} 