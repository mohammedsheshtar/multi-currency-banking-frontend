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
    var showBuyConfirmDialog by mutableStateOf(false)
    var selectedItemToBuy by mutableStateOf<ShopItem?>(null)
    var showKycDialog by mutableStateOf(false)

    var showSuccessDialog by mutableStateOf(false)
    var purchaseMessage by mutableStateOf<String?>(null)

    var userPoints by mutableStateOf(0)
        private set

    var userTier by mutableStateOf<String?>(null)
        private set

    private val shopApiService = RetrofitHelper.ShopApi
    private val accountApiService = RetrofitHelper.AccountApi
    private val membershipApiService = RetrofitHelper.MembershipApi
    private val kycApiService = RetrofitHelper.KycApi


    fun fetchUserPointsAndItems() {
        viewModelScope.launch {
            try {
                val response = kycApiService.getMyKYC(token ?: return@launch)

                if (response.isSuccessful) {
                    val kyc = response.body()
                    userPoints = kyc?.points ?: 0
                    userTier = kyc?.tier ?: "BRONZE"
                    fetchShopItems(userTier)
                } else if (response.code() == 404) {
                    println("⚠️ No KYC found. Cannot access shop.")
                    showKycDialog = true
                    return@launch
                } else {
                    val err = response.errorBody()?.string()
                    println("❌ KYC failed: ${response.code()} - $err")
                    errorMessage.value = "Failed to load KYC: $err"
                    return@launch
                }

            } catch (e: Exception) {
                println("❗ KYC exception: ${e.message}")
                errorMessage.value = "Error fetching KYC: ${e.message}"
            }
        }
    }






    fun fetchShopItems(userTier: String?) {
        viewModelScope.launch {
            try {
                println("🛍️ Fetching shop items with tier: $userTier, points: $userPoints")
                val response = shopApiService.viewItems(token)
                if (response.isSuccessful) {
                    val data = response.body() as? List<Map<String, Any>>
                    if (data == null) {
                        println("❌ Shop items response was null or not a list")
                        errorMessage.value = "Unexpected shop response format"
                        return@launch
                    }

                    val shopItems = data.mapNotNull { item ->
                        try {
                            val id = (item["id"] as? Double)?.toLong() ?: return@mapNotNull null
                            val name = item["itemName"] as? String ?: return@mapNotNull null
                            val tier = item["tierName"] as? String ?: return@mapNotNull null
                            val cost = (item["pointCost"] as? Double)?.toInt() ?: return@mapNotNull null
                            val unlocked = item["isPurchasable"] as? Boolean ?: false
                            val quantity = (item["itemQuantity"] as? Double)?.toInt() ?: 0

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
                                isUnlocked = unlocked && compareTier(userTier ?: "", tier),
                                userPoints = userPoints,
                                itemQuantity = quantity
                            )
                        } catch (e: Exception) {
                            println("❌ Error parsing item: ${e.message}")
                            null
                        }
                    }

                    items.clear()
                    items.addAll(shopItems)
                    println("✅ Shop items loaded: ${items.size}")
                    errorMessage.value = null
                } else {
                    val body = response.errorBody()?.string()
                    println("❌ Shop API failed: ${response.code()} - $body")
                    errorMessage.value = "Shop API error: $body"
                }
            } catch (e: Exception) {
                println("❗ Unexpected error loading shop: ${e.message}")
                errorMessage.value = "Unexpected error occurred: ${e.message}"
            }
        }
    }



    fun buyItem(itemId: Long) {
        viewModelScope.launch {
            try {
                val response = shopApiService.buyItem(token, itemId.toInt())
                if (response.isSuccessful) {
                    val updatedPoints = (response.body() as? Map<*, *>)?.get("updatedPoints") as? Double
                    updatedPoints?.let { userPoints = it.toInt() }

                    val item = selectedItemToBuy
                    purchaseMessage = "You bought ${item?.name} for ${item?.requiredPoints} points.\nNow you have ${updatedPoints?.toInt()} points."
                    showSuccessDialog = true
                    selectedItemToBuy = null
                    fetchShopItems(userTier)
                } else {
                    errorMessage.value = "Purchase failed: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Unexpected error: ${e.message}"
            }
        }
    }

    fun triggerBuy(item: ShopItem) {
        selectedItemToBuy = item
        showBuyConfirmDialog = true
    }

    fun compareTier(userTier: String, itemTier: String): Boolean {
        val tierOrder = listOf("BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND")
        val userIndex = tierOrder.indexOf(userTier.uppercase())
        val itemIndex = tierOrder.indexOf(itemTier.uppercase())
        return userIndex >= itemIndex
    }

    fun getTierColor(): Color {
        return when (userTier?.uppercase()) {
            "BRONZE" -> Color(0xFFCD7F32)
            "SILVER" -> Color(0xFFC0C0C0)
            "GOLD" -> Color(0xFFFFD700)
            "PLATINUM" -> Color(0xFFB0E0E6)
            "DIAMOND" -> Color(0xFF00BFFF)
            else -> Color.Gray
        }
    }

}

