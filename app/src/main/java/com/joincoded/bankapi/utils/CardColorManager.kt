package com.joincoded.bankapi.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class CardColorManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "card_colors"
        private const val KEY_PREFIX = "card_color_"
        
        @Volatile
        private var instance: CardColorManager? = null
        
        fun getInstance(context: Context): CardColorManager {
            return instance ?: synchronized(this) {
                instance ?: CardColorManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    fun saveCardColor(accountNumber: String, colorName: String) {
        try {
            Log.d("CardColorManager", "Saving color for account $accountNumber: $colorName")
            prefs.edit().putString("$KEY_PREFIX$accountNumber", colorName).apply()
        } catch (e: Exception) {
            Log.e("CardColorManager", "Error saving card color", e)
        }
    }
    
    fun getCardColor(accountNumber: String): String? {
        return try {
            val color = prefs.getString("$KEY_PREFIX$accountNumber", null)
            Log.d("CardColorManager", "Retrieved color for account $accountNumber: $color")
            color
        } catch (e: Exception) {
            Log.e("CardColorManager", "Error getting card color", e)
            null
        }
    }
    
    fun clearCardColor(accountNumber: String) {
        try {
            Log.d("CardColorManager", "Clearing color for account $accountNumber")
            prefs.edit().remove("$KEY_PREFIX$accountNumber").apply()
        } catch (e: Exception) {
            Log.e("CardColorManager", "Error clearing card color", e)
        }
    }
} 