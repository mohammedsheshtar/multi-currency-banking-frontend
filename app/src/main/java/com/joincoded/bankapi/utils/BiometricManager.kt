package com.joincoded.bankapi.utils

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager as AndroidXBiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricManager(private val context: Context) {
    private val biometricManager = AndroidXBiometricManager.from(context)

    fun canAuthenticate(): Boolean {
        val result = biometricManager.canAuthenticate(AndroidXBiometricManager.Authenticators.BIOMETRIC_STRONG)
        Log.d("BiometricManager", "Can authenticate result: $result")
        return result == AndroidXBiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFallback: () -> Unit
    ) {
        if (!canAuthenticate()) {
            Log.e("BiometricManager", "Biometric authentication not available")
            onError("Biometric authentication is not available on this device")
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d("BiometricManager", "Authentication succeeded")
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.e("BiometricManager", "Authentication error: $errString (code: $errorCode)")
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED -> {
                        Log.d("BiometricManager", "User canceled authentication")
                        onFallback()
                    }
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        Log.d("BiometricManager", "User chose fallback authentication")
                        onFallback()
                    }
                    BiometricPrompt.ERROR_LOCKOUT -> {
                        Log.e("BiometricManager", "Too many failed attempts")
                        onError("Too many failed attempts. Please try again later.")
                    }
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        Log.e("BiometricManager", "Too many failed attempts - permanent lockout")
                        onError("Too many failed attempts. Biometric authentication is permanently disabled.")
                    }
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                        Log.e("BiometricManager", "No biometrics enrolled")
                        onError("No biometrics enrolled. Please set up biometric authentication in your device settings.")
                    }
                    BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                        Log.e("BiometricManager", "No biometric hardware")
                        onError("This device does not have biometric hardware.")
                    }
                    BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                        Log.e("BiometricManager", "Biometric hardware unavailable")
                        onError("Biometric hardware is currently unavailable.")
                    }
                    else -> {
                        Log.e("BiometricManager", "Unknown error: $errString")
                        onError("Authentication failed: $errString")
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.e("BiometricManager", "Authentication failed")
                onError("Authentication failed. Please try again.")
            }
        }

        try {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authenticate")
                .setSubtitle("Please authenticate to proceed with the payment")
                .setNegativeButtonText("Use Password")
                .build()

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
            Log.d("BiometricManager", "Biometric prompt shown successfully")
        } catch (e: Exception) {
            Log.e("BiometricManager", "Error showing biometric prompt", e)
            onError("Failed to start biometric authentication: ${e.message}")
        }
    }
} 