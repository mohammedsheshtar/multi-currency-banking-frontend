package com.joincoded.bankapi

import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.data.Transaction
import com.joincoded.bankapi.data.TransactionType
import com.joincoded.bankapi.data.TransactionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CardRepository(private val apiService: CardApiService) {

    fun listUserAccounts(): Flow<List<PaymentCard>> = flow {
        try {
            val response = apiService.listUserAccounts()
            emit(response.map { account ->
                PaymentCard(
                    id = account.id,
                    number = account.accountNumber,
                    name = account.holderName,
                    expMonth = account.expiryMonth,
                    expYear = account.expiryYear,
                    cvv = account.cvv,
                    type = account.type,
                    background = R.drawable.card_background_1,
                    balance = account.balance,
                    currency = account.currency
                )
            })
        } catch (e: Exception) {
            throw e
        }
    }

    fun getTransactionHistory(accountId: String): Flow<List<Transaction>> = flow {
        try {
            val response = apiService.getTransactionHistory()
            emit(response.map { transaction ->
                Transaction(
                    id = transaction.id,
                    cardId = transaction.fromAccountId,
                    type = when (transaction.type) {
                        "DEPOSIT" -> TransactionType.DEPOSIT
                        "WITHDRAWAL" -> TransactionType.WITHDRAWAL
                        "TRANSFER" -> TransactionType.TRANSFER
                        "PAYMENT" -> TransactionType.PAYMENT
                        "REFUND" -> TransactionType.REFUND
                        else -> TransactionType.PAYMENT
                    },
                    amount = BigDecimal(transaction.amount.toString()),
                    timestamp = LocalDateTime.parse(
                        transaction.timestamp,
                        DateTimeFormatter.ISO_DATE_TIME
                    ),
                    description = "Transaction ${transaction.type.lowercase()}",
                    status = when (transaction.status) {
                        "PENDING" -> TransactionStatus.PENDING
                        "COMPLETED" -> TransactionStatus.COMPLETED
                        "FAILED" -> TransactionStatus.FAILED
                        "CANCELLED" -> TransactionStatus.CANCELLED
                        else -> TransactionStatus.PENDING
                    },
                    recipientId = transaction.toAccountId,
                    recipientName = null
                )
            })
        } catch (e: Exception) {
            throw e
        }
    }

    fun closeAccount(accountId: String): Flow<CloseAccountResponse> = flow {
        try {
            val response = apiService.closeAccount(
                accountId = accountId,
                request = CloseAccountRequest(reason = "User requested account closure")
            )
            emit(response)
        } catch (e: Exception) {
            throw e
        }
    }

    fun transferMoney(transferRequest: TransferRequest): Flow<TransferResponse> = flow {
        try {
            val response = apiService.transferAccounts(transferRequest)
            emit(response)
        } catch (e: Exception) {
            throw e
        }
    }

    companion object {
        fun create(baseUrl: String): CardRepository {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(CardApiService::class.java)
            return CardRepository(apiService)
        }
    }
} 