package com.joincoded.bankapi.composable

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joincoded.bankapi.R
import com.joincoded.bankapi.ViewModel.WalletViewModel
import com.joincoded.bankapi.composable.FilterOptionsButton
import com.joincoded.bankapi.composable.PaymentCardView
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.ServiceAction
import com.joincoded.bankapi.data.TransactionItem
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.utils.TokenManager
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun FanCarouselFocusedView(
    card: PaymentCard,
    services: List<ServiceAction>,
    transactions: List<TransactionItem>,
    onClose: () -> Unit,
    onSwipeOut: () -> Unit,
    onTransferClick: () -> Unit,
    walletViewModel: WalletViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Collect states from ViewModel
    val walletTransactions = walletViewModel.transactions.collectAsStateWithLifecycle()
    val isLoadingTransactions = walletViewModel.isLoadingTransactions.collectAsStateWithLifecycle()
    val transactionError = walletViewModel.transactionError.collectAsStateWithLifecycle()

    // Log the card details for debugging
    Log.d("FanCarouselFocusedView", """
        🎯 Card details:
        - Account Number: ${card.accountNumber}
        - Type: ${card.type}
        - Currency: ${card.currency}
    """.trimIndent())

    // Fetch transactions when expanded or when card changes
    LaunchedEffect(card.accountNumber, expanded) {
        Log.d("FanCarouselFocusedView", "🔄 Fetching transactions for account: ${card.accountNumber}")
        walletViewModel.fetchTransactionHistory(card.accountNumber, forceRefresh = expanded)
    }

    // Refresh transactions when returning from transfer screen
    LaunchedEffect(Unit) {
        Log.d("FanCarouselFocusedView", "🔄 Initial transaction fetch for account: ${card.accountNumber}")
        walletViewModel.fetchTransactionHistory(card.accountNumber, forceRefresh = true)
    }

    BackHandler(enabled = true) {
        onSwipeOut()
    }

    val targetHeight = if (expanded) 740.dp else 460.dp
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(durationMillis = 350),
        label = "sheetHeight"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -100f || dragAmount > 100f) {
                        onSwipeOut()
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PaymentCardView(
                card = card,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                backgroundGradient = Brush.verticalGradient(
                    listOf(Color(0xFF5E5280), Color.Black)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Service Actions Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                services.forEach { service ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A1A1D))
                            .clickable { service.onClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        CompositionLocalProvider(LocalContentColor provides Color(0xFFB297E7)) {
                            Box(modifier = Modifier.size(28.dp)) {
                                service.icon()
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedHeight)
                    .padding(15.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 6.dp)
                        .background(Color.DarkGray, RoundedCornerShape(3.dp))
                        .align(Alignment.CenterHorizontally)
                        .clickable { expanded = !expanded }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterOptionsButton(
                            onFilterSelected = { selectedFilter ->
                                println("Selected Filter: $selectedFilter")
                                walletViewModel.fetchTransactionHistory(card.accountNumber, true)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Recent Transactions",
                            color = Color(0xFFB297E7),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Add refresh button
                        IconButton(
                            onClick = { walletViewModel.fetchTransactionHistory(card.accountNumber, true) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_refresh_24),
                                contentDescription = "Refresh",
                                tint = Color(0xFFB297E7)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(
                                id = if (expanded)
                                    R.drawable.baseline_keyboard_double_arrow_down_24
                                else
                                    R.drawable.baseline_keyboard_double_arrow_up_24
                            ),
                            contentDescription = "Toggle Sheet",
                            tint = Color(0xFFB297E7),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLoadingTransactions.value) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFFB297E7),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    } else if (transactionError.value != null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    transactionError.value ?: "Error loading transactions",
                                    color = Color.Red,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else if (walletTransactions.value.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No transactions found",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        items(walletTransactions.value) { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1A1A1D), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        tx.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        tx.date,
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                    // Add account number if it's a transfer
                                    if (tx.title.contains("Transfer", ignoreCase = true)) {
                                        Text(
                                            "Account: ${tx.cardId}",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${tx.amount} ${card.currency}",
                                        color = if (tx.amount.startsWith("-")) 
                                            Color(0xFFFF6F91) 
                                        else 
                                            Color(0xFF00BCD4),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Success",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getDrawableId(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}