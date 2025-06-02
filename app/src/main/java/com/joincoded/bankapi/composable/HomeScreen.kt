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

    LaunchedEffect(viewModel.accounts) {
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
            // Balances
            Text("Your Balances", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            LazyRow {
                items(userAccounts) { account ->
                    Card(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .width(160.dp)
                            .height(130.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(getCurrencyFlag(account.symbol), fontSize = 24.sp, modifier = Modifier.padding(bottom = 8.dp))
                            Text("${account.symbol} Account", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${account.balance} ${account.symbol}", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Transactions
            Text("Recent Transactions", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    transactions.take(5).forEach { txn ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
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
                                    Text(formatDateTime(txn.timeStamp.toString()), fontSize = 12.sp, color = AccentLight)
                                }

                                val amountColor = if (txn.amount > BigDecimal.ZERO) Color(0xFF4CAF50) else Color(0xFFF44336)

                                Text(
                                    text = "${txn.amount} ${txn.accountCurrency}",
                                    color = amountColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Quick Services
            Text("Quick Services", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            val services = listOf(
                ServiceAction("Transfer", Icons.Default.Send) { navController.navigate("transfer") },
                ServiceAction("Your Currencies", Icons.Default.Add) { },
                ServiceAction("Add Account", Icons.Default.Add) { }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                services.forEach { service ->
                    QuickServiceTile(
                        label = service.title,
                        icon = service.icon,
                        onClick = service.onClick,
                        modifier = Modifier.weight(1f)
                    )
                }
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

fun getCurrencyFlag(currency: String): String {
    return when (currency.uppercase()) {
        "KWD" -> "🇰🇼"
        "USD" -> "🇺🇸"
        "EUR" -> "🇪🇺"
        "GBP" -> "🇬🇧"
        "JPY" -> "🇯🇵"
        else -> "🌐"
    }
}


fun formatDateTime(isoString: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("MMM d • h:mm a")
        val dateTime = LocalDateTime.parse(isoString, inputFormatter)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        isoString // fallback if parsing fails
    }
}