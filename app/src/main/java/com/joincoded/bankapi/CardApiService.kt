package com.joincoded.bankapi

import retrofit2.http.*

interface CardApiService {
    @GET("accounts")
    suspend fun listUserAccounts(): List<AccountResponse>

    @POST("accounts/{accountId}/close")
    suspend fun closeAccount(
        @Path("accountId") accountId: String,
        @Body request: CloseAccountRequest
    ): CloseAccountResponse

    @POST("accounts/transfer")
    suspend fun transferAccounts(
        @Body request: TransferRequest
    ): TransferResponse

    @GET("transactions")
    suspend fun getTransactionHistory(): List<TransactionResponse>
}

data class AccountResponse(
    val id: String,
    val accountNumber: String,
    val balance: Double,
    val currency: String,
    val type: String,
    val holderName: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvv: String
)

data class CloseAccountRequest(
    val reason: String
)

data class CloseAccountResponse(
    val success: Boolean,
    val message: String
)

data class TransferRequest(
    val fromAccountId: String,
    val toAccountId: String,
    val amount: Double,
    val currency: String
)

data class TransferResponse(
    val success: Boolean,
    val message: String,
    val transactionId: String
)

data class TransactionResponse(
    val id: String,
    val fromAccountId: String,
    val toAccountId: String,
    val amount: Double,
    val currency: String,
    val type: String,
    val status: String,
    val timestamp: String
) 