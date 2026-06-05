package com.example.exp.domain.classifier

import com.example.exp.domain.model.ConfidenceLevel
import com.example.exp.domain.model.TransactionType

class TransactionClassifier {

    fun classify(name: String): Pair<TransactionType, Int> {

        val n = name.lowercase()

        return when {

            n.contains("swiggy") ||
                    n.contains("zomato") ||
                    n.contains("amazon") ||
                    n.contains("flipkart") ->
                TransactionType.MERCHANT to 90

            n.contains("@") ->
                TransactionType.MERCHANT to 60

            else ->
                TransactionType.PERSON to 30
        }
    }
}