package com.joincoded.bankapi.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.joincoded.bankapi.ui.theme.*
import com.joincoded.bankapi.viewmodel.HomeViewModel
import com.joincoded.bankapi.viewmodel.ShopViewModel
import com.joincoded.bankapi.viewmodel.WalletViewModel
import java.math.BigDecimal
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.joincoded.bankapi.data.TransactionItem
import com.joincoded.bankapi.data.response.ListAccountResponse
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    shopViewModel: ShopViewModel,
    walletViewModel: WalletViewModel
) {
    val userName by homeViewModel.userName.collectAsState()
    val accounts by homeViewModel.accounts.collectAsState()
    val transactions by homeViewModel.transactions.collectAsState()
    val errorMessage by homeViewModel.error.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.refreshData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Welcome, $userName",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextLight
                )

                IconButton(
                    onClick = { homeViewModel.refreshData() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(CardDark, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Accent
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Accent
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Your Accounts",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextLight,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(accounts) { account ->
                        AccountCard(account = account)
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextLight,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(transactions) { transaction ->
                        TransactionItem(transaction = transaction)
                    }
                }
            }
        }

        errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(
                    text = error,
                    color = TextLight
                )
            }
        }
    }
}

@Composable
fun AccountCard(account: ListAccountResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = account.accountType,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight
                    )
                    Text(
                        text = account.accountNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLight.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "${account.balance} ${account.symbol}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Accent
                )
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight
                    )
                    Text(
                        text = "Account: ${transaction.cardId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = transaction.amount,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Accent
                )
            }
            Text(
                text = transaction.date,
                style = MaterialTheme.typography.bodySmall,
                color = TextLight.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun getCurrencyFlag(currencyCode: String?): String {
    if (currencyCode.isNullOrBlank()) return "🌐"

    val currencyToCountryCode = mapOf(
        "USD" to "US",
        "KWD" to "KW",
        "EUR" to "EU",
        "GBP" to "GB",
        "JPY" to "JP",
        "CAD" to "CA",
        "AUD" to "AU",
        "INR" to "IN",
        "CNY" to "CN",
        "CHF" to "CH"
    )

    val countryCode = currencyToCountryCode[currencyCode.uppercase()] ?: currencyCode.take(2).uppercase()

    return if (countryCode.length == 2) {
        val first = Character.codePointAt(countryCode, 0) - 'A'.code + 0x1F1E6
        val second = Character.codePointAt(countryCode, 1) - 'A'.code + 0x1F1E6
        String(Character.toChars(first)) + String(Character.toChars(second))
    } else {
        "🌐"
    }
}

@Composable
fun TransactionCard(txn: TransactionHistoryResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(txn.transactionType, color = TextLight, fontWeight = FontWeight.SemiBold)

                Text(
                    text = "Account: ${txn.accountNumber}",
                    fontSize = 12.sp,
                    color = Accent
                )

                Text(
                    formatDateTime(txn.timeStamp.toString()),
                    fontSize = 12.sp,
                    color = AccentLight
                )
            }

            val amountColor = when {
                txn.transactionType.contains("fee", ignoreCase = true) -> Color.Red
                txn.amount > BigDecimal.ZERO -> Color(0xFF4CAF50)
                else -> Color(0xFFF44336)
            }

            Text(
                text = String.format("%,.2f %s", txn.amount, txn.requestedCurrency),
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun formatDateTime(isoString: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("MMM d • h:mm a")
        val dateTime = LocalDateTime.parse(isoString, inputFormatter)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        isoString
    }
}
