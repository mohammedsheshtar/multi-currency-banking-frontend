package com.joincoded.bankapi.data.request

data class RegisterRequest(
    val username: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String
) 