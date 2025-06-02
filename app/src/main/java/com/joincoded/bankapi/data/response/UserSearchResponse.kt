package com.joincoded.bankapi.data.response

data class UserSearchResponse(
    val username: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val accountNumber: String
) 