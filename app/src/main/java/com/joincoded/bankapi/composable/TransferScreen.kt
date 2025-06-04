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
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.PaymentCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.fragment.app.FragmentActivity
import androidx.activity.ComponentActivity
import android.content.ContextWrapper
import com.joincoded.bankapi.utils.BiometricManager
import com.joincoded.bankapi.SVG.RotationArrowIcon
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import com.joincoded.bankapi.viewmodel.WalletViewModel
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
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
    var isSwapping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var swipeDirection by remember { mutableStateOf(0) }
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

    // Add LaunchedEffect to observe transaction updates
    LaunchedEffect(Unit) {
        walletViewModel?.fetchUserCards()
    }

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

    // Add focus state for amount input
    var isAmountFocused by remember { mutableStateOf(false) }
    val amountFocusRequester = remember { FocusRequester() }

    // Simplified keyboard control
    val keyboardController = LocalSoftwareKeyboardController.current
    
    fun hideKeyboard() {
        keyboardController?.hide()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Color.Black, Color(0xFF000000), Color.Black)))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
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
                            .pointerInput(currentCards.size) {
                                detectHorizontalDragGestures(
                                    onDragEnd = { swipeDirection = 0 },
                                    onDragCancel = { swipeDirection = 0 }
                                ) { change, dragAmount ->
                                    change.consume()
                                    if (!isSwapping && currentCards.isNotEmpty()) {
                                        if (dragAmount < -20) {
                                            swipeDirection = -1
                                            currentToIndex = (currentToIndex + 1) % currentCards.size
                                        } else if (dragAmount > 20) {
                                            swipeDirection = 1
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
                                        // Swipe left: current card slides left, new card comes from right
                                        (slideInHorizontally { it } + fadeIn()) with (slideOutHorizontally { -it } + fadeOut())
                                    } else if (swipeDirection > 0) {
                                        // Swipe right: current card slides right, new card comes from left
                                        (slideInHorizontally { -it } + fadeIn()) with (slideOutHorizontally { it } + fadeOut())
                                    } else {
                                        // No swipe direction (initial state): fade transition
                                        fadeIn() with fadeOut()
                                    }
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

            // Amount input section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .windowInsetsPadding(WindowInsets.ime.only(WindowInsetsSides.Bottom)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Amount input field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onAmountChanged(it)
                        }
                    },
                    label = { Text("Amount", color = Color.White) },
                    prefix = { Text(fromCard.currency, color = Color.White.copy(alpha = 0.7f)) },
                    suffix = if (amount.isNotEmpty()) { { Text(fromCard.currency, color = Color.White.copy(alpha = 0.5f)) } } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (amount.isNotEmpty()) {
                                showPasswordDialog = true
                            }
                        }
                    ),
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
                        .focusRequester(amountFocusRequester)
                        .onFocusChanged { focusState ->
                            isAmountFocused = focusState.isFocused
                        }
                )

                if (showConversionInfo) {
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
                    hideKeyboard()
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

    // Update BackHandler
    BackHandler {
        when {
            showSuccessDialog -> {
                // Don't allow back navigation when success dialog is showing
                Log.d("TransferScreen", "Back pressed while success dialog showing - ignoring")
            }
            showPasswordDialog -> {
                hideKeyboard()
                showPasswordDialog = false
            }
            showErrorDialog -> {
                hideKeyboard()
                showErrorDialog = false
            }
            isAmountFocused -> {
                hideKeyboard()
            }
            else -> {
                hideKeyboard()
                onBack()
            }
        }
    }

    // Debug log for initial composition
    Log.d("TransferScreen", "🚀 Component composed - showSuccessDialog: $showSuccessDialog")

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

    // Success Dialog
    if (showSuccessDialog) {
        Log.d("TransferScreen", "🎯 About to render success dialog - showSuccessDialog: $showSuccessDialog, amount: $transferAmount $transferCurrency")
        Dialog(
            onDismissRequest = { 
                hideKeyboard()
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
                                hideKeyboard()
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
    } else {
        Log.d("TransferScreen", "❌ Success dialog not showing - showSuccessDialog: $showSuccessDialog")
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                hideKeyboard()
                showErrorDialog = false 
            },
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
private fun TransferCardSelector(
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