package com.joincoded.bankapi.composable

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.joincoded.bankapi.ViewModel.WalletViewModel
import com.joincoded.bankapi.data.PaymentCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseAccountModal(
    card: PaymentCard,
    showModal: Boolean,
    onDismiss: () -> Unit,
    walletViewModel: WalletViewModel
) {
    var showSuccessDialog by remember { mutableStateOf(false) }
    var modalScale by remember { mutableStateOf(0.8f) }
    var modalAlpha by remember { mutableStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Animation for modal appearance
    LaunchedEffect(showModal) {
        if (showModal) {
            modalScale = 1f
            modalAlpha = 1f
        } else {
            modalScale = 0.8f
            modalAlpha = 0f
        }
    }

    if (showModal) {
        Dialog(
            onDismissRequest = { 
                if (!isLoading) {
                    coroutineScope.launch {
                        modalScale = 0.8f
                        modalAlpha = 0f
                        delay(300)
                        onDismiss()
                    }
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
                        // Close button
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { 
                                    if (!isLoading) {
                                        coroutineScope.launch {
                                            modalScale = 0.8f
                                            modalAlpha = 0f
                                            delay(300)
                                            onDismiss()
                                        }
                                    }
                                },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Warning icon
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFFFA500),
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            "Close Account",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Are you sure you want to close this account?\n\n" +
                            "Account Details:\n" +
                            "Type: ${card.type}\n" +
                            "Number: ${card.accountNumber}\n" +
                            "Balance: ${card.balance} ${card.currency}\n\n" +
                            "This action cannot be undone.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                errorMessage!!,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Close Account button
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                walletViewModel.closeAccount(
                                    accountNumber = card.accountNumber,
                                    onSuccess = {
                                        Log.d("CloseAccountModal", "✅ Account closed successfully")
                                        showSuccessDialog = true
                                    },
                                    onError = { error ->
                                        Log.e("CloseAccountModal", "❌ Account closure failed: $error")
                                        errorMessage = error
                                        isLoading = false
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF4444),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFFF4444).copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Close Account")
                            }
                        }
                    }
                }
            }
        }
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
                    onDismiss()
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
                                Icons.Default.Warning,
                                contentDescription = "Success",
                                tint = Color(0xFFFFA500),
                                modifier = Modifier.size(64.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            "Account Closed",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Your account has been successfully closed.\n\n" +
                            "Account Details:\n" +
                            "Type: ${card.type}\n" +
                            "Number: ${card.accountNumber}\n" +
                            "Balance: ${card.balance} ${card.currency}\n\n" +
                            "The account will no longer be accessible.",
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
                                    modalScale = 0.8f
                                    modalAlpha = 0f
                                    delay(300)
                                    showSuccessDialog = false
                                    onDismiss()
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
} 