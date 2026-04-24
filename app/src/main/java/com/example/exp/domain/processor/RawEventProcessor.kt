package com.example.exp.domain.processor

import com.example.exp.data.local.dao.TransactionDao
import com.example.exp.data.local.entity.TransactionEntity
import com.example.exp.data.repository.RawEventRepository
import com.example.exp.data.source.parser.SimpleSmsParser

class RawEventProcessor(
    private val repository: RawEventRepository,
    private val transactionDao: TransactionDao,
    private val parser: SimpleSmsParser
) {

    suspend fun processBatch() {

        val events = repository.getUnprocessed(20)
        if (events.isEmpty()) return

        val processedIds = mutableListOf<String>()

        for (event in events) {

            try {
                val transaction = parser.parse(event.rawText)

                if (transaction != null) {

                    val entity = TransactionEntity(
                        id = transaction.id,
                        amount = transaction.amount,
                        currency = transaction.currency,
                        merchantName = transaction.merchantName,
                        normalizedName = transaction.normalizedName,
                        category = transaction.category,
                        type = transaction.type.name,
                        source = transaction.source.name,
                        confidence = transaction.confidence.name,
                        transactionTime = transaction.transactionTime,
                        rawEventId = event.id
                    )

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