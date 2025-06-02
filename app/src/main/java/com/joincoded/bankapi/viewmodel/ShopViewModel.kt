package com.joincoded.bankapi.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.composable.ShopItem
import com.joincoded.bankapi.network.RetrofitHelper
import kotlinx.coroutines.launch

class ShopViewModel : ViewModel() {
    val items = mutableStateListOf<ShopItem>()
    val errorMessage = mutableStateOf<String?>(null)

    var token by mutableStateOf<String?>(null)

    fun fetchShopItems() {
        viewModelScope.launch {
            try {
                val response = RetrofitHelper.ShopApi.viewItems(token)
                if (response.isSuccessful) {
                    val data = response.body() as? List<*>
                    val shopItems = (data as? List<Map<String, Any>>)?.mapNotNull { item ->
                        val name = item["itemName"] as? String ?: return@mapNotNull null
                        val tier = item["tierName"] as? String ?: return@mapNotNull null
                        val cost = (item["pointCost"] as? Double)?.toInt() ?: return@mapNotNull null
                        val unlocked = item["isPurchasable"] as? Boolean ?: false

                        ShopItem(
                            name = name,
//                            description = "No description provided",
                            tier = tier,
                            requiredPoints = cost,
                            tierColor = when (tier.uppercase()) {
                                "BRONZE" -> Color(0xFFCD7F32)
                                "SILVER" -> Color.LightGray
                                "GOLD" -> Color(0xFFFFD700)
                                else -> Color.Gray
                            },
                            isUnlocked = unlocked
                        )
                    } ?: emptyList()

                    items.clear()
                    items.addAll(shopItems)
                    errorMessage.value = null // ✅ Clear previous error on success
                } else {
                    val msg = response.message().takeIf { it.isNotBlank() } ?: "Unexpected error occurred"
                    errorMessage.value = msg
                }
            } catch (e: Exception) {
                errorMessage.value = "Fetch failed: ${e.localizedMessage}"
            }
        }
    }
}