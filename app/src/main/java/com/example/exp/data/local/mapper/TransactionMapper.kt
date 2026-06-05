package com.example.exp.data.local.mapper

import com.example.exp.data.local.entity.TransactionEntity
import com.example.exp.domain.model.Transaction

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        currency = currency,
        merchantName = merchantName,
        normalizedName = normalizedName,
        category = category,
        type = type.name,
        source = source.name,
        confidenceScore = confidenceScore,
        transactionTime = transactionTime,
        rawEventId = rawEventId
    )
}