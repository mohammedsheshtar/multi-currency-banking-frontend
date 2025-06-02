package com.joincoded.bankapi.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.joincoded.bankapi.ui.theme.Accent
import com.joincoded.bankapi.ui.theme.CardDark
import com.joincoded.bankapi.ui.theme.DarkBackground
import com.joincoded.bankapi.ui.theme.TextLight
import com.joincoded.bankapi.viewmodel.ShopViewModel

@Composable
fun ShopPage(navController: NavController, token: String, shopViewModel: ShopViewModel = viewModel()) {
    val items = shopViewModel.items
    val error = shopViewModel.errorMessage.value

    LaunchedEffect(token) {
        shopViewModel.token = token
        shopViewModel.fetchShopItems()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text("Shop Items", color = Accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (!error.isNullOrBlank()) {
            Text(text = "Error: $error", color = Color.Red)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                ShopItemCard(item)
            }
        }
    }
}

@Composable
fun ShopItemCard(item: ShopItem) {
    val backgroundColor = if (item.isUnlocked) CardDark else CardDark.copy(alpha = 0.3f)
    val textColor = if (item.isUnlocked) TextLight else TextLight.copy(alpha = 0.5f)
    val purpleAccent = Color(0xFFB297E7)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Tier Icon",
                    tint = item.tierColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    item.name,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description
//            Text(
//                text = item.description,
//                fontSize = 12.sp,
//                color = textColor
//            )

            Spacer(modifier = Modifier.height(4.dp))

            // Tier info
            Text("Tier: ${item.tier}", color = textColor, fontSize = 12.sp)
            Text("Required: ${item.requiredPoints} pts", color = textColor, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Button
            Button(
                onClick = { /* TODO: Buy item or show locked tooltip */ },
                enabled = item.isUnlocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item.isUnlocked) purpleAccent else Color.Gray,
                    contentColor = Color.Black,
                    disabledContentColor = Color.DarkGray
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                if (item.isUnlocked) {
                    Text("Buy")
                } else {
                    Icon(Icons.Filled.Lock, contentDescription = "Locked")
                }
            }
        }
    }
}

data class ShopItem(
    val name: String,
//    val description: String,
    val tier: String,
    val requiredPoints: Int,
    val tierColor: Color,
    val isUnlocked: Boolean
)

