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
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.joincoded.bankapi.viewmodel.WalletViewModel
import kotlinx.coroutines.launch

data class CurrencyOption(
    val code: String,      // ISO currency code (e.g., "USD")
    val symbol: String,    // Currency symbol (e.g., "$")
    val name: String       // Full currency name (e.g., "US Dollar")
)

data class CardColorOption(
    val name: String,      // Color name (e.g., "Purple Gradient")
    val gradient: Brush    // Card gradient for preview
)

val availableCurrencies = listOf(
    // Middle Eastern Currencies
    CurrencyOption("AED", "د.إ", "UAE Dirham"),
    CurrencyOption("KWD", "د.ك", "Kuwaiti Dinar"),
    CurrencyOption("SAR", "ر.س", "Saudi Riyal"),
    CurrencyOption("QAR", "ر.ق", "Qatari Riyal"),
    CurrencyOption("OMR", "ر.ع", "Omani Rial"),
    CurrencyOption("BHD", "د.ب", "Bahraini Dinar"),
    CurrencyOption("EGP", "ج.م", "Egyptian Pound"),
    
    // Major Global Currencies
    CurrencyOption("USD", "$", "US Dollar"),
    CurrencyOption("EUR", "€", "Euro"),
    CurrencyOption("GBP", "£", "British Pound"),
    CurrencyOption("JPY", "¥", "Japanese Yen"),
    CurrencyOption("CNY", "元", "Chinese Yuan"),
    
    // Asian Currencies
    CurrencyOption("INR", "₹", "Indian Rupee"),
    CurrencyOption("KRW", "₩", "South Korean Won"),
    CurrencyOption("SGD", "S$", "Singapore Dollar"),
    CurrencyOption("MYR", "RM", "Malaysian Ringgit"),
    
    // Other Major Currencies
    CurrencyOption("AUD", "A$", "Australian Dollar"),
    CurrencyOption("CAD", "C$", "Canadian Dollar"),
    CurrencyOption("CHF", "₣", "Swiss Franc"),
    CurrencyOption("NZD", "NZ$", "New Zealand Dollar")
)

val availableCardColors = listOf(
    CardColorOption("Purple Gradient", Brush.verticalGradient(listOf(Color(0xFF070709), Color(0xFF9E85CB)))),
    CardColorOption("Dark Purple", Brush.verticalGradient(listOf(Color(0xFF352F3F), Color(0xFF000000)))),
    CardColorOption("Lavendor Gradient", Brush.verticalGradient(listOf(Color(0xFF9E85CB), Color(0xFF070709)))),

    CardColorOption("Deep Purple", Brush.verticalGradient(listOf(Color(0xFF1D1B2C), Color(0xFF7B699B)))),
    CardColorOption("Dark Gray", Brush.verticalGradient(listOf(Color(0xFF15101C), Color(0xFF53426B)))),
    CardColorOption("Royal Purple", Brush.verticalGradient(listOf(Color(0xFF2D273B), Color(
        0xFFDDCFEF
    )
    ))),
    CardColorOption("Drak", Brush.verticalGradient(listOf(Color(0xFF19161E), Color(0xFF5C4F62)))),

            CardColorOption("Lavender", Brush.verticalGradient(listOf(Color(0xFF382F44), Color(0xFFF4F4F6)))),
    CardColorOption("Mauve", Brush.verticalGradient(listOf(Color(0xFFA882F6), Color(0xFFDFCAE7))))
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
    var selectedCurrency by remember { mutableStateOf(availableCurrencies[0]) }
    var selectedCardColor by remember { mutableStateOf(availableCardColors[0]) }
    var selectedType by remember { mutableStateOf(accountTypes[0]) }
    var initialBalance by remember { mutableStateOf("10000") }
    var showCurrencyDropdown by remember { mutableStateOf(false) }
    var showAccountTypeDropdown by remember { mutableStateOf(false) }
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
                    .background(selectedCardColor.gradient)
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
                            "Balance: ${initialBalance} ${selectedCurrency.symbol}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Type and Currency Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Account Type Selection
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Select Account Type",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedType,
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showAccountTypeDropdown = true }) {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Account Type",
                                        tint = Color.White
                                    )
                                }
                            },
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
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = showAccountTypeDropdown,
                            onDismissRequest = { showAccountTypeDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color(0xFF27272A))
                        ) {
                            accountTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            type,
                                            color = Color.White
                                        )
                                    },
                                    onClick = {
                                        selectedType = type
                                        showAccountTypeDropdown = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = Color.White,
                                        leadingIconColor = Color.White,
                                        trailingIconColor = Color.White,
                                        disabledTextColor = Color.Gray,
                                        disabledLeadingIconColor = Color.Gray,
                                        disabledTrailingIconColor = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                }

                // Currency Selection Dropdown
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Select Currency",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCurrency.code,
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showCurrencyDropdown = true }) {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Currency",
                                        tint = Color.White
                                    )
                                }
                            },
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
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = showCurrencyDropdown,
                            onDismissRequest = { showCurrencyDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color(0xFF27272A))
                        ) {
                            availableCurrencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "${currency.code} - ${currency.name}",
                                                color = Color.White
                                            )
                                            Text(
                                                currency.symbol,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedCurrency = currency
                                        showCurrencyDropdown = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = Color.White,
                                        leadingIconColor = Color.White,
                                        trailingIconColor = Color.White,
                                        disabledTextColor = Color.Gray,
                                        disabledLeadingIconColor = Color.Gray,
                                        disabledTrailingIconColor = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Selection
            Text(
                "Select Card Color",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableCardColors) { color ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color.gradient)
                            .border(
                                width = 2.dp,
                                color = if (color == selectedCardColor) Color(0xFFB297E7) else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedCardColor = color }
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
                prefix = { Text(selectedCurrency.symbol, color = Color.White.copy(alpha = 0.7f)) },
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
                        countryCode = selectedCurrency.code,
                        accountType = selectedType,
                        cardColor = selectedCardColor.name,
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
                        "Initial Balance: ${initialBalance} ${selectedCurrency.symbol}\n" +
                        "Currency: ${selectedCurrency.code}\n" +
                        "Card Color: ${selectedCardColor.name}\n\n" +
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