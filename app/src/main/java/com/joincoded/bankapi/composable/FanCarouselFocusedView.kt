package com.joincoded.bankapi.composable

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.joincoded.bankapi.viewmodel.WalletViewModel
import com.joincoded.bankapi.composable.FilterOptionsButton
import com.joincoded.bankapi.composable.PaymentCardView
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.ServiceAction
import com.joincoded.bankapi.data.TransactionItem
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.utils.TokenManager
import com.joincoded.bankapi.composable.availableCardColors
import com.joincoded.bankapi.utils.CardColorManager
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.UUID
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.foundation.lazy.rememberLazyListState
import com.joincoded.bankapi.data.TransactionFilter

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
    var currentFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    var showColorPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cardColorManager = remember { CardColorManager.getInstance(context) }
    var selectedColor by remember { mutableStateOf(card.background) }
    var previewColor by remember { mutableStateOf(card.background) }

    // Collect states from ViewModel
    val walletTransactions = walletViewModel.transactions.collectAsStateWithLifecycle()
    val isLoadingTransactions = walletViewModel.isLoadingTransactions.collectAsStateWithLifecycle()
    val transactionError = walletViewModel.transactionError.collectAsStateWithLifecycle()

    // Filter transactions based on current filter
    val filteredTransactions = remember(walletTransactions.value, currentFilter) {
        val sortedTransactions = when (currentFilter) {
            TransactionFilter.ALL -> walletTransactions.value
            TransactionFilter.DEPOSITS -> walletTransactions.value.filter { !it.amount.startsWith("-") && !it.title.contains("Transfer", ignoreCase = true) }
            TransactionFilter.WITHDRAWALS -> walletTransactions.value.filter { it.amount.startsWith("-") && !it.title.contains("Transfer", ignoreCase = true) }
            TransactionFilter.TRANSFERS -> walletTransactions.value.filter { it.title.contains("Transfer", ignoreCase = true) }
            TransactionFilter.RECENT -> walletTransactions.value
            TransactionFilter.OLDEST -> walletTransactions.value.sortedBy { it.date }
            TransactionFilter.HIGHEST_AMOUNT -> walletTransactions.value.sortedByDescending { 
                it.amount.replace("-", "").toDoubleOrNull() ?: 0.0 
            }
            TransactionFilter.LOWEST_AMOUNT -> walletTransactions.value.sortedBy { 
                it.amount.replace("-", "").toDoubleOrNull() ?: 0.0 
            }
        }
        
        // Always sort by date in descending order except for explicit sorting filters
        when (currentFilter) {
            TransactionFilter.OLDEST -> sortedTransactions
            TransactionFilter.HIGHEST_AMOUNT, TransactionFilter.LOWEST_AMOUNT -> sortedTransactions
            else -> sortedTransactions.sortedByDescending { it.date }
        }
    }

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

    val targetHeight = if (expanded) 740.dp else 390.dp
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PaymentCardView(
                card = card,
                modifier = Modifier.size(width = 400.dp, height = 240.dp),
                backgroundGradient = availableCardColors.find { it.name == (if (showColorPicker) previewColor else card.background) }?.gradient 
                    ?: availableCardColors[0].gradient,
                isFocused = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Either show services with edit icon or color picker
            if (showColorPicker) {
                // Color Picker Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    val lazyListState = rememberLazyListState()
                    
                    LazyRow(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 72.dp), // Space for the confirm button
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(availableCardColors) { colorOption ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colorOption.gradient)
                                    .border(
                                        width = 2.dp,
                                        color = if (previewColor == colorOption.name) 
                                            Color(0xFFB297E7) 
                                        else 
                                            Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        previewColor = colorOption.name
                                        selectedColor = colorOption.name
                                    }
                            )
                        }
                    }

                    // Confirm button
                    FloatingActionButton(
                        onClick = {
                            walletViewModel.updateCardColor(card.accountNumber, selectedColor)
                            showColorPicker = false
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(48.dp),
                        containerColor = Color(0xFFB297E7),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm Color"
                        )
                    }
                }
            } else {
                // Service Actions Row with Edit Icon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Regular services
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

                    // Edit icon as part of services
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A1A1D))
                            .clickable { showColorPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_edit_square_24),
                            contentDescription = "Edit Card Color",
                            tint = Color(0xFFB297E7),
                            modifier = Modifier.size(28.dp)
                        )
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
                        .size(width = 55.dp, height = 6.dp)
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
                    // Left side: Filter button
                    FilterOptionsButton(
                        currentFilter = currentFilter,
                        onFilterSelected = { filter ->
                            currentFilter = filter
                        }
                    )

                    // Right side: Refresh and expand buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Refresh button
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

                        // Expand/Collapse button
                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (expanded)
                                        R.drawable.baseline_keyboard_double_arrow_down_24
                                    else
                                        R.drawable.baseline_keyboard_double_arrow_up_24
                                ),
                                contentDescription = "Toggle Sheet",
                                tint = Color(0xFFB297E7)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp)
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
                                    text = transactionError.value ?: "Error loading transactions",
                                    color = Color.Red,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else if (filteredTransactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    when (currentFilter) {
                                        TransactionFilter.ALL -> "No transactions found"
                                        TransactionFilter.DEPOSITS -> "No deposits found"
                                        TransactionFilter.WITHDRAWALS -> "No withdrawals found"
                                        TransactionFilter.TRANSFERS -> "No transfers found"
                                        else -> "No transactions found"
                                    },
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        items(filteredTransactions) { tx ->
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