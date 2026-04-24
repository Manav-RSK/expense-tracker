package com.example.exp.data.repository

import com.example.exp.data.local.dao.RawEventDao
import com.example.exp.data.local.entity.RawEventEntity

class RawEventRepository(
    private val dao: RawEventDao
) {

    // 1. Insert single event (with basic dedup)
    suspend fun insert(event: RawEventEntity) {

        val sourceId = event.sourceId

        if (sourceId != null) {
            val existing = dao.findBySourceId(sourceId)
            if (existing != null) {
                return
            }
        }

        dao.insert(event)
    }

    // 2. Insert multiple events (batch)
    suspend fun insertAll(events: List<RawEventEntity>) {
        dao.insertAll(events)
    }

    // 3. Get unprocessed events (for pipeline)
    suspend fun getNUnprocessed(limit: Int): List<RawEventEntity> {
        return dao.getNUnprocessedEvents(limit)
    }
    // 3. Get unprocessed events (for pipeline)
    suspend fun getUnprocessed(limit: Int): List<RawEventEntity> {
        return dao.getUnprocessedEvents()
    }

    // 4. Mark events as processed
    suspend fun markProcessed(ids: List<String>) {
        dao.markProcessed(ids)
    }

    // 5. Get latest events (for UI/debug)
    suspend fun getLatest(limit: Int): List<RawEventEntity> {
        return dao.getLatestEvents(limit)
    }
}