package com.example.exp.data.source.parser

import com.example.exp.domain.model.ConfidenceLevel
import com.example.exp.domain.model.Transaction
import com.example.exp.domain.model.TransactionSource
import com.example.exp.domain.model.TransactionType
import java.util.UUID

class SimpleSmsParser {

    fun parse(rawText: String): Transaction? {

        val amount = extractAmount(rawText) ?: return null
        val merchant = extractMerchant(rawText) ?: "unknown"

        return Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            currency = "INR",
            merchantName = merchant,
            normalizedName = merchant.lowercase(),
            category = categorize(merchant),
            type = classifyType(merchant),
            source = TransactionSource.SMS,
            confidenceScore = 0,
            transactionTime = System.currentTimeMillis(),
            rawEventId = "" // will set later
        )
    }

    private fun extractAmount(text: String): Double? {
        val regex = Regex("""\b\d+(\.\d{1,2})?\b""")
        return regex.find(text)?.value?.toDoubleOrNull()
    }

    private fun extractMerchant(text: String): String? {
        return when {
            text.contains("Swiggy", true) -> "Swiggy"
            text.contains("Amazon", true) -> "Amazon"
            else -> null
        }
    }

    private fun categorize(merchant: String): String {
        return when (merchant.lowercase()) {
            "swiggy", "zomato" -> "Food"
            "amazon", "flipkart" -> "Shopping"
            else -> "Others"
        }
    }

    private fun classifyType(merchant: String): TransactionType {
        return if (merchant == "unknown") {
            TransactionType.UNKNOWN
        } else {
            TransactionType.MERCHANT
        }
    }
}