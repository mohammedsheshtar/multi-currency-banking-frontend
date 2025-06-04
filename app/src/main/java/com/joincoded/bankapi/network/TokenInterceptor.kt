package com.joincoded.bankapi.network

import android.content.Context
import android.util.Log
import com.joincoded.bankapi.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.util.Base64

class TokenInterceptor(private val context: Context) : Interceptor {
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var token = TokenManager.getToken(context)

        // Skip adding token for login requests
        if (originalRequest.url.encodedPath.contains("/auth/login") || 
            originalRequest.url.encodedPath.contains("/authentication/api/v1/authentication/login")) {
            Log.d("TokenInterceptor", "Skipping token for login request to: ${originalRequest.url.encodedPath}")
            return chain.proceed(originalRequest)
        }

        // Check if token is expired
        if (!token.isNullOrBlank() && isTokenExpired(token)) {
            Log.d("TokenInterceptor", "Token is expired, attempting to refresh")
            // For now, we'll just clear the token and force a re-login
            TokenManager.clearToken(context)
            token = null
        }

        Log.d("TokenInterceptor", """
            🔄 Intercepting request:
            - URL: ${originalRequest.url}
            - Method: ${originalRequest.method}
            - Path: ${originalRequest.url.encodedPath}
            - Query: ${originalRequest.url.query}
            - Token present: ${!token.isNullOrBlank()}
        """.trimIndent())

        if (!token.isNullOrBlank()) {
            Log.d("TokenInterceptor", "Token first 20 chars: ${token.take(20)}...")
            Log.d("TokenInterceptor", "Full token: $token")
        }

        // Remove any existing Authorization headers to prevent duplicates
        val newRequestBuilder = originalRequest.newBuilder()
        originalRequest.headers.forEach { (name, value) ->
            if (name.equals("Authorization", ignoreCase = true)) {
                Log.d("TokenInterceptor", "Removing existing Authorization header: $value")
                newRequestBuilder.removeHeader(name)
            }
        }

        val newRequest = if (!token.isNullOrBlank()) {
            // Add the token with Bearer prefix if not already present
            val tokenValue = if (token.startsWith("Bearer ")) token else "Bearer $token"
            newRequestBuilder
                .addHeader("Authorization", tokenValue)
                .build()
        } else {
            newRequestBuilder.build()
        }

        // Log all request headers
        Log.d("TokenInterceptor", """
            📤 Request details:
            - Final URL: ${newRequest.url}
            - Headers: ${newRequest.headers.toMultimap()}
        """.trimIndent())

        val response = chain.proceed(newRequest)

        Log.d("TokenInterceptor", """
            📥 Response received:
            - Code: ${response.code}
            - Message: ${response.message}
            - URL: ${response.request.url}
            - Headers: ${response.headers.toMultimap()}
        """.trimIndent())

        if (response.code == 401) {
            Log.w("TokenInterceptor", "Unauthorized – token may be expired.")
            // Clear the token on 401 to force re-login
            TokenManager.clearToken(context)
        } else if (response.code == 403) {
            Log.w("TokenInterceptor", "Forbidden – token may be invalid or insufficient permissions.")
            // Log response body for more details
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            Log.w("TokenInterceptor", "Response body: $responseBody")
            // Only clear token if it's a token-related 403
            if (responseBody.contains("token", ignoreCase = true) || 
                responseBody.contains("authentication", ignoreCase = true) ||
                responseBody.contains("authorization", ignoreCase = true)) {
                TokenManager.clearToken(context)
            }
        }

        return response
    }

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true
            
            val payload = parts[1]
            val decodedPayload = Base64.getUrlDecoder().decode(payload)
            val jsonString = String(decodedPayload, Charsets.UTF_8)
            val jsonObject = JSONObject(jsonString)
            
            val exp = jsonObject.getLong("exp")
            val currentTime = System.currentTimeMillis() / 1000
            
            exp <= currentTime
        } catch (e: Exception) {
            Log.e("TokenInterceptor", "Error checking token expiration", e)
            true // If we can't parse the token, consider it expired
        }
    }
}
