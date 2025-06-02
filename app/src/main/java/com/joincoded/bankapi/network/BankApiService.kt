package com.joincoded.bankapi.network

import com.joincoded.bankapi.data.AmountChange
import com.joincoded.bankapi.data.User
import com.joincoded.bankapi.data.request.CreateAccount
import com.joincoded.bankapi.data.request.CreateUserDTO
import com.joincoded.bankapi.data.request.DepositRequest
import com.joincoded.bankapi.data.request.KYCRequest
import com.joincoded.bankapi.data.request.TransferRequest
import com.joincoded.bankapi.data.request.WithdrawRequest
import com.joincoded.bankapi.data.response.AuthenticationResponse
import com.joincoded.bankapi.data.response.ListAccountResponse
import com.joincoded.bankapi.data.response.ListMembershipResponse
import com.joincoded.bankapi.data.response.TokenResponse
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import com.joincoded.bankapi.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface BankApiService {

    @POST(Constants.signupEndpoint)
    suspend fun signup(@Body user: User): Response<TokenResponse>


    @PUT(Constants.depositEndpoint)
    suspend fun deposit(@Header(Constants.authorization) token: String?,
                        @Body amountChange: AmountChange
    ): Response<Unit>
}
interface AccountApiService {
    @GET("api/v1/users/accounts")
    suspend fun listUserAccounts(
        @Header(Constants.authorization) token: String?
    ): Response<List<ListAccountResponse>>

    @POST("api/v1/users/accounts")
    suspend fun createAccount(
        @Header(Constants.authorization) token: String?,
        @Body request: CreateAccount
    ): Response<*>

    @POST("/api/v1/users/accounts/{accountNumber}")
    suspend fun closeAccount(
        @Header(Constants.authorization) token: String?,
        @Path("accountNumber") accountNumber: String
    ): Response<*>
}

interface AuthenticationApiService {
    @POST("authentication/api/v1/authentication/login")
    suspend fun login(@Body authRequest: User): Response<AuthenticationResponse>
}

interface KycApiService {
    @GET("api/v1/users/kyc")
    suspend fun getMyKYC(@Header(Constants.authorization) token: String?,
                         request: KYCRequest): Response<*>?

    @POST("api/v1/users/kyc")
    suspend fun addOrUpdateMyKYC(@Header(Constants.authorization) token: String?,
                                 @Body request: KYCRequest): Response<*>?
}

interface MembershipApiService {
    @GET("api/v1/memberships")
    suspend fun getAll(@Header(Constants.authorization) token: String?): List<ListMembershipResponse>

    @GET("api/v1/memberships/tier/{name}")
    suspend fun getByTierName(@Header(Constants.authorization) token: String?,
                              @Path("name") name: String): Response<*>
}

interface ShopApiService {
    @GET("api/v1/shop/items")
    suspend fun viewItems(@Header(Constants.authorization) token: String?): Response<*>

    @POST("api/v1/shop/buy/{itemId}")
    suspend fun buyItem(@Header(Constants.authorization) token: String?,
                        @Path("itemId") itemId: Int): Response<*>

    @GET("/api/v1/shop/history")
    suspend fun getShopTransaction(@Header(Constants.authorization) token: String?): Response<*>
}

interface TransactionApiService {
    @POST("api/v1/accounts/deposit")
    suspend fun depositAccount(@Header(Constants.authorization) token: String?,
                               @Body request: DepositRequest): Response<*>

    @POST("api/v1/accounts/withdraw")
    suspend fun withdrawAccount(@Header(Constants.authorization) token: String?,
                                @Body request: WithdrawRequest): Response<*>

    @POST("/api/v1/accounts/transfer")
    suspend fun transferAccounts(@Header(Constants.authorization) token: String?,
                                 @Body request: TransferRequest): Response<*>

    @GET("api/v1/accounts/transactions/{accountNumber}")
    suspend fun getTransactionHistory(
        @Header(Constants.authorization) token: String?,
        @Path("accountNumber") accountNumber: String
    ): Response<List<TransactionHistoryResponse>>


}


    interface UserApiService {
    @POST("api/v1/authentication/register")
    suspend fun registerUser(@Body request: CreateUserDTO): Response<Any>
}