package com.joincoded.bankapi.data.response

data class ShopItemResponse(
    val id: Long,
    val name: String,
    val description: String,
    val pointCost: Int,
    val tierName: String,
    val imageUrl: String
) 