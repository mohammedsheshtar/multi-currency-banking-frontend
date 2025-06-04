package com.joincoded.bankapi.data.response

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import android.util.Log
import com.google.gson.annotations.SerializedName

data class TokenResponse(val token: String)

data class ListAccountResponse(
    val id: Long? = null,
    val balance: BigDecimal,
    val accountNumber: String,
    val accountType: String,
    val createdAt: String,
    val countryCode: String,
    val symbol: String,
    val cardColor: String? = null
)

data class CreateAccountResponse(
    val id: Long,
    val balance: BigDecimal,
    val accountNumber: String,
    val accountType: String,
    val createdAt: LocalDateTime,
    val countryCode: String,
    val symbol: String
)

data class AuthenticationResponse(
    val token: String,
    val username: String
)

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val role: String
)

data class KYCResponse(
    val firstName: String,
    val lastName: String,
    @SerializedName("dateOfBirth")
    private val dateOfBirthStr: String,
    val civilId: String,
    val country: String,
    val phoneNumber: String,
    val email: String
) {
    val dateOfBirth: LocalDate
        get() = try {
            LocalDate.parse(dateOfBirthStr)
        } catch (e: Exception) {
            Log.e("KYCResponse", "Error parsing date: $dateOfBirthStr", e)
            LocalDate.now()
        }
}

data class ListMembershipResponse(
    val tierName: String,
    val memberLimit: Int,
    val discountAmount: BigDecimal
)

data class PurchaseResponse(
    val updatedPoints: Int,
)

data class ListItemsResponse(
    val id: Long,
    val itemName: String,
    val tierName: String,
    val pointCost: Int,
    val itemQuantity: Int,
    val isPurchasable: Boolean
) : Serializable

data class LegacyShopTransactionResponse(
    val itemName: String,
    val itemTier: String,
    val accountTier: String,
    val pointsSpent: Int,
    val timeOfTransaction: String
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
    val id: Long,
    val accountNumber: String,
    val amount: BigDecimal,
    val transactionType: String,
    val timeStamp: LocalDateTime,
    val requestedCurrency: String,
    val conversionRate: BigDecimal?
)

data class ConversionRateResponse(
    val from: String,
    val to: String,
    val rate: BigDecimal
)

data class CurrencyResponse(
    val countryCode: String,
    val symbol: String,
    val name: String
)

data class ApiResponse(
    val success: Boolean,
    val message: String
)