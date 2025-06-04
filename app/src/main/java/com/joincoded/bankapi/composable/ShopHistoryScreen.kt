package com.joincoded.bankapi.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.material3.TextFieldDefaults
import com.joincoded.bankapi.ui.theme.Accent

@Composable
fun ShopHistoryScreen(viewModel: ShopHistoryViewModel, token: String) {
    val transactions by viewModel.transactions.collectAsState()
    val error by viewModel.error.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("Newest First") }

    LaunchedEffect(token) {
        viewModel.token = token
        viewModel.fetchHistory()
    }

    val sortedTransactions = remember(transactions, sortOption) {
        when (sortOption) {
            "Newest First" -> transactions.sortedByDescending { it.purchasedAt }
            "Oldest First" -> transactions.sortedBy { it.purchasedAt }
            "Least Points" -> transactions.sortedBy { it.pointCost }
            "Most Points" -> transactions.sortedByDescending { it.pointCost }
            else -> transactions
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "X-Claimed",
            color = Accent,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val menuWidth = maxWidth

            Column {
                TextField(
                    value = sortOption,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown icon",
                                tint = Color.LightGray
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        disabledContainerColor = Color(0xFF2A2A2A),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                        cursorColor = Color.White
                    )
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(menuWidth)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A2A2A))
                ) {
                    listOf("Newest First", "Oldest First", "Least Points", "Most Points").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = Color.White) },
                            onClick = {
                                sortOption = option
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            error != null -> {
                Text("Error: $error", color = Color.Red)
            }
            sortedTransactions.isEmpty() -> {
                Text("You haven't purchased anything yet.")
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sortedTransactions) { txn ->
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
        val outputFormatter = DateTimeFormatter.ofPattern("MMM d • h:mm a")
        txn.purchasedAt.format(outputFormatter)
    } catch (e: Exception) {
        txn.purchasedAt.toString()
    }

    val tierColor = when (txn.tierName.uppercase()) {
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
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Tier Icon",
                tint = tierColor,
                modifier = Modifier.size(36.dp)
            )

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
                    text = "${txn.tierName} • -${txn.pointCost} pts",
                    color = tierColor,
                    fontSize = 12.sp
                )

                Text(
                    text = formattedDate,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Updated Points",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = "${txn.updatedPoints}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}