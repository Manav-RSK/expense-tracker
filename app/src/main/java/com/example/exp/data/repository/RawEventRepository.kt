package com.example.exp.data.repository

import com.example.exp.data.local.dao.RawEventDao
import com.example.exp.data.local.entity.RawEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RawEventRepository(
    private val dao: RawEventDao
) {

    // 1. Insert single event (with basic dedup)
    // Returns true if insert happened (new row created), false if dedup'd/ignored
    suspend fun insert(event: RawEventEntity): Boolean {

        val sourceId = event.sourceId

        if (sourceId != null) {
            val existing = dao.findBySourceId(sourceId)
            if (existing != null) {
                // Duplicate (same sourceId) — nothing inserted
                return false
            }
        }

        val inserted = dao.insert(event)
        val success = inserted > 0
        // If insert succeeded (Room returns rowId > 0), emit a new-event signal so
        // observers (e.g., ViewModel) can react and run the pipeline.
        if (success) {
            _newEvents.tryEmit(event.id)
        }
        return success
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

    // Observe all events as a Flow so UI can receive realtime updates when DB changes
    fun observeAll(): Flow<List<RawEventEntity>> = dao.observeAll()

    // SharedFlow that emits new inserted raw-event ids. Observers can collect this
    // to trigger processing/side-effects in realtime.
    private val _newEvents = MutableSharedFlow<String>(extraBufferCapacity = 64)
    fun newEvents(): Flow<String> = _newEvents.asSharedFlow()
}