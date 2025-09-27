package com.wisme.firstapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Room entity for storing episode information locally
 */
@Entity(
    tableName = "episodes",
    indices = [
        Index(value = ["episodeId"], unique = true),
        Index(value = ["journeyId"])
    ]
)
data class EpisodeEntity(
    @PrimaryKey
    val episodeId: String,
    val journeyId: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val orderIndex: Int,
    val audioFilePath: String,
    val audioUrl: String? = null, // Streaming URL from backend
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Int = 1
)