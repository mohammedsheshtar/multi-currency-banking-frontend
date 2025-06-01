package com.joincoded.bankapi.composable


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.joincoded.bankapi.data.PaymentCard
import kotlinx.coroutines.delay

@Composable
fun PaymentCardView(
    card: PaymentCard,
    modifier: Modifier = Modifier,
    backgroundGradient: Brush? = null
) {
    var showSensitive by remember { mutableStateOf(false) }

    LaunchedEffect(showSensitive) {
        if (showSensitive) {
            delay(3000)
            showSensitive = false
        }
    }

    val defaultBackground = card.background
        ?: "https://media.istockphoto.com/id/2165041511/vector/abstract-black-and-gray-color-gradient-background.jpg"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.DarkGray)
    ) {
        // ✅ Optional background image — comment this to remove image support
        AsyncImage(
            model = defaultBackground,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
        )

        // ✅ Optional gradient overlay
        if (backgroundGradient != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(brush = backgroundGradient)
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
                // ✅ Chip Image
//                AsyncImage(
//                    model = "https://cdn-icons-png.flaticon.com/512/9334/9334639.png",
//                    contentDescription = "Chip",
//                    modifier = Modifier.size(30.dp)
//                )
                Text(
                    text = card.balance.toString(), fontSize = 35.sp, color =Color(0xFFD1B4FF), fontWeight = FontWeight.Bold
                )
                // ✅ Card Type
//                Text(
//                    text = card.type.uppercase(),
//                    fontSize = 20.sp,
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold
//                )
                Text(
                    text=card.currency,  fontSize = 27.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }


            Text(
                text = if (showSensitive) card.cardNumber else maskCardNumber(card.cardNumber),
                fontSize = 18.sp,
                color = Color.White,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Card Holder", fontSize = 12.sp, color = Color.LightGray)
                    Text(card.name.ifEmpty { "FULL NAME" }, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Expires", fontSize = 12.sp, color = Color.LightGray)
                    Text("${card.expMonth}/${card.expYear}", color = Color.White)
                }
            }

            Text(
                text = if (showSensitive) "👁️ Hide Info" else "👁️ Show Info",
                color = Color.White,
                modifier = Modifier.clickable { showSensitive = true }
            )
        }
    }
}

fun maskCardNumber(number: String): String {
    return number.replace(Regex(".(?=.{4})"), "*")
}
