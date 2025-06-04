package com.joincoded.bankapi.network

import com.joincoded.bankapi.data.response.ShopItemResponse
import com.joincoded.bankapi.data.response.ShopTransactionResponse
import retrofit2.Response
import retrofit2.http.*

interface ShopApiService {
    @GET("api/v1/shop/items")
    suspend fun viewItems(
        @Header("Authorization") token: String
    ): Response<List<ShopItemResponse>>

    @POST("api/v1/shop/items/{itemId}/buy")
    suspend fun buyItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: Long
    ): Response<Unit>

    @GET("api/v1/shop/transactions")
    suspend fun getShopTransaction(
        @Header("Authorization") token: String
    ): Response<List<ShopTransactionResponse>>
} 