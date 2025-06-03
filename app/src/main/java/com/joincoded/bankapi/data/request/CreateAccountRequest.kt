package com.joincoded.bankapi.data.request

data class CreateAccountRequest(
    val initialBalance: Double,
    val countryCode: String,
    val accountType: String,
    val cardColor: String = "default"  // Default to "default" for backward compatibility
) 