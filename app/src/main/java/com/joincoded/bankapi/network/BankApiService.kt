package com.joincoded.bankapi.network

import com.joincoded.bankapi.data.AmountChange
import com.joincoded.bankapi.data.User
import com.joincoded.bankapi.data.request.AuthenticationRequest
import com.joincoded.bankapi.data.request.CreateAccount
import com.joincoded.bankapi.data.request.CreateMembership
import com.joincoded.bankapi.data.request.CreateUserDTO
import com.joincoded.bankapi.data.request.DepositRequest
import com.joincoded.bankapi.data.request.KYCRequest
import com.joincoded.bankapi.data.request.TransferRequest
import com.joincoded.bankapi.data.request.WithdrawRequest
import com.joincoded.bankapi.data.response.AuthenticationResponse
import com.joincoded.bankapi.data.response.ConversionRateResponse
import com.joincoded.bankapi.data.response.CreateAccountResponse
import com.joincoded.bankapi.data.response.CurrencyResponse
import com.joincoded.bankapi.data.response.KYCResponse
import com.joincoded.bankapi.data.response.ListAccountResponse
import com.joincoded.bankapi.data.response.ListMembershipResponse
import com.joincoded.bankapi.data.response.ShopTransactionResponse
import com.joincoded.bankapi.data.response.TokenResponse
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import com.joincoded.bankapi.data.response.UserSearchResponse
import com.joincoded.bankapi.data.response.TransferLinkResponse
import com.joincoded.bankapi.data.request.TransferLinkRequest
import com.joincoded.bankapi.data.request.PaymentLinkRequest
import com.joincoded.bankapi.data.response.PaymentLinkResponse
import com.joincoded.bankapi.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BankApiService {
    @POST("api/v1/accounts/{accountNumber}/deposit")
    suspend fun deposit(
        @Header(Constants.authorization) token: String?,
        @Path("accountNumber") accountNumber: String,
        @Body amountChange: AmountChange
    ): Response<Unit>

    @POST("api/v1/accounts/{accountNumber}/withdraw")
    suspend fun withdraw(
        @Header(Constants.authorization) token: String?,
        @Path("accountNumber") accountNumber: String,
        @Body amountChange: AmountChange
    ): Response<Unit>
}

interface AuthenticationApiService {
    @POST("/authentication/api/v1/authentication/login")
    suspend fun login(@Body authRequest: AuthenticationRequest): Response<AuthenticationResponse>
}

interface KycApiService {
    @GET("api/v1/users/kyc")
    suspend fun getMyKYC(@Header(Constants.authorization) token: String?): Response<KYCResponse>

    @POST("api/v1/users/kyc")
    suspend fun addOrUpdateMyKYC(
        @Header(Constants.authorization) token: String?,
        @Body request: KYCRequest
    ): Response<*>

    @GET("api/v1/users/kyc/search")
    suspend fun searchUserByKYC(
        @Header(Constants.authorization) token: String?,
        @Query("query") query: String
    ): Response<KYCResponse>
}

interface MembershipApiService {
    @GET("api/v1/users/memberships")
    suspend fun listUserMemberships(
        @Header(Constants.authorization) token: String?
    ): Response<List<ListMembershipResponse>>

    @POST("api/v1/users/memberships")
    suspend fun createMembership(
        @Header(Constants.authorization) token: String?,
        @Body request: CreateMembership
    ): Response<*>
}

interface TransactionApiService {
    @POST("api/v1/accounts/{accountNumber}/transfer")
    suspend fun transfer(
        @Header(Constants.authorization) token: String?,
        @Path("accountNumber") accountNumber: String,
        @Body request: TransferRequest
    ): Response<*>

    @GET("api/v1/accounts/transactions/{accountNumber}")
    suspend fun getTransactionHistory(
        @Header(Constants.authorization) token: String?,
        @Path("accountNumber") accountNumber: String
    ): Response<*>

    @GET("/api/v1/user/accounts/transactions")
    suspend fun getAllTransactionHistory(
        @Header(Constants.authorization) token: String?
    ): Response<List<TransactionHistoryResponse>>

    @POST("api/v1/transfer-links/generate")
    suspend fun generateTransferLink(
        @Header(Constants.authorization) token: String?,
        @Body request: TransferLinkRequest
    ): Response<TransferLinkResponse>

    @GET("api/v1/accounts/transfer-link/{linkId}")
    suspend fun getTransferLink(
        @Header(Constants.authorization) token: String?,
        @Path("linkId") linkId: String
    ): Response<TransferLinkResponse>

    @POST("api/v1/accounts/transfer-link/{linkId}/accept")
    suspend fun acceptTransferLink(
        @Header(Constants.authorization) token: String?,
        @Path("linkId") linkId: String
    ): Response<Unit>

    @POST("api/v1/transfer-links/generate-payment-link")
    suspend fun generatePaymentLink(
        @Header(Constants.authorization) token: String?,
        @Body request: PaymentLinkRequest
    ): Response<PaymentLinkResponse>
}

interface UserApiService {
    @POST("api/v1/authentication/register")
    suspend fun registerUser(@Body request: CreateUserDTO): Response<Any>

    @GET("api/v1/users/search")
    suspend fun searchUser(
        @Header("Authorization") token: String,
        @Query("query") query: String
    ): Response<UserSearchResponse>
}

interface ConversionRateApiService {
    @GET("api/v1/conversion/rates")
    suspend fun getAllRates(
        @Header(Constants.authorization) token: String?
    ): Response<List<ConversionRateResponse>>

    @GET("api/v1/conversion/rate")
    suspend fun getConversionRate(
        @Header(Constants.authorization) token: String?,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<*>
}

interface CurrenciesApiService {
    @GET("/api/v1/currencies")
    suspend fun getAllCurrencies(
        @Header(Constants.authorization) token: String?
    ): Response<List<CurrencyResponse>>
}