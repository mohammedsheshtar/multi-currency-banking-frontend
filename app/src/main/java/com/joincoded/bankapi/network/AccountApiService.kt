package com.joincoded.bankapi.network

import com.joincoded.bankapi.data.request.CreateAccountRequest
import com.joincoded.bankapi.data.response.ListAccountResponse
import com.joincoded.bankapi.utils.Constants
import retrofit2.Response
import retrofit2.http.*

interface AccountApiService {
    @GET("/api/v1/users/accounts")
    suspend fun listUserAccounts(
        @Header(Constants.authorization) token: String?
    ): Response<List<ListAccountResponse>>

    @POST("/api/v1/users/accounts")
    suspend fun createAccount(
        @Header(Constants.authorization) token: String?,
        @Body request: CreateAccountRequest
    ): Response<Any>

    @POST("/api/v1/users/accounts/{accountNumber}")
    suspend fun closeAccount(
        @Header(Constants.authorization) token: String?,
        @Path("accountNumber") accountNumber: String
    ): Response<Any>
} 