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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                ShopItemCard(item, viewModel = shopViewModel)
            }
        }

    }
}

@Composable
fun ShopItemCard(item: ShopItem, viewModel: ShopViewModel) {
    val backgroundColor = if (item.isUnlocked) CardDark else CardDark.copy(alpha = 0.3f)
    val textColor = if (item.isUnlocked) TextLight else TextLight.copy(alpha = 0.5f)
    val purpleAccent = Color(0xFFB297E7)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isUnlocked) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left icon
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Tier Icon",
                tint = item.tierColor,
                modifier = Modifier.size(36.dp)
            )

            // Center content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.name,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )

//                Text(
//                    text = item.description,
//                    color = textColor,
//                    fontSize = 12.sp,
//                    maxLines = 1
//                )
                Text(
                    text = "${item.tier} • ${item.requiredPoints} pts",
                    color = item.tierColor,
                    fontSize = 12.sp
                )

            }

            // Right button
            Button(
                onClick = { viewModel.buyItem(item.id) },
                enabled = item.isUnlocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item.isUnlocked) purpleAccent else Color.Gray,
                    contentColor = Color.Black,
                    disabledContentColor = Color.DarkGray
                ),
                modifier = Modifier.height(36.dp)
            ) {
                if (item.isUnlocked) {
                    Text("Buy", fontSize = 12.sp)
                } else {
                    Icon(Icons.Filled.Lock, contentDescription = "Locked")
                }
            }
        }
    }
}




data class ShopItem(
    val id: Long,
    val name: String,
    val tier: String,
    val requiredPoints: Int,
    val tierColor: Color,
    val isUnlocked: Boolean
)



