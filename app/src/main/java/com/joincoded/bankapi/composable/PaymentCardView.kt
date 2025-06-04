package com.joincoded.bankapi.composable

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.joincoded.bankapi.data.PaymentCard
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun PaymentCardView(
    card: PaymentCard,
    modifier: Modifier = Modifier,
    backgroundGradient: Brush? = null,
    isFocused: Boolean = false
) {
    var flipped by remember { mutableStateOf(false) }
    var showSensitive by remember { mutableStateOf(false) }
    
    // Generate a random CVV that stays consistent while the card is shown
    val randomCvv = remember { Random.nextInt(100, 1000).toString() }
    
    // Debug log the actual card number and card data
    Log.d("PaymentCardView", """
        Card Data:
        - Account Number: ${card.accountNumber}
        - Card Number: ${card.cardNumber}
        - Card Type: ${card.type}
        - Card Name: ${card.name}
        - Show Sensitive: $showSensitive
        - Random CVV: $randomCvv
        - Is Focused: $isFocused
    """.trimIndent())
    
    // Memoize the card number and CVV to prevent unnecessary recompositions
    val cardNumber = remember(card.accountNumber, card.cardNumber, showSensitive) {
        val masked = if (showSensitive) {
            Log.d("PaymentCardView", "UNMASKING: Using account number: ${card.accountNumber}")
            card.accountNumber
        } else {
            Log.d("PaymentCardView", "MASKING: Using card number: ${card.cardNumber}")
            card.cardNumber
        }
        masked
    }
    
    val cvv = remember(randomCvv, showSensitive) {
        val masked = if (showSensitive) {
            Log.d("PaymentCardView", "Showing unmasked CVV: $randomCvv")
            randomCvv
        } else {
            Log.d("PaymentCardView", "Showing masked CVV")
            "***"
        }
        masked
    }

    // Handle auto-hide in a separate effect
    LaunchedEffect(showSensitive) {
        if (showSensitive) {
            Log.d("PaymentCardView", "Starting auto-hide timer")
            delay(3000)
            if (showSensitive) {  // Only hide if still showing sensitive info
                Log.d("PaymentCardView", "Auto-hiding sensitive info")
                showSensitive = false
            }
        }
    }

    val defaultBackground = remember(card.background) {
        card.background ?: "https://media.istockphoto.com/id/2165041511/vector/abstract-black-and-gray-color-gradient-background.jpg"
    }

    Box(
        modifier = modifier
            .clickable(enabled = isFocused) { 
                if (isFocused) {
                    Log.d("PaymentCardView", "Card clicked, flipping from ${if (flipped) "back" else "front"} to ${if (flipped) "front" else "back"}")
                    flipped = !flipped 
                }
            }
            .graphicsLayer {
                rotationY = if (flipped) 180f else 0f
                cameraDistance = 8 * density
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    backgroundGradient != null -> backgroundGradient
                    else -> Brush.linearGradient(listOf(Color.DarkGray, Color.DarkGray))
                }
            )
    ) {
        if (!flipped) {
            FrontSide(
                card = card,
                cardNumber = cardNumber,
                showSensitive = showSensitive,
                backgroundImg = if (backgroundGradient == null) defaultBackground else null,
                backgroundGradient = null,  // We already applied the gradient to the Box
                onToggle = {
                    if (isFocused) {
                        Log.d("PaymentCardView", "Show info clicked, current state: $showSensitive")
                        showSensitive = !showSensitive
                        Log.d("PaymentCardView", "New showSensitive state: $showSensitive")
                    }
                }
            )
        } else {
            BackSide(
                card = card,
                cvv = cvv,
                showSensitive = showSensitive,
                backgroundImg = if (backgroundGradient == null) defaultBackground else null,
                backgroundGradient = null  // We already applied the gradient to the Box
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun FrontSide(
    card: PaymentCard,
    cardNumber: String,
    showSensitive: Boolean,
    backgroundImg: String?,
    backgroundGradient: Brush?,
    onToggle: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        // Only show background image if provided and no gradient is used
        if (backgroundImg != null) {
            AsyncImage(
                model = backgroundImg,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.balance.toString(),
                    fontSize = 35.sp,
                    color = Color(0xFFD1B4FF),
                    fontWeight = FontWeight.Bold
                )
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = when(card.currency) {
                            "€" -> "EUR"
                            "$" -> "USD"
                            "د.ك" -> "KWD"
                            "د.إ" -> "AED"
                            else -> card.currency
                        },
                        fontSize = 27.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Debug log the card number being displayed
            Log.d("PaymentCardView", """
                FrontSide Display:
                - Card Number: $cardNumber
                - Show Sensitive: $showSensitive
                - Original Number: ${card.cardNumber}
            """.trimIndent())

            // Animated card number with debug background
            AnimatedContent(
                targetState = Pair(cardNumber, showSensitive),
                transitionSpec = {
                    fadeIn() + slideInVertically() with fadeOut() + slideOutVertically()
                },
                label = "cardNumber"
            ) { (number, isSensitive) ->
                Text(
                    text = number,
                    fontSize = 18.sp,
                    color = Color.White,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            if (isSensitive) Color(0xFF1A1A1D) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .animateContentSize()
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Account Type: ", fontSize = 12.sp, color = Color.LightGray)
                        Text(card.type, fontSize = 12.sp, color = Color(0xFFD1B4FF), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.name,
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Expires", fontSize = 12.sp, color = Color.LightGray)
                    Text("${card.expMonth}/${card.expYear}", color = Color.White)
                }
            }

            // Animated show/hide button
            AnimatedContent(
                targetState = showSensitive,
                transitionSpec = {
                    fadeIn() with fadeOut()
                },
                label = "showHideButton"
            ) { isShowing ->
                Text(
                    text = if (isShowing) "👁️ Hide Info" else "👁️ Show Info",
                    color = if (isShowing) Color(0xFFD1B4FF) else Color.White,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { 
                            Log.d("PaymentCardView", "Show/Hide info button clicked")
                            onToggle()
                        }
                        .animateContentSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BackSide(
    card: PaymentCard,
    cvv: String,
    showSensitive: Boolean,
    backgroundImg: String?,
    backgroundGradient: Brush?
) {
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { rotationY = 180f }
    ) {
        // Only show background image if provided and no gradient is used
        if (backgroundImg != null) {
            AsyncImage(
                model = backgroundImg,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Box(
            Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.6f))
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.Black)
                    .clip(RoundedCornerShape(4.dp))
            )

            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text("CVV", fontSize = 14.sp, color = Color.LightGray)
                // Animated CVV
                AnimatedContent(
                    targetState = cvv,
                    transitionSpec = {
                        fadeIn() + slideInVertically() with fadeOut() + slideOutVertically()
                    },
                    label = "cvv"
                ) { cvvValue ->
                    Text(
                        text = cvvValue,
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                if (showSensitive) Color(0xFFD1B4FF) else Color.White,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .animateContentSize()
                    )
                }
            }
        }
    }
}

fun maskCardNumber(number: String): String {
    // Debug log the masking process
    Log.d("PaymentCardView", """
        Masking Process:
        - Input number: $number
        - Length: ${number.length}
        - Last 4 digits: ${number.takeLast(4)}
    """.trimIndent())
    
    // Only mask if the number is long enough
    if (number.length <= 4) {
        Log.d("PaymentCardView", "Number too short to mask, returning as is: $number")
        return number
    }
    
    val masked = "*".repeat(number.length - 4) + number.takeLast(4)
    Log.d("PaymentCardView", "Masked result: $masked")
    return masked
}
