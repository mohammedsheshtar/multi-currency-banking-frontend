package com.joincoded.bankapi.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.data.response.ShopTransactionResponse
import com.joincoded.bankapi.viewmodel.ShopHistoryViewModel
import java.time.format.DateTimeFormatter

@Composable
fun ShopHistoryScreen(viewModel: ShopHistoryViewModel, token: String) {
    val transactions by viewModel.transactions.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(token) {
        println("⚡ token received in ShopHistoryScreen: $token")
        viewModel.token = token // ✅ just the raw token
        viewModel.fetchHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Purchase History", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        when {
            error != null -> {
                Text("Error: $error", color = Color.Red)
            }
            transactions.isEmpty() -> {
                Text("You haven't purchased anything yet.")
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(transactions) { txn ->
                        ShopHistoryCard(txn)
                    }
                }
            }
        }
    }
}


@Composable
fun ShopHistoryCard(txn: ShopTransactionResponse) {
    val formattedDate = try {
        val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("MMM d • h:mm a")
        val parsedDate = java.time.LocalDateTime.parse(txn.timeOfTransaction, inputFormatter)
        parsedDate.format(outputFormatter)
    } catch (e: Exception) {
        txn.timeOfTransaction
    }

    // Define color by tier
    val tierColor = when (txn.itemTier.uppercase()) {
        "BRONZE" -> Color(0xFFCD7F32)
        "SILVER" -> Color(0xFFC0C0C0)
        "GOLD" -> Color(0xFFFFD700)
        "PLATINUM" -> Color(0xFFB0E0E6)
        "DIAMOND" -> Color(0xFF00BFFF)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left icon
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Tier Icon",
                tint = tierColor,
                modifier = Modifier.size(36.dp)
            )

            // Center content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = txn.itemName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "${txn.itemTier} • -${txn.pointsSpent} pts",
                    color = tierColor,
                    fontSize = 12.sp
                )

                Text(
                    text = formattedDate,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            // Right content
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "From Tier:",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = txn.accountTier,
                    color = tierColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

