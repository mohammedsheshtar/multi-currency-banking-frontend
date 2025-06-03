
package com.joincoded.bankapi.data.request

import java.math.BigDecimal
import java.time.LocalDate

data class CreateAccount(
    val initialBalance: BigDecimal,
    val countryCode: String,
    val accountType: String
)

data class AuthenticationRequest(
    val username: String,
    val password: String
)

class KYCRequest(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val civilId: String,
    val country: String,
    val phoneNumber: String,
    val homeAddress: String,
    val salary: BigDecimal

)

data class DepositRequest(
    val accountNumber: String,
    val countryCode: String,
    val amount: BigDecimal
)

data class WithdrawRequest(
    val accountNumber: String,
    val countryCode: String,
    val amount: BigDecimal
)

data class TransferRequest(
    val sourceAccount: String,
    val destinationAccount: String,
    val amount: BigDecimal,
    val countryCode: String
)

data class CreateUserDTO(
    val username: String,
    val password: String
)

data class ConversionRateRequest(
    val from: String,
    val to: String
)
