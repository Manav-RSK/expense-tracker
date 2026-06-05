package com.example.exp.domain.model

data class Transaction(

    val id: String,

    val amount: Double,

    val currency: String,

    val merchantName: String,

    val normalizedName: String,

    val category: String,

    val type: TransactionType,

    val source: TransactionSource,

    val confidenceScore: Int,

    val transactionTime: Long,

    val rawEventId: String
)