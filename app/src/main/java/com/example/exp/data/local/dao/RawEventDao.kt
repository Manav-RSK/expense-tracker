package com.example.exp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.exp.data.local.entity.RawEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RawEventDao {
    // ✅ Insert single event (ignore duplicates)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: RawEventEntity): Long

    // ✅ Insert list (batch)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(events: List<RawEventEntity>): List<Long>

    // ✅ Get all events (for debugging / UI)
    @Query("SELECT * FROM raw_events ORDER BY receivedAt DESC")
    suspend fun getAll(): List<RawEventEntity>

    // ✅ Flow version (if you want live updates in UI)
    @Query("SELECT * FROM raw_events ORDER BY receivedAt DESC")
    fun observeAll(): Flow<List<RawEventEntity>>

    // ✅ Get unprocessed events (CORE for pipeline)
    @Query("SELECT * FROM raw_events WHERE processed = 0 ORDER BY receivedAt ASC")
    suspend fun getUnprocessedEvents(): List<RawEventEntity>

    // ✅ Get n unprocessed events (CORE for pipeline)
    @Query("SELECT * FROM raw_events WHERE processed = 0 ORDER BY receivedAt ASC LIMIT :limit")
    suspend fun getNUnprocessedEvents(limit: Int): List<RawEventEntity>

    // ✅ Mark single event as processed
    @Query("UPDATE raw_events SET processed = 1 WHERE id = :id")
    suspend fun markProcessed(id: String)

    // ✅ Mark multiple as processed (faster batch)
    @Query("UPDATE raw_events SET processed = 1 WHERE id IN (:ids)")
    suspend fun markProcessed(ids: List<String>)

    // ✅ Find by sourceId (dedup check)
    @Query("SELECT * FROM raw_events WHERE sourceId = :sourceId LIMIT 1")
    suspend fun findBySourceId(sourceId: String): RawEventEntity?

    // ✅ Delete all (useful for testing)
    @Query("DELETE FROM raw_events")
    suspend fun clearAll()

    // ✅ Count total events
    @Query("SELECT COUNT(*) FROM raw_events")
    suspend fun count(): Int

    // ✅ Count unprocessed events
    @Query("SELECT COUNT(*) FROM raw_events WHERE processed = 0")
    suspend fun countUnprocessed(): Int

    @Query("""
    SELECT * FROM raw_events 
    ORDER BY receivedAt DESC 
    LIMIT :limit
""")
    suspend fun getLatestEvents(limit: Int): List<RawEventEntity>
}