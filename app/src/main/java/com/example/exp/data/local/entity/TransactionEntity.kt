package com.example.exp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(

    @PrimaryKey
    val id: String,

    val amount: Double,

    val currency: String,              // ✅ added

    val merchantName: String,
    val normalizedName: String,        // ✅ added

    val category: String,

    val type: String,

    val source: String,

    val confidenceScore: Int,            // ✅ added

    val transactionTime: Long,

    val rawEventId: String
)