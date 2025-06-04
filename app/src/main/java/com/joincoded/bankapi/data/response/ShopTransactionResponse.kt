package com.joincoded.bankapi.data.response

import java.time.LocalDateTime

data class ShopTransactionResponse(
    val id: Long,
    val itemName: String,
    val pointCost: Int,
    val purchasedAt: LocalDateTime,
    val tierName: String,
    val updatedPoints: Int
) 