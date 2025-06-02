package com.joincoded.bankapi.data.request

data class CreateAccountRequest(
    val initialBalance: Double,
    val countryCode: String,
    val accountType: String
) 