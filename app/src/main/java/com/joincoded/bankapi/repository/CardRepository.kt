package com.joincoded.bankapi.repository

import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.Transaction
import com.joincoded.bankapi.data.TransactionType
import com.joincoded.bankapi.network.CardApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class CardRepository(
    private val cardApiService: CardApiService
) {
    suspend fun getCards(): List<PaymentCard> {
        return withContext(Dispatchers.IO) {
            cardApiService.getCards()
        }
    }

    suspend fun getCardById(id: String): PaymentCard {
        require(id.isNotBlank()) { "Card ID cannot be empty" }
        return withContext(Dispatchers.IO) {
            cardApiService.getCardById(id)
        }
    }

    suspend fun getCardTransactions(
        cardId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        type: TransactionType? = null
    ): List<Transaction> {
        require(cardId.isNotBlank()) { "Card ID cannot be empty" }
        require(startDate <= endDate) { "Start date must be before or equal to end date" }
        
        return withContext(Dispatchers.IO) {
            cardApiService.getCardTransactions(
                cardId = cardId,
                startDate = startDate,
                endDate = endDate,
                type = type
            )
        }
    }

    suspend fun createCard(
        currency: String,
        cardType: String,
        cardHolderName: String
    ): PaymentCard {
        require(currency.isNotBlank()) { "Currency cannot be empty" }
        require(cardType.isNotBlank()) { "Card type cannot be empty" }
        require(cardHolderName.isNotBlank()) { "Card holder name cannot be empty" }
        
        return withContext(Dispatchers.IO) {
            cardApiService.createCard(
                currency = currency,
                cardType = cardType,
                cardHolderName = cardHolderName
            )
        }
    }

    suspend fun closeCard(cardId: String, reason: String) {
        require(cardId.isNotBlank()) { "Card ID cannot be empty" }
        require(reason.isNotBlank()) { "Reason cannot be empty" }
        
        withContext(Dispatchers.IO) {
            cardApiService.closeCard(
                cardId = cardId,
                reason = reason
            )
        }
    }

    suspend fun getRecentTransactions(
        cardId: String,
        limit: Int = 10
    ): List<Transaction> {
        require(cardId.isNotBlank()) { "Card ID cannot be empty" }
        require(limit > 0) { "Limit must be greater than 0" }
        
        return withContext(Dispatchers.IO) {
            cardApiService.getCardTransactions(
                cardId = cardId,
                startDate = LocalDate.now().minusMonths(1),
                endDate = LocalDate.now()
            ).take(limit)
        }
    }
} 