package com.joincoded.bankapi.network

import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.Transaction
import com.joincoded.bankapi.data.TransactionType
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.LocalDate

interface CardApiService {
    @GET("cards")
    suspend fun getCards(): List<PaymentCard>

    @GET("cards/{id}")
    suspend fun getCardById(
        @Path("id") id: String
    ): PaymentCard

    @GET("cards/{cardId}/transactions")
    suspend fun getCardTransactions(
        @Path("cardId") cardId: String,
        @Query("startDate") startDate: LocalDate,
        @Query("endDate") endDate: LocalDate,
        @Query("type") type: TransactionType? = null
    ): List<Transaction>

    @POST("cards")
    suspend fun createCard(
        @Query("currency") currency: String,
        @Query("cardType") cardType: String,
        @Query("cardHolderName") cardHolderName: String
    ): PaymentCard

    @POST("cards/{cardId}/close")
    suspend fun closeCard(
        @Path("cardId") cardId: String,
        @Query("reason") reason: String
    )
} 