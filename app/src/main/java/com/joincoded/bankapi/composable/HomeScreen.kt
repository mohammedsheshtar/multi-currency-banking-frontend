package com.joincoded.bankapi.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.joincoded.bankapi.ui.theme.Accent
import com.joincoded.bankapi.ui.theme.AccentLight
import com.joincoded.bankapi.ui.theme.CardDark
import com.joincoded.bankapi.ui.theme.DarkBackground
import com.joincoded.bankapi.ui.theme.TextLight
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.math.BigDecimal
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.data.response.KYCResponse
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomePage(navController: NavController, viewModel: BankViewModel = viewModel()) {
    val userAccounts = viewModel.accounts
    val transactions = viewModel.transactions
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(Unit) {
        viewModel.getAccounts()
    }

    LaunchedEffect(userAccounts) {
        if (userAccounts.isNotEmpty()) {
            viewModel.getAllTransactionsForUserAccounts()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        if (errorMessage != null && userAccounts.isEmpty()) {
            Text(errorMessage!!, color = Color.Red)
        }

        if (userAccounts.isEmpty()) {
            Text("Loading data...", color = Accent)
        } else {
            Text("Hello, ${viewModel.userName ?: "Xchanger"}!", color = Accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Your Balances", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(userAccounts) { account ->
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .height(130.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(getCurrencyFlag(account.countryCode), fontSize = 24.sp, modifier = Modifier.padding(bottom = 8.dp))
                            Text("${account.countryCode} Account", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%,.2f %s", account.balance, account.symbol),
                                color = TextLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text("Recent Transactions", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(transactions.take(10)) { txn ->
                        TransactionCard(txn)
                    }
                }
            }
        }

        Text("Quick Services", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        val services = listOf(
            ServiceAction("Transfer", Icons.Default.Send) { navController.navigate("transfer") },
            ServiceAction("Shop", Icons.Default.Add) { navController.navigate("shopScreen")},
            ServiceAction("ShopHist", Icons.Default.Add) { navController.navigate("shopHistory")}
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            services.forEach { service ->
                QuickServiceTile(
                    label = service.title,
                    icon = service.icon,
                    onClick = service.onClick,
                    modifier = Modifier.size(90.dp)
                )
            }
        }
    }
}

@Composable
fun QuickServiceTile(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(horizontal = 4.dp)
            .background(CardDark, shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = Accent, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = TextLight, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

data class ServiceAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

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
                Text(txn.transactionType, color = TextLight)
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
                text = String.format("%,.2f %s", txn.amount, txn.accountCurrency),
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


