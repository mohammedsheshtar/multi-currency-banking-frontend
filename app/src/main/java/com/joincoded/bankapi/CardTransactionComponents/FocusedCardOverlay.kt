package com.joincoded.bankapi.CardTransactionComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.Transaction
import com.joincoded.bankapi.data.TransactionFilter
import com.joincoded.bankapi.data.TransactionType
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun FocusedCardOverlay(
    focusedCard: CardState,
    onClose: () -> Unit,
    onTransfer: () -> Unit,
    onTransferToUser: () -> Unit,
    onCloseAccount: () -> Unit,
    transactions: List<Transaction>,
    onFilterTransactions: (TransactionFilter) -> Unit
) {
    var isTransactionsExpanded by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isTransactionsExpanded) 180f else 0f,
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Card Details",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Filter",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Services Section
            Text(
                text = "Services",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ServiceButton(
                    icon = Icons.Default.Person,
                    label = "Transfer to User",
                    onClick = onTransferToUser
                )
                ServiceButton(
                    icon = Icons.Default.Close,
                    label = "Close Account",
                    onClick = onCloseAccount
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Transactions Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isTransactionsExpanded = !isTransactionsExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = Color.White,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            AnimatedVisibility(
                visible = isTransactionsExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(transaction = transaction)
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            onDismiss = { showFilterDialog = false },
            onFilterSelected = { filter ->
                onFilterTransactions(filter)
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun ServiceButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val isDeposit = transaction.type == TransactionType.DEPOSIT
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = transaction.description,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = transaction.timestamp.format(dateFormatter),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        Text(
            text = "${if (isDeposit) "+" else "-"} ${transaction.amount}",
            color = if (isDeposit) Color(0xFF4CAF50) else Color(0xFFE57373),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onFilterSelected: (TransactionFilter) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<TransactionFilter?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Transactions") },
        text = {
            Column {
                FilterOption(
                    text = "All",
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null }
                )
                FilterOption(
                    text = "Deposits",
                    selected = selectedFilter == TransactionFilter.DEPOSITS,
                    onClick = { selectedFilter = TransactionFilter.DEPOSITS }
                )
                FilterOption(
                    text = "Withdrawals",
                    selected = selectedFilter == TransactionFilter.WITHDRAWALS,
                    onClick = { selectedFilter = TransactionFilter.WITHDRAWALS }
                )
                FilterOption(
                    text = "Transfers",
                    selected = selectedFilter == TransactionFilter.TRANSFERS,
                    onClick = { selectedFilter = TransactionFilter.TRANSFERS }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedFilter?.let { onFilterSelected(it) }
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FilterOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
} 