package com.joincoded.bankapi.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

//class TokenInterceptor(private val tokenProvider: () -> String?) : Interceptor {
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val originalRequest = chain.request()
//        val token = tokenProvider()
//
//        Log.d("TokenInterceptor", "Intercepting request to: ${originalRequest.url}")
//        Log.d("TokenInterceptor", "Token available: ${!token.isNullOrBlank()}")
//        if (!token.isNullOrBlank()) {
//            Log.d("TokenInterceptor", "Token first 20 chars: ${token.take(20)}")
//        }
//
//        val newRequest = if (!token.isNullOrBlank()) {
//            originalRequest.newBuilder()
//                .addHeader("Authorization", "Bearer $token")
//                .build()
//        } else {
//            originalRequest
//        }
//
//        Log.d("TokenInterceptor", "Request headers: ${newRequest.headers}")
//
//        val response = chain.proceed(newRequest)
//
//        Log.d("TokenInterceptor", "Response code: ${response.code}")
//        if (response.code == 401) {
//            Log.w("TokenInterceptor", "Token expired or unauthorized")
//        }
//
//        return response
//    }
//}


import android.content.Context

import com.joincoded.bankapi.utils.TokenManager


class TokenInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = TokenManager.getToken(context)

        Log.d("TokenInterceptor", "Intercepting: ${originalRequest.url}")
        Log.d("TokenInterceptor", "Token present: ${!token.isNullOrBlank()}")
        if (!token.isNullOrBlank()) {
            Log.d("TokenInterceptor", "Token first 20 chars: ${token.take(20)}...")
            Log.d("TokenInterceptor", "Full token: $token")
        }

        val newRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        // Log all request headers
        Log.d("TokenInterceptor", "Request headers:")
        newRequest.headers.forEach { (name, value) ->
            Log.d("TokenInterceptor", "$name: $value")
        }

        val response = chain.proceed(newRequest)

        Log.d("TokenInterceptor", "Response code: ${response.code}")
        if (response.code == 401) {
            Log.w("TokenInterceptor", "Unauthorized – token may be expired.")
        } else if (response.code == 403) {
            Log.w("TokenInterceptor", "Forbidden – token may be invalid or insufficient permissions.")
            // Log response body for more details
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            Log.w("TokenInterceptor", "Response body: $responseBody")
        }

        return response
    }
}
