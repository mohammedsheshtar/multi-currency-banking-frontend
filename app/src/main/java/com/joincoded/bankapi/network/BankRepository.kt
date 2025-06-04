package com.joincoded.bankapi.network

import android.util.Log
import com.joincoded.bankapi.data.TransactionItem
import com.joincoded.bankapi.data.request.TransferRequest
import com.joincoded.bankapi.data.response.ListAccountResponse
import com.joincoded.bankapi.data.response.TransactionHistoryResponse
import java.util.*
import java.time.format.DateTimeFormatter

class BankRepository(
    private val accountApi: AccountApiService,
    private val transactionApi: TransactionApiService,
    private val tokenProvider: () -> String
) {

    suspend fun getUserAccounts(): List<ListAccountResponse> {
        val response = accountApi.listUserAccounts(tokenProvider())
        Log.d("BankRepo", "Accounts API status: ${response.code()}")
        Log.d("BankRepo", "Accounts API success: ${response.isSuccessful}")
        Log.d("BankRepo", "Accounts API body: ${response.body()}")
        Log.d("BankRepo", "Accounts API error: ${response.errorBody()?.string()}")
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList()
        }
    }


    suspend fun transfer(from: String, to: String, amount: String, countryCode: String): Boolean {
        val request = TransferRequest(
            sourceAccount = from,
            destinationAccount = to,
            amount = amount,
            countryCode = countryCode
        )
        val response = transactionApi.transfer(tokenProvider(), from, request)
        return response.isSuccessful
    }

    suspend fun getTransactions(accountId: String): List<TransactionItem> {
        val response = transactionApi.getTransactionHistory(tokenProvider(), accountId)
        return if (response.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            val history = response.body() as? List<TransactionHistoryResponse> ?: emptyList()
            history.map {
                TransactionItem(
                    id = UUID.randomUUID().toString(),
                    title = it.transactionType,
                    date = it.timeStamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    amount = if (it.transactionType.lowercase() == "withdraw") "-${it.amount}" else "+${it.amount}",
                    cardId = it.accountNumber
                )
            }
        } else {
            emptyList()
        }
    }
}
