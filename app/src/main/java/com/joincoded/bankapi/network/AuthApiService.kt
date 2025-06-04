package com.joincoded.bankapi.network

import com.joincoded.bankapi.data.request.AuthenticationRequest
import com.joincoded.bankapi.data.request.RegisterRequest
import com.joincoded.bankapi.data.response.AuthenticationResponse
import com.joincoded.bankapi.data.response.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/authentication/api/v1/authentication/login")
    suspend fun login(@Body request: AuthenticationRequest): Response<AuthenticationResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
} 