package com.example.exp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raw_events")
data class RawEventEntity(
    @PrimaryKey
    val id: String, // UUID

    val source: String, // SMS / NOTIFICATION / MANUAL

    val sourceId: String, // SMS id or notification unique id (for dedup)

    val rawText: String, // full message text

    val sender: String?, // bank name / app name

    val receivedAt: Long, // when device received it

    val eventTime: Long?, // actual transaction time if available

    val processed: Boolean = false, // parsed or not

    val createdAt: Long = System.currentTimeMillis()

)
