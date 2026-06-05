package com.example.exp.domain.processor

import com.example.exp.data.local.dao.TransactionDao
import com.example.exp.domain.contact.ContactMatcher
import com.example.exp.data.local.mapper.toEntity
import com.example.exp.data.repository.RawEventRepository
import com.example.exp.data.source.parser.SimpleSmsParser
import com.example.exp.domain.classifier.TransactionClassifier
import com.example.exp.domain.history.HistoryMatcher

class RawEventProcessor(
    private val repository: RawEventRepository,
    private val transactionDao: TransactionDao,
    private val parser: SimpleSmsParser,
    private val classifier: TransactionClassifier,
    private val contactMatcher: ContactMatcher,
    private val historyMatcher: HistoryMatcher
){

    suspend fun processBatch() {

        val events = repository.getUnprocessed(20)
        if (events.isEmpty()) return

        val processedIds = mutableListOf<String>()

        for (event in events) {

            try {
                val transaction = parser.parse(event.rawText)

                if (transaction != null) {

                    val (type, confidenceScore) =
                        classifier.classify(transaction.merchantName)

                    val contactScore =
                        contactMatcher.getScore(transaction.merchantName)

                    val historyScore =
                        historyMatcher.getScore(
                            transaction.normalizedName
                        )

                    val finalScore =
                        (confidenceScore + contactScore + historyScore)
                            .coerceAtMost(100)

                    val updated = transaction.copy(
                        type = type,
                        confidenceScore = finalScore,
                        rawEventId = event.id
                    )

                    val entity = updated.toEntity()

                    transactionDao.insert(entity)
                }

                processedIds.add(event.id)

            } catch (e: Exception) {
                // DO NOT mark processed if something crashes badly
            }
        }

        repository.markProcessed(processedIds)
    }
}