package com.joincoded.bankapi.composable

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.joincoded.bankapi.ViewModel.WalletViewModel
import kotlinx.coroutines.launch

data class AccountTheme(
    val name: String,
    val gradient: Brush,
    val countryCode: String,
    val currency: String
)

val accountThemes = listOf(
    AccountTheme(
        name = "AED",
        gradient = Brush.verticalGradient(listOf(Color(0xFF070709), Color(0xFF9E85CB))),
        countryCode = "AED",
        currency = "د.إ"
    ),
    AccountTheme(
        name = "KWD",
        gradient = Brush.verticalGradient(listOf(Color(0xFF352F3F), Color(0xFF000000))),
        countryCode = "KWD",
        currency = "د.ك"
    ),
    AccountTheme(
        name = "USD",
        gradient = Brush.verticalGradient(listOf(Color(0xFF423F4F), Color(0xFF2A282A))),
        countryCode = "USD",
        currency = "$"
    ),
    AccountTheme(
        name = "EUR",
        gradient = Brush.verticalGradient(listOf(Color(0xFF1D1B2C), Color(0xFF7B699B))),
        countryCode = "EUR",
        currency = "€"
    )
)

val accountTypes = listOf("Online", "Savings", "Business", "Checking", "Digital")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    onBack: () -> Unit,
    walletViewModel: WalletViewModel
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf(accountThemes[0]) }
    var selectedType by remember { mutableStateOf(accountTypes[0]) }
    var initialBalance by remember { mutableStateOf("10000") }
    val coroutineScope = rememberCoroutineScope()

    // Add BackHandler
    BackHandler {
        if (!isLoading && !showSuccessDialog) {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    "Create New Account",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                // Empty box for balance
                Box(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(selectedTheme.gradient)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        selectedType,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Column {
                        Text(
                            "Preview Card",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            "Balance: ${initialBalance} ${selectedTheme.currency}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Currency Selection
            Text(
                "Select Currency",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(accountThemes) { theme ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(theme.gradient)
                            .border(
                                width = 2.dp,
                                color = if (theme == selectedTheme) Color(0xFFB297E7) else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedTheme = theme },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            theme.name,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account Type Selection
            Text(
                "Select Account Type",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(accountTypes) { type ->
                    FilterChip(
                        selected = type == selectedType,
                        onClick = { selectedType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFB297E7),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Initial Balance Input
            OutlinedTextField(
                value = initialBalance,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        initialBalance = it
                    }
                },
                label = { Text("Initial Balance", color = Color.White.copy(alpha = 0.7f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFB297E7),
                    focusedContainerColor = Color(0xFF27272A),
                    unfocusedContainerColor = Color(0xFF27272A)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                prefix = { Text(selectedTheme.currency, color = Color.White.copy(alpha = 0.7f)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    errorMessage!!,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Create Account Button
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    walletViewModel.createAccount(
                        initialBalance = initialBalance,
                        countryCode = selectedTheme.countryCode,
                        accountType = selectedType,
                        onSuccess = {
                            showSuccessDialog = true
                            isLoading = false
                        },
                        onError = { error ->
                            errorMessage = error
                            isLoading = false
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB297E7),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFB297E7).copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                enabled = !isLoading && initialBalance.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Account")
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        Dialog(
            onDismissRequest = { 
                coroutineScope.launch {
                    showSuccessDialog = false
                    onBack()
                }
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1D)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Success icon with animation
                    val successScale by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = 0.7f,
                            stiffness = 300f
                        ),
                        label = "successScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(successScale)
                            .background(Color(0xFF2A2A2D), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color.Green,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        "Account Created!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Your new ${selectedType} account has been created successfully.\n\n" +
                        "Initial Balance: ${initialBalance} ${selectedTheme.currency}\n" +
                        "Currency: ${selectedTheme.name}\n\n" +
                        "The account has been added to your wallet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Done button with animation
                    val buttonScale by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = 0.7f,
                            stiffness = 300f
                        ),
                        label = "buttonScale"
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                showSuccessDialog = false
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .scale(buttonScale),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB297E7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            "Done",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
} 