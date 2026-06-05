package com.example.exp.domain.history

import com.example.exp.data.local.dao.TransactionDao

class HistoryMatcher(
    private val transactionDao: TransactionDao
) {

    suspend fun getScore(
        normalizedName: String
    ): Int {

        val count =
            transactionDao.countTransactions(
                normalizedName
            )

        return when {

            count >= 100 -> 20

            count >= 20 -> 15

            count >= 5 -> 10

            count >= 1 -> 5

            else -> 0
        }
    }
}