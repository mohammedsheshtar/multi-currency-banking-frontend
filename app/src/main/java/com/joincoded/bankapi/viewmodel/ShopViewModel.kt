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

    var token by mutableStateOf<String?>(null)
    val items = mutableStateListOf<ShopItem>()
    val errorMessage = mutableStateOf<String?>(null)

    private val shopApiService = RetrofitHelper.ShopApi

    fun fetchShopItems() {
        viewModelScope.launch {
            try {
                val response = shopApiService.viewItems(token)
                if (response.isSuccessful) {
                    val data = response.body() as? List<*>
                    println("🔍 Raw shop item data: $data")
                    val shopItems = (data as? List<Map<String, Any>>)?.mapNotNull { item ->
                        val id = (item["id"] as? Double)?.toLong() ?: return@mapNotNull null
                        val name = item["itemName"] as? String ?: return@mapNotNull null
                        val tier = item["tierName"] as? String ?: return@mapNotNull null
                        val cost = (item["pointCost"] as? Double)?.toInt() ?: return@mapNotNull null
                        val unlocked = item["isPurchasable"] as? Boolean ?: false

                        ShopItem(
                            id = id,
                            name = name,
                            tier = tier,
                            requiredPoints = cost,
                            tierColor = when (tier.uppercase()) {
                                "BRONZE" -> Color(0xFFCD7F32)
                                "SILVER" -> Color(0xFFC0C0C0)
                                "GOLD" -> Color(0xFFFFD700)
                                "PLATINUM" -> Color(0xFFB0E0E6)
                                "DIAMOND" -> Color(0xFF00BFFF)
                                else -> Color.Gray
                            },
                            isUnlocked = unlocked
                        )
                    } ?: emptyList()

                    items.clear()
                    items.addAll(shopItems)
                    errorMessage.value = null
                } else {
                    val msg = response.message().takeIf { it.isNotBlank() } ?: "Unexpected error occurred"
                    errorMessage.value = msg
                }
            } catch (e: Exception) {
                errorMessage.value = "Fetch failed: ${e.localizedMessage}"
            }
        }
    }

    fun buyItem(itemId: Long) {
        viewModelScope.launch {
            try {
                val response = shopApiService.buyItem(token, itemId.toInt())
                if (response.isSuccessful) {
                    val updatedPoints = (response.body() as? Map<*, *>)?.get("updatedPoints") as? Double
                    println("✅ Purchase successful, new points: $updatedPoints")
                    fetchShopItems() // refresh after purchase
                } else {
                    errorMessage.value = "Purchase failed: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Unexpected error: ${e.message}"
            }
        }
    }
}

