package com.joincoded.bankapi.CardTransactionComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.joincoded.bankapi.data.PaymentCard

@Composable
fun BackSide(card: PaymentCard, showSensitive: Boolean, backgroundImg: String) {
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { rotationY = 180f }
    ) {
        // Background image
        AsyncImage(
            model = backgroundImg,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
        )

        // Dark overlay
        Box(
            Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clip(RoundedCornerShape(16.dp))
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Magnetic strip
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.Black)
                    .clip(RoundedCornerShape(4.dp))
            )

            // CVV section aligned to the right
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text("CVV", color = Color.White)
                Text(
                    text = if (showSensitive) card.cvv else "***",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
            }
        }
    }
} 