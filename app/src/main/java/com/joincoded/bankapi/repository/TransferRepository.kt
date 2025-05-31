package com.joincoded.bankapi.repository

import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.TransferUser
import com.joincoded.bankapi.network.TransferApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class TransferRepository(
    private val transferApiService: TransferApiService
) {
    suspend fun searchUserByPhone(phoneNumber: String): TransferUser {
        return withContext(Dispatchers.IO) {
            transferApiService.searchUserByPhone(phoneNumber)
        }
    }

    suspend fun transferToUser(fromCard: PaymentCard, toUser: TransferUser, amount: BigDecimal) {
        withContext(Dispatchers.IO) {
            transferApiService.transferToUser(
                fromCardId = fromCard.id,
                toUserId = toUser.id,
                amount = amount
            )
        }
    }

    suspend fun transferToCard(fromCard: PaymentCard, toCard: PaymentCard, amount: BigDecimal) {
        withContext(Dispatchers.IO) {
            transferApiService.transferToCard(
                fromCardId = fromCard.id,
                toCardId = toCard.id,
                amount = amount
            )
        }
    }

    suspend fun verifyPassword(password: String): Boolean {
        return withContext(Dispatchers.IO) {
            transferApiService.verifyPassword(password)
        }
    }
} 