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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.joincoded.bankapi.ViewModel.WalletViewModel
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.PaymentCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    var swipeDirection by remember { mutableStateOf(0) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    // Add BackHandler
    BackHandler {
        when {
            showSuccessDialog -> {
                // Don't allow back navigation when success dialog is showing
                // User must use the "Done" button
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

    // Debug log for initial composition
    Log.d("TransferScreen", "🎯 Component composed - showSuccessDialog: $showSuccessDialog")

    // Animation for modal appearance
    LaunchedEffect(showSuccessDialog) {
        Log.d("TransferScreen", "🔄 LaunchedEffect triggered - showSuccessDialog changed to: $showSuccessDialog")
        if (showSuccessDialog) {
            Log.d("TransferScreen", "📈 Setting modal animation values to visible")
            modalScale = 1f
            modalAlpha = 1f
        } else {
            Log.d("TransferScreen", "📉 Setting modal animation values to hidden")
            modalScale = 0.8f
            modalAlpha = 0f
        }
    }

    // Observe the cards state from WalletViewModel with explicit type
    val walletCardsState = walletViewModel?.cards?.collectAsStateWithLifecycle<List<CardState>>(initialValue = emptyList())
    val walletCards = walletCardsState?.value ?: emptyList()
    val currentCards: List<PaymentCard> = walletCards.map { it.card }

    // Use initialFromCard if provided, otherwise use first card
    var fromCard: PaymentCard by remember { 
        mutableStateOf(
            initialFromCard ?: currentCards.firstOrNull() ?: cards.firstOrNull() 
                ?: throw IllegalStateException("No cards available")
        )
    }
    
    // Set initial toCard to be different from fromCard
    var currentToIndex: Int by remember { 
        mutableStateOf(
            currentCards.indexOfFirst { card -> card.accountNumber != fromCard.accountNumber }.coerceAtLeast(0)
        )
    }
    
    // Update fromCard and toCard when walletCards changes
    LaunchedEffect(walletCards) {
        // Find the updated versions of our cards
        val updatedFromCard = walletCards.find { it.card.accountNumber == fromCard.accountNumber }?.card
        val updatedToCard = currentCards.getOrNull(currentToIndex)
        
        if (updatedFromCard != null) {
            fromCard = updatedFromCard
        }
        
        if (updatedToCard != null) {
            currentToIndex = currentCards.indexOf(updatedToCard)
        }
    }
    
    val toCard: PaymentCard = currentCards.getOrNull(currentToIndex) 
        ?: cards.firstOrNull { card -> card.accountNumber != fromCard.accountNumber } 
        ?: cards.firstOrNull() 
        ?: fromCard

    var isSwapping: Boolean by remember { mutableStateOf(false) }

    var password: String by remember { mutableStateOf("") }

    var transferAmount by remember { mutableStateOf("") }
    var transferCurrency by remember { mutableStateOf("") }

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

    var errorDialogMessage by remember { mutableStateOf("") }

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
                            backgroundGradient = Brush.verticalGradient(listOf(Color(0xFF5E5280), Color.Black))
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
                                        if (dragAmount < -40) {
                                            swipeDirection = -1 // Swipe left
                                            currentToIndex = (currentToIndex + 1) % currentCards.size
                                        } else if (dragAmount > 40) {
                                            swipeDirection = 1 // Swipe right
                                            currentToIndex = (currentToIndex - 1 + currentCards.size) % currentCards.size
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
                                    // Match animation direction with swipe direction
                                    if (swipeDirection < 0) {
                                        // Swipe left: new card comes from right, old card goes left
                                        (slideInHorizontally { it } + fadeIn()) with (slideOutHorizontally { -it } + fadeOut())
                                    } else {
                                        // Swipe right: new card comes from left, old card goes right
                                        (slideInHorizontally { -it } + fadeIn()) with (slideOutHorizontally { it } + fadeOut())
                                    }
                                }
                            },
                            label = "ToCard"
                        ) { card ->
                            PaymentCardView(
                                card = card,
                                modifier = Modifier.fillMaxWidth().height(230.dp),
                                backgroundGradient = Brush.verticalGradient(listOf(Color.DarkGray, Color.Black))
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
                                    val temp = fromCard
                                    fromCard = toCard
                                    currentToIndex = currentCards.indexOf(temp).coerceAtLeast(0)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = onAmountChanged,
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
                            .weight(1f)
                            .height(56.dp),
                        prefix = { Text(fromCard.currency, color = Color.White.copy(alpha = 0.7f)) }
                    )

                    // Transfer All button
                    Button(
                        onClick = { 
                            // Calculate 99.9% of the balance to ensure transfer goes through
                            val originalBalance = fromCard.balance
                            val transferAmount = (originalBalance * 0.999).toString()
                            
                            Log.d("TransferScreen", """
                                💰 Transfer All calculation:
                                - Original balance: $originalBalance
                                - Transfer amount (99.9%): $transferAmount
                                - Difference: ${originalBalance - transferAmount.toDoubleOrNull()!!}
                            """.trimIndent())
                            
                            onAmountChanged(transferAmount)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF27272A),
                            contentColor = Color(0xFFB297E7)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .padding(start = 4.dp)
                    ) {
                        Text(
                            "All",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

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

    if (showPasswordDialog) {
        Log.d("TransferScreen", "🔑 Password dialog visible")
        AlertDialog(
            onDismissRequest = { 
                Log.d("TransferScreen", "❌ Password dialog dismissed")
                showPasswordDialog = false 
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
                                walletViewModel.transfer(
                                    fromCard = fromCard,
                                    toCard = toCard,
                                    amount = amount,
                                    currency = fromCard.currency,
                                    onSuccess = {
                                        Log.d("TransferScreen", "🎉 Transfer successful callback received")
                                        val finalAmount = amount
                                        val finalCurrency = fromCard.currency
                                        onAmountChanged("")
                                        showSuccessDialog = true
                                        transferAmount = finalAmount
                                        transferCurrency = finalCurrency
                                        onTransfer(password)
                                    },
                                    onError = { error: String ->
                                        Log.e("TransferScreen", """
                                            ❌ Transfer failed with error: $error
                                            Details:
                                            - From Card Balance: ${fromCard.balance}
                                            - To Card Balance: ${toCard.balance}
                                            - Transfer Amount: $amount
                                            - Currency: ${fromCard.currency}
                                        """.trimIndent())
                                        
                                        // Show error dialog with a more user-friendly message
                                        errorDialogMessage = when {
                                            error.contains("insufficient balance") -> 
                                                "Transfer failed due to insufficient balance.\n\n" +
                                                "Current balance: ${fromCard.balance} ${fromCard.currency}\n" +
                                                "Transfer amount: $amount ${fromCard.currency}\n" +
                                                "Try using a slightly smaller amount."
                                            else -> "Transfer failed: $error"
                                        }
                                        showErrorDialog = true
                                    }
                                )
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
            title = { Text("Enter Password", color = Color.White) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
            containerColor = Color(0xFF1A1A1D)
        )
    }

    // Success Dialog
    if (showSuccessDialog) {
        Log.d("TransferScreen", "🎯 About to render success dialog - showSuccessDialog: $showSuccessDialog, amount: $transferAmount $transferCurrency")
        Dialog(
            onDismissRequest = { 
                Log.d("TransferScreen", "❌ Success dialog dismissed")
                coroutineScope.launch {
                    // First animate out
                    modalScale = 0.8f
                    modalAlpha = 0f
                    Log.d("TransferScreen", "⏳ Waiting for animation to complete before dismissing")
                    delay(300) // Wait for animation to complete
                    showSuccessDialog = false
                    Log.d("TransferScreen", "✅ Animation complete, calling onBack")
                    onBack()
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Log.d("TransferScreen", "🎨 Rendering success dialog content")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .scale(modalScale)
                    .alpha(modalAlpha)
            ) {
                Log.d("TransferScreen", "📊 Current animation values - scale: $modalScale, alpha: $modalAlpha")
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

                        // Close button with animation
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
                                    // First animate out
                                    modalScale = 0.8f
                                    modalAlpha = 0f
                                    Log.d("TransferScreen", "⏳ Waiting for animation to complete before dismissing")
                                    delay(300) // Wait for animation to complete
                                    showSuccessDialog = false
                                    Log.d("TransferScreen", "✅ Animation complete, calling onBack")
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
    } else {
        Log.d("TransferScreen", "❌ Success dialog not showing - showSuccessDialog: $showSuccessDialog")
    }

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
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TransferCardSelector(
    cards: List<PaymentCard>,
    modifier: Modifier = Modifier,
    onSwapCards: (PaymentCard, PaymentCard) -> Unit
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Vibrator::class.java)

    var fromCardIndex by remember { mutableStateOf(0) }
    var toCardIndex by remember { mutableStateOf(1.coerceAtMost(cards.lastIndex)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -40 && fromCardIndex < cards.lastIndex) {
                        fromCardIndex++
                        toCardIndex = (fromCardIndex + 1).coerceAtMost(cards.lastIndex)
                        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else if (dragAmount > 40 && fromCardIndex > 0) {
                        fromCardIndex--
                        toCardIndex = (fromCardIndex + 1).coerceAtMost(cards.lastIndex)
                        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = cards[fromCardIndex],
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() with slideOutHorizontally { -it } + fadeOut()
            },
            label = "Transfer Card Animation"
        ) { card ->
            PaymentCardView(
                card = card,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1.6f)
            )
        }

        IconButton(
            onClick = {
                onSwapCards(cards[fromCardIndex], cards[toCardIndex])
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            RoundTransferVerticalBoldIcon(modifier = Modifier.size(80.dp))
        }

        Text(
            text = "${cards[fromCardIndex].balance} ${cards[fromCardIndex].currency}",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
    }
}