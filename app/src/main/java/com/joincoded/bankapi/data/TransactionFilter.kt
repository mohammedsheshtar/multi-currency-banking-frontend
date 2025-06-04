package com.joincoded.bankapi.data

enum class TransactionFilter(val label: String) {
    ALL("All"),
    DEPOSITS("Deposits"),
    WITHDRAWALS("Withdrawals"),
    TRANSFERS("Transfers"),
    RECENT("Recent"),
    OLDEST("Oldest"),
    HIGHEST_AMOUNT("Highest Amount"),
    LOWEST_AMOUNT("Lowest Amount")
} 