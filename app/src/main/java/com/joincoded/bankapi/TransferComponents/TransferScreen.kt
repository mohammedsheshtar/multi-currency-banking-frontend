package com.joincoded.bankapi.TransferComponents

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.joincoded.bankapi.R
import com.joincoded.bankapi.data.PaymentCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.joincoded.bankapi.TransferComponents.RoundTransferVerticalBoldIcon

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    cards: List<PaymentCard>,
    amount: String,
    currencies: List<String>,
    selectedCurrency: String,
    onAmountChanged: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var fromCard by remember { mutableStateOf(cards[0]) }
    var currentToIndex by remember { mutableStateOf(1.coerceAtMost(cards.lastIndex)) }
    val toCard = cards[currentToIndex]
    var isSwapping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black, Color(0xFF000000), Color.Black),
                    startY = Float.POSITIVE_INFINITY,
                    endY = 0f
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Transfer",
            color = Color(0xFFD1B4FF),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.zIndex(0f)
            ) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
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
                                        currentToIndex = (currentToIndex + 1) % cards.size
                                    } else if (dragAmount > 40) {
                                        currentToIndex = (currentToIndex - 1 + cards.size) % cards.size
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .zIndex(1f)
                    .background(Color(0xFF59516B), shape = RoundedCornerShape(32.dp))
                    .border(4.dp, Color(0xFF000000), shape = RoundedCornerShape(32.dp))
                    .clickable {
                        if (!isSwapping) {
                            coroutineScope.launch {
                                isSwapping = true
                                delay(300)
                                val temp = fromCard
                                fromCard = toCard
                                currentToIndex = cards.indexOf(temp).coerceAtLeast(0)
                                delay(300)
                                isSwapping = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                RoundTransferVerticalBoldIcon(
                    modifier = Modifier.size(55.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF1A1A1D), shape = RoundedCornerShape(30.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            var expanded by remember { mutableStateOf(false) }

            TextField(
                value = amount,
                onValueChange = onAmountChanged,
                placeholder = { Text("Amount", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0x8DD1B4FF), shape = CircleShape)
                    .clickable { expanded = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedCurrency,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text(currency) },
                        onClick = {
                            onCurrencySelected(currency)
                            expanded = false
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_circle_right_24),
                contentDescription = "Continue",
                modifier = Modifier.size(70.dp),
                tint = Color(0xFFD1B4FF)
            )
        }
    }
} 