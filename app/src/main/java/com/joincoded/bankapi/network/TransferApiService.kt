package com.joincoded.bankapi.network

import com.joincoded.bankapi.data.TransferUser
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.math.BigDecimal

interface TransferApiService {
    @GET("users/search")
    suspend fun searchUserByPhone(
        @Query("phoneNumber") phoneNumber: String
    ): TransferUser

    @POST("transfers/user")
    suspend fun transferToUser(
        @Query("fromCardId") fromCardId: String,
        @Query("toUserId") toUserId: String,
        @Query("amount") amount: BigDecimal
    )

    @POST("transfers/card")
    suspend fun transferToCard(
        @Query("fromCardId") fromCardId: String,
        @Query("toCardId") toCardId: String,
        @Query("amount") amount: BigDecimal
    )

    @POST("auth/verify-password")
    suspend fun verifyPassword(
        @Query("password") password: String
    ): Boolean
} 