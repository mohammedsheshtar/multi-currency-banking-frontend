package com.joincoded.bankapi.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

object TokenManager {
    private const val PREF_NAME = "auth_prefs"
    private const val TOKEN_KEY = "jwt"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String?) {
        Log.d("TokenManager", "Saving token: ${token?.take(20)}...")
        getPrefs(context).edit { 
            if (token != null) {
                putString(TOKEN_KEY, token)
                Log.d("TokenManager", "Token saved successfully")
            } else {
                remove(TOKEN_KEY)
                Log.d("TokenManager", "Token removed")
            }
        }
    }

    fun getToken(context: Context): String? {
        val token = getPrefs(context).getString(TOKEN_KEY, null)
        Log.d("TokenManager", "Retrieved token: ${token?.take(20)}...")
        return token
    }

    fun clearToken(context: Context) {
        Log.d("TokenManager", "Clearing token")
        getPrefs(context).edit { remove(TOKEN_KEY) }
    }
}