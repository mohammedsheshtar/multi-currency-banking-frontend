package com.joincoded.bankapi.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.composable.ShopItem
import com.joincoded.bankapi.data.response.ShopItemResponse
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
                // First get the user's tier from memberships
                val membershipResponse = membershipApiService.listUserMemberships(token)
                if (membershipResponse.isSuccessful) {
                    val memberships = membershipResponse.body()
                    userTier = memberships?.firstOrNull()?.tierName ?: "BRONZE"
                } else {
                    userTier = "BRONZE"
                }

                // Then get the shop items
                fetchShopItems(userTier)
            } catch (e: Exception) {
                println("❗ Error fetching user data: ${e.message}")
                errorMessage.value = "Error fetching user data: ${e.message}"
            }
        }
    }

    fun fetchShopItems(userTier: String?) {
        viewModelScope.launch {
            try {
                println("🛍️ Fetching shop items with tier: $userTier")
                val response = shopApiService.viewItems(token ?: return@launch)
                if (response.isSuccessful) {
                    val shopItems = response.body()?.mapNotNull { item ->
                        try {
                            ShopItem(
                                id = item.id,
                                name = item.name,
                                tier = item.tierName,
                                requiredPoints = item.pointCost,
                                tierColor = when (item.tierName.uppercase()) {
                                    "BRONZE" -> Color(0xFFCD7F32)
                                    "SILVER" -> Color(0xFFC0C0C0)
                                    "GOLD" -> Color(0xFFFFD700)
                                    "PLATINUM" -> Color(0xFFB0E0E6)
                                    "DIAMOND" -> Color(0xFF00BFFF)
                                    else -> Color.Gray
                                },
                                isUnlocked = compareTier(userTier ?: "", item.tierName),
                                userPoints = userPoints,
                                itemQuantity = 1 // Default to 1 since it's not in the response
                            )
                        } catch (e: Exception) {
                            println("❌ Error parsing item: ${e.message}")
                            null
                        }
                    } ?: emptyList()

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
                val response = shopApiService.buyItem(token ?: return@launch, itemId)
                if (response.isSuccessful) {
                    // Get the updated points from the transaction history
                    val transactionResponse = shopApiService.getShopTransaction(token ?: return@launch)
                    if (transactionResponse.isSuccessful) {
                        val transactions = transactionResponse.body()
                        userPoints = transactions?.firstOrNull()?.updatedPoints ?: userPoints
                    }

                    val item = selectedItemToBuy
                    purchaseMessage = "You bought ${item?.name} for ${item?.requiredPoints} points.\nNow you have $userPoints points."
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

