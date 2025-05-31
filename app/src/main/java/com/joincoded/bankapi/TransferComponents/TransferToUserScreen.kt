package com.joincoded.bankapi.TransferComponents

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.TransferUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferToUserScreen(
    fromCard: PaymentCard,
    availableCards: List<PaymentCard>,
    onBack: () -> Unit,
    onSearchUser: (String) -> Unit,
    onTransfer: (PaymentCard, TransferUser, Double) -> Unit,
    searchedUser: TransferUser?,
    isLoading: Boolean,
    error: String?,
    isTransferSuccess: Boolean = false
) {
    var amount by remember { mutableStateOf("") }
    var selectedCard by remember { mutableStateOf<PaymentCard?>(null) }
    var phoneNumber by remember { mutableStateOf("") }
    var showAmountDialog by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1D))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Transfer to User",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Success Message
        AnimatedVisibility(
            visible = isTransferSuccess,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transfer successful!",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Phone Number Search
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { 
                phoneNumber = it
                if (it.length == 10) {
                    onSearchUser(it)
                }
            },
            label = { Text("Phone Number", color = Color.White) },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (phoneNumber.isNotEmpty()) {
                    IconButton(onClick = { phoneNumber = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.White
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // User Search Result
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color.White
            )
        } else if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else if (searchedUser != null) {
            UserCard(
                user = searchedUser,
                onClick = { showAmountDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Available Cards
        Text(
            text = "Select Account to Transfer From",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availableCards) { card ->
                CardItem(
                    card = card,
                    isSelected = card == selectedCard,
                    onClick = { selectedCard = card }
                )
            }
        }
    }

    if (showAmountDialog && searchedUser != null && selectedCard != null) {
        AlertDialog(
            onDismissRequest = { showAmountDialog = false },
            title = { Text("Enter Amount") },
            text = {
                Column {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            amount = it
                            amountError = validateAmount(it, selectedCard!!.balance)
                        },
                        label = { Text("Amount") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        isError = amountError != null
                    )
                    if (amountError != null) {
                        Text(
                            text = amountError!!,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        text = "Available Balance: ${selectedCard!!.currency} ${selectedCard!!.balance}",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        amount.toDoubleOrNull()?.let { amountValue ->
                            if (validateAmount(amount, selectedCard!!.balance) == null) {
                                onTransfer(selectedCard!!, searchedUser, amountValue)
                                showAmountDialog = false
                            }
                        }
                    },
                    enabled = amountError == null && amount.isNotEmpty()
                ) {
                    Text("Transfer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAmountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun UserCard(
    user: TransferUser,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2C)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = user.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.email,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.phoneNumber,
                color = Color.Gray,
                fontSize = 14.sp
            )
            if (user.kycVerified) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "KYC Verified",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "KYC Verified",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CardItem(
    card: PaymentCard,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFD1B4FF) else Color(0xFF2C2C2C)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.type,
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${card.currency} ${card.balance}",
                    color = if (isSelected) Color.Black else Color.Gray,
                    fontSize = 14.sp
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color.Black
                )
            }
        }
    }
}

private fun validateAmount(amount: String, availableBalance: Double): String? {
    if (amount.isEmpty()) return null
    val amountValue = amount.toDoubleOrNull()
    return when {
        amountValue == null -> "Please enter a valid amount"
        amountValue <= 0 -> "Amount must be greater than 0"
        amountValue > availableBalance -> "Insufficient balance"
        else -> null
    }
} 