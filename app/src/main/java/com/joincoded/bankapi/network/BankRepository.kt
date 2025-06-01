package com.joincoded.bankapi.network

import android.util.Log
import com.joincoded.bankapi.data.TransactionItem
import com.joincoded.bankapi.data.request.TransferRequest
import com.joincoded.bankapi.data.response.ListAccountResponse

class BankRepository(
    private val accountApi: AccountApiService,
    private val transactionApi: TransactionApiService,
    private val tokenProvider: () -> String
) {

    suspend fun getUserAccounts(): List<ListAccountResponse> {
        val response = accountApi.listUserAccounts("Bearer ${tokenProvider()}")
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


    suspend fun transfer(from: String, to: String, amount: String, currency: String): Boolean {
        val request = TransferRequest(from, to, amount, currency)
        val response = transactionApi.transferAccounts("Bearer ${tokenProvider()}", request)
        return response.isSuccessful
    }

    suspend fun getTransactions(accountId: Int): List<TransactionItem> {
        val response = transactionApi.getTransactionHistory("Bearer ${tokenProvider()}", accountId)
        return if (response.isSuccessful) {
            response.body() as List<TransactionItem> // cast or map
        } else {
            emptyList()
        }
    }
}
