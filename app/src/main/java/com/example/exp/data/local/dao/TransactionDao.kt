package com.example.exp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.exp.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()

    @Query("""
    SELECT COUNT(*)
    FROM transactions
    WHERE normalizedName = :normalizedName
""")
    suspend fun countTransactions(
        normalizedName: String
    ): Int

    @Query("SELECT * FROM transactions ORDER BY transactionTime DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
}