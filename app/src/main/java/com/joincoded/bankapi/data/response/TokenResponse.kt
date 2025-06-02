package com.joincoded.bankapi.data.response

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class TokenResponse(val token: String?) {
    fun getBearerToken(): String {
        return "Bearer $token"
    }
}

data class ListAccountResponse(
    val id: Long? = null,
    val balance: BigDecimal,
    val accountNumber: String,
    val accountType: String,
    val createdAt: LocalDateTime,
    val countryCode: String,
    val symbol: String
)

data class CreateAccountResponse(
    val balance: BigDecimal,
    val accountNumber: String,
    val accountType: String,
    val createdAt: LocalDateTime,
    val countryCode: String,
    val symbol: String,
)

data class AuthenticationResponse(
    val token: String
)

data class KYCResponse(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val civilId: String,
    val country: String,
    val phoneNumber: String,
    val homeAddress: String,
    val salary: BigDecimal,
    val tier: String,
    val points: Int
)

data class ListMembershipResponse(
    val tierName: String,
    val memberLimit: Int,
    val discountAmount: BigDecimal
)

data class PurchaseResponse(
    val updatedPoints: Int,
)

data class ListItemsResponse(
    val itemName: String,
    val tierName: String,
    val pointCost: Int,
    val itemQuantity: Int,
    val isPurchasable: Boolean
) : Serializable

data class ShopTransactionResponse(
    val itemName: String,
    val itemTier: String,
    val accountTier: String,
    val pointsSpent: Int,
    val timeOfTransaction: LocalDateTime
)

data class DepositResponse(
    val newBalance: BigDecimal,
    val transferStatus: String,
    val isConverted: Boolean,
    val amountDeposited: BigDecimal
)

data class WithdrawResponse(
    val newBalance: BigDecimal,
    val transferStatus: String,
    val isConverted: Boolean,
    val amountWithdrawn: BigDecimal
)

data class TransferResponse(
    val sourceNewBalance: BigDecimal,
    val transferStatus: String,
    val isSourceConverted: Boolean,
    val sourceAmountWithdrawn: BigDecimal,
    val transferFee: BigDecimal
)

data class TransactionHistoryResponse(
    val accountNumber: String,
    val accountCurrency: String,
    val requestedCurrency: String,
    val amount: BigDecimal,
    val status: String,
    val timeStamp: String,
    val transactionType: String,
    val conversionRate: BigDecimal?
)

data class ApiResponse(
    val success: Boolean,
    val message: String
)