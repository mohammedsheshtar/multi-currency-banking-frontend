package com.joincoded.bankapi.data

data class TransferUser(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val kycVerified: Boolean
) 