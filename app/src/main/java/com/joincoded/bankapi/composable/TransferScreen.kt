package com.joincoded.bankapi.composable

import RoundTransferVerticalBoldIcon
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joincoded.bankapi.R
import com.joincoded.bankapi.viewmodel.WalletViewModel
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.PaymentCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.fragment.app.FragmentActivity
import androidx.activity.ComponentActivity
import android.content.ContextWrapper
import com.joincoded.bankapi.utils.BiometricManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState

// Add performTransfer as a private function outside the composable
private fun performTransfer(
    walletViewModel: WalletViewModel?,
    fromCard: PaymentCard,
    toCard: PaymentCard,
    amount: String,
    password: String,
    onAmountChanged: (String) -> Unit,
    onTransfer: (String) -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    walletCards: List<CardState>,
    currentCards: List<PaymentCard>,
    onSuccess: (String, String) -> Unit,
    onError: (String) -> Unit
) {
    val currentFromCard = fromCard // Store the current from card
    walletViewModel?.transfer(
        fromCard = currentFromCard,
        toCard = toCard,
        amount = amount,
        currency = currentFromCard.currency,
        onSuccess = {
            Log.d("TransferScreen", "🎉 Transfer successful callback received")
            val finalAmount = amount
            val finalCurrency = currentFromCard.currency
            onAmountChanged("")
            onSuccess(finalAmount, finalCurrency)
            
            // Refresh card data after successful transaction
            coroutineScope.launch {
                try {
                    Log.d("TransferScreen", "🔄 Refreshing card data after successful transfer")
                    walletViewModel.fetchUserCards()
                    
                    // Wait for cards to update
                    delay(500)
                    
                    // Update fromCard with fresh data
                    val updatedFromCard = walletCards.find { it.card.accountNumber == currentFromCard.accountNumber }?.card
                    if (updatedFromCard != null) {
                        Log.d("TransferScreen", "✅ Updated fromCard with fresh data: ${updatedFromCard.balance} ${updatedFromCard.currency}")
                    }
                    
                    // Update toCard with fresh data
                    val updatedToCard = walletCards.find { it.card.accountNumber == toCard.accountNumber }?.card
                    if (updatedToCard != null) {
                        Log.d("TransferScreen", "✅ Updated toCard with fresh data: ${updatedToCard.balance} ${updatedToCard.currency}")
                    }
                } catch (e: Exception) {
                    Log.e("TransferScreen", "❌ Error refreshing card data: ${e.message}", e)
                }
            }
            
            onTransfer(password)
        },
        onError = { error: String ->
            Log.e("TransferScreen", """
                ❌ Transfer failed with error: $error
                Details:
                - From Card Balance: ${currentFromCard.balance}
                - To Card Balance: ${toCard.balance}
                - Transfer Amount: $amount
                - Currency: ${currentFromCard.currency}
            """.trimIndent())
            
            val errorMessage = when {
                error.contains("insufficient balance") -> 
                    "Transfer failed due to insufficient balance.\n\n" +
                    "Current balance: ${currentFromCard.balance} ${currentFromCard.currency}\n" +
                    "Transfer amount: $amount ${currentFromCard.currency}\n" +
                    "Try using a slightly smaller amount."
                else -> "Transfer failed: $error"
            }
            onError(errorMessage)
        }
    )
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    cards: List<PaymentCard>,
    amount: String,
    onAmountChanged: (String) -> Unit,
    onBack: () -> Unit,
    onTransfer: (String) -> Unit,
    errorMessage: String? = null,
    walletViewModel: WalletViewModel? = null,
    initialFromCard: PaymentCard? = null
) {
    var showSuccessDialog by remember { mutableStateOf(false) }
    var modalScale by remember { mutableStateOf(0.8f) }
    var modalAlpha by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    var isBiometricAvailable by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var transferCurrency by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val context = LocalContext.current
    
    val activity = remember(context) {
        when (val ctx = context) {
            is ComponentActivity -> ctx
            is ContextWrapper -> {
                var currentContext = ctx.baseContext
                while (currentContext is ContextWrapper) {
                    if (currentContext is ComponentActivity) {
                        return@remember currentContext
                    }
                    currentContext = currentContext.baseContext
                }
                null
            }
            else -> null
        }
    }
    val biometricManager = remember { BiometricManager(context.applicationContext) }

    // Update card state handling
    val walletCardsState = walletViewModel?.cards?.collectAsStateWithLifecycle<List<CardState>>(initialValue = emptyList())
    val walletCards = walletCardsState?.value ?: emptyList()
    val currentCards: List<PaymentCard> = walletCards.map { it.card }

    // Use initialFromCard if provided, otherwise use first card
    var fromCardIndex by remember { mutableStateOf(0) }
    var toCardIndex by remember { mutableStateOf(1.coerceAtMost(currentCards.lastIndex)) }
    
    val fromCard: PaymentCard by remember(fromCardIndex, currentCards) {
        mutableStateOf(
            initialFromCard ?: currentCards.getOrNull(fromCardIndex) ?: cards.firstOrNull() 
                ?: throw IllegalStateException("No cards available")
        )
    }
    
    val toCard: PaymentCard by remember(toCardIndex, currentCards) {
        mutableStateOf(
            currentCards.getOrNull(toCardIndex) 
                ?: cards.firstOrNull { card -> card.accountNumber != fromCard.accountNumber } 
                ?: cards.firstOrNull() 
                ?: fromCard
        )
    }

    // Update card indices when walletCards changes
    LaunchedEffect(walletCards) {
        if (walletCards.isEmpty()) return@LaunchedEffect
        
        // Find the updated versions of our cards
        val updatedFromCard = walletCards.find { it.card.accountNumber == fromCard.accountNumber }?.card
        val updatedToCard = currentCards.getOrNull(toCardIndex)
        
        if (updatedFromCard != null) {
            fromCardIndex = currentCards.indexOf(updatedFromCard)
        }
        
        if (updatedToCard != null) {
            toCardIndex = currentCards.indexOf(updatedToCard)
        }
    }

    // Add LaunchedEffect to observe transaction updates
    LaunchedEffect(Unit) {
        walletViewModel?.fetchUserCards()
    }

    var isSwapping by remember { mutableStateOf(false) }
    val vibrator = context.getSystemService(Vibrator::class.java)

    // Add state for showing conversion info
    var showConversionInfo by remember { mutableStateOf(false) }
    var convertedAmount by remember { mutableStateOf<String?>(null) }

    // Update conversion info when amount or cards change
    LaunchedEffect(amount, fromCard, toCard) {
        if (amount.isNotEmpty() && amount.toDoubleOrNull() != null) {
            showConversionInfo = fromCard.currency != toCard.currency
            // The actual conversion will be handled by the backend
            // We just show a message that conversion will be applied
        } else {
            showConversionInfo = false
            convertedAmount = null
        }
    }

    // Add animation states for fingerprint icon
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isPressed) 5f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconRotation"
    )

    // Check biometric availability
    LaunchedEffect(Unit) {
        isBiometricAvailable = biometricManager.canAuthenticate()
        Log.d("TransferScreen", "Biometric availability: $isBiometricAvailable")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Color.Black, Color(0xFF000000), Color.Black)))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text("Transfer", color = Color(0xFFD1B4FF), fontSize = 28.sp, modifier = Modifier.padding(bottom = 4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.zIndex(0f)) {
                    AnimatedContent(
                        targetState = fromCard,
                        transitionSpec = {
                            if (isSwapping) {
                                (slideInVertically { it } + fadeIn()) with (slideOutVertically { it } + fadeOut())
                            } else {
                                (slideInHorizontally { it } + fadeIn()) with (slideOutHorizontally { -it } + fadeOut())
                            }
                        },
                        label = "FromCard"
                    ) { card ->
                        PaymentCardView(
                            card = card,
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            backgroundGradient = availableCardColors.find { it.name == card.background }?.gradient 
                                ?: availableCardColors[0].gradient
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    if (!isSwapping) {
                                        if (dragAmount < -40 && toCardIndex < currentCards.lastIndex) {
                                            toCardIndex = (toCardIndex + 1) % currentCards.size
                                            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                        } else if (dragAmount > 40 && toCardIndex > 0) {
                                            toCardIndex = (toCardIndex - 1 + currentCards.size) % currentCards.size
                                            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = toCard,
                            transitionSpec = {
                                if (isSwapping) {
                                    (slideInVertically { -it } + fadeIn()) with (slideOutVertically { -it } + fadeOut())
                                } else {
                                    (slideInHorizontally { it } + fadeIn()) with (slideOutHorizontally { -it } + fadeOut())
                                }
                            },
                            label = "ToCard"
                        ) { card ->
                            PaymentCardView(
                                card = card,
                                modifier = Modifier.fillMaxWidth().height(230.dp),
                                backgroundGradient = availableCardColors.find { it.name == card.background }?.gradient 
                                    ?: availableCardColors[0].gradient
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .zIndex(1f)
                        .background(Color(0xFF59516B), shape = RoundedCornerShape(32.dp))
                        .border(4.dp, Color.Black, shape = RoundedCornerShape(32.dp))
                        .clickable {
                            if (!isSwapping) {
                                coroutineScope.launch {
                                    isSwapping = true
                                    delay(300)
                                    val temp = fromCardIndex
                                    fromCardIndex = toCardIndex
                                    toCardIndex = temp
                                    delay(300)
                                    isSwapping = false
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    RoundTransferVerticalBoldIcon(modifier = Modifier.size(55.dp))
                }
            }

            // Amount input with currency conversion info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        // Only allow numbers and one decimal point
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onAmountChanged(newValue)
                        }
                    },
                    label = { Text("Amount", color = Color.White.copy(alpha = 0.7f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                        .height(56.dp),
                    prefix = { Text(fromCard.currency, color = Color.White.copy(alpha = 0.7f)) }
                )

                if (showConversionInfo) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Amount will be converted from ${fromCard.currency} to ${toCard.currency}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "The final amount will be calculated using current exchange rates",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Continue Button
            Button(
                onClick = { 
                    if (amount.isNotEmpty()) {
                        showPasswordDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB297E7),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFB297E7).copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = amount.isNotEmpty()
            ) {
                Text("Continue")
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    // Password Dialog
    if (showPasswordDialog) {
        Log.d("TransferScreen", "🔑 Password dialog visible")
        AlertDialog(
            onDismissRequest = { 
                Log.d("TransferScreen", "❌ Password dialog dismissed")
                showPasswordDialog = false 
            },
            title = { Text("Enter Password", color = Color.White) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isBiometricAvailable && !showBiometricPrompt) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .scale(scale)
                                .rotate(rotation)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    Log.d("TransferScreen", "Fingerprint icon clicked")
                                    showBiometricPrompt = true
                                    activity?.let { fragmentActivity ->
                                        if (fragmentActivity is FragmentActivity) {
                                            biometricManager.authenticate(
                                                activity = fragmentActivity,
                                                onSuccess = {
                                                    Log.d("TransferScreen", "Biometric authentication successful")
                                                    showBiometricPrompt = false
                                                    performTransfer(
                                                        walletViewModel = walletViewModel,
                                                        fromCard = fromCard,
                                                        toCard = toCard,
                                                        amount = amount,
                                                        password = password,
                                                        onAmountChanged = onAmountChanged,
                                                        onTransfer = onTransfer,
                                                        coroutineScope = coroutineScope,
                                                        walletCards = walletCards,
                                                        currentCards = currentCards,
                                                        onSuccess = { finalAmount, finalCurrency ->
                                                            transferAmount = finalAmount
                                                            transferCurrency = finalCurrency
                                                            showSuccessDialog = true
                                                        },
                                                        onError = { error ->
                                                            errorDialogMessage = error
                                                            showErrorDialog = true
                                                        }
                                                    )
                                                },
                                                onError = { error ->
                                                    Log.e("TransferScreen", "Biometric authentication error: $error")
                                                    showBiometricPrompt = false
                                                    errorDialogMessage = error
                                                    showErrorDialog = true
                                                },
                                                onFallback = {
                                                    Log.d("TransferScreen", "Falling back to password")
                                                    showBiometricPrompt = false
                                                    performTransfer(
                                                        walletViewModel = walletViewModel,
                                                        fromCard = fromCard,
                                                        toCard = toCard,
                                                        amount = amount,
                                                        password = password,
                                                        onAmountChanged = onAmountChanged,
                                                        onTransfer = onTransfer,
                                                        coroutineScope = coroutineScope,
                                                        walletCards = walletCards,
                                                        currentCards = currentCards,
                                                        onSuccess = { finalAmount, finalCurrency ->
                                                            transferAmount = finalAmount
                                                            transferCurrency = finalCurrency
                                                            showSuccessDialog = true
                                                        },
                                                        onError = { error ->
                                                            errorDialogMessage = error
                                                            showErrorDialog = true
                                                        }
                                                    )
                                                }
                                            )
                                        } else {
                                            Log.e("TransferScreen", "Activity is not a FragmentActivity: ${fragmentActivity.javaClass.name}")
                                            errorDialogMessage = "Unable to start biometric authentication"
                                            showErrorDialog = true
                                        }
                                    } ?: run {
                                        Log.e("TransferScreen", "Activity context is null")
                                        errorDialogMessage = "Unable to start biometric authentication"
                                        showErrorDialog = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_fingerprint_24),
                                contentDescription = "Authenticate with biometric",
                                tint = Color(0xFFB297E7),
                                modifier = Modifier.size(80.dp)
                            )
                        }

                        Text(
                            "Tap to authenticate with biometric",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            "or enter your password below",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password", color = Color.Gray) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF27272A),
                            unfocusedContainerColor = Color(0xFF27272A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d("TransferScreen", "✅ Confirm transfer clicked with password length: ${password.length}")
                        showPasswordDialog = false
                        if (walletViewModel != null) {
                            Log.d("TransferScreen", """
                                🚀 Initiating transfer with:
                                - From Card: ${fromCard.accountNumber} (${fromCard.currency})
                                - To Card: ${toCard.accountNumber} (${toCard.currency})
                                - Amount: $amount
                                - Currency: ${fromCard.currency}
                            """.trimIndent())
                            
                            try {
                                if (isBiometricAvailable) {
                                    showBiometricPrompt = true
                                    activity?.let { fragmentActivity ->
                                        if (fragmentActivity is FragmentActivity) {
                                            biometricManager.authenticate(
                                                activity = fragmentActivity,
                                                onSuccess = {
                                                    Log.d("TransferScreen", "Biometric authentication successful")
                                                    showBiometricPrompt = false
                                                    performTransfer(
                                                        walletViewModel = walletViewModel,
                                                        fromCard = fromCard,
                                                        toCard = toCard,
                                                        amount = amount,
                                                        password = password,
                                                        onAmountChanged = onAmountChanged,
                                                        onTransfer = onTransfer,
                                                        coroutineScope = coroutineScope,
                                                        walletCards = walletCards,
                                                        currentCards = currentCards,
                                                        onSuccess = { finalAmount, finalCurrency ->
                                                            transferAmount = finalAmount
                                                            transferCurrency = finalCurrency
                                                            showSuccessDialog = true
                                                        },
                                                        onError = { error ->
                                                            errorDialogMessage = error
                                                            showErrorDialog = true
                                                        }
                                                    )
                                                },
                                                onError = { error ->
                                                    Log.e("TransferScreen", "Biometric authentication error: $error")
                                                    showBiometricPrompt = false
                                                    errorDialogMessage = error
                                                    showErrorDialog = true
                                                },
                                                onFallback = {
                                                    Log.d("TransferScreen", "Falling back to password")
                                                    showBiometricPrompt = false
                                                    performTransfer(
                                                        walletViewModel = walletViewModel,
                                                        fromCard = fromCard,
                                                        toCard = toCard,
                                                        amount = amount,
                                                        password = password,
                                                        onAmountChanged = onAmountChanged,
                                                        onTransfer = onTransfer,
                                                        coroutineScope = coroutineScope,
                                                        walletCards = walletCards,
                                                        currentCards = currentCards,
                                                        onSuccess = { finalAmount, finalCurrency ->
                                                            transferAmount = finalAmount
                                                            transferCurrency = finalCurrency
                                                            showSuccessDialog = true
                                                        },
                                                        onError = { error ->
                                                            errorDialogMessage = error
                                                            showErrorDialog = true
                                                        }
                                                    )
                                                }
                                            )
                                        } else {
                                            Log.e("TransferScreen", "Activity is not a FragmentActivity: ${fragmentActivity.javaClass.name}")
                                            errorDialogMessage = "Unable to start biometric authentication"
                                            showErrorDialog = true
                                        }
                                    } ?: run {
                                        Log.e("TransferScreen", "Activity context is null")
                                        errorDialogMessage = "Unable to start biometric authentication"
                                        showErrorDialog = true
                                    }
                                } else {
                                    performTransfer(
                                        walletViewModel = walletViewModel,
                                        fromCard = fromCard,
                                        toCard = toCard,
                                        amount = amount,
                                        password = password,
                                        onAmountChanged = onAmountChanged,
                                        onTransfer = onTransfer,
                                        coroutineScope = coroutineScope,
                                        walletCards = walletCards,
                                        currentCards = currentCards,
                                        onSuccess = { finalAmount, finalCurrency ->
                                            transferAmount = finalAmount
                                            transferCurrency = finalCurrency
                                            showSuccessDialog = true
                                        },
                                        onError = { error ->
                                            errorDialogMessage = error
                                            showErrorDialog = true
                                        }
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("TransferScreen", "❌ Exception during transfer: ${e.message}", e)
                                errorDialogMessage = "An unexpected error occurred. Please try again."
                                showErrorDialog = true
                            }
                        } else {
                            Log.e("TransferScreen", "❌ walletViewModel is null, cannot perform transfer")
                            errorDialogMessage = "Unable to process transfer. Please try again."
                            showErrorDialog = true
                        }
                        password = ""
                    }
                ) {
                    Text("Confirm", color = Color(0xFFD1B4FF))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        password = ""
                    }
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1A1A1D)
        )
    }

    // Success Dialog
    if (showSuccessDialog) {
        Dialog(
            onDismissRequest = { 
                coroutineScope.launch {
                    modalScale = 0.8f
                    modalAlpha = 0f
                    delay(300)
                    showSuccessDialog = false
                    onBack()
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .scale(modalScale)
                    .alpha(modalAlpha)
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
                        val successScale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
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
                            "Transfer Successful!",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            if (fromCard.currency != toCard.currency) {
                                "Your transfer of $transferAmount $transferCurrency has been completed successfully.\n\n" +
                                "The amount was converted to ${toCard.currency} using current exchange rates.\n\n" +
                                "The recipient's account has been updated with the converted amount."
                            } else {
                                "Your transfer of $transferAmount $transferCurrency has been completed successfully.\n\n" +
                                "The recipient's account has been updated with the transferred amount."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        val buttonScale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "buttonScale"
                        )

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    modalScale = 0.8f
                                    modalAlpha = 0f
                                    delay(300)
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "You can close this window",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Transfer Failed", color = Color.White) },
            text = {
                Text(
                    errorDialogMessage,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("OK", color = Color(0xFFB297E7))
                }
            },
            containerColor = Color(0xFF1A1A1D),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Add BackHandler
    BackHandler {
        when {
            showSuccessDialog -> {
                // Don't allow back navigation when success dialog is showing
                Log.d("TransferScreen", "Back pressed while success dialog showing - ignoring")
            }
            showPasswordDialog -> {
                // Close password dialog on back press
                Log.d("TransferScreen", "Back pressed while password dialog showing - closing dialog")
                showPasswordDialog = false
            }
            showErrorDialog -> {
                // Close error dialog on back press
                Log.d("TransferScreen", "Back pressed while error dialog showing - closing dialog")
                showErrorDialog = false
            }
            else -> {
                // Normal back navigation
                Log.d("TransferScreen", "Back pressed - navigating back")
                onBack()
            }
        }
    }
} 