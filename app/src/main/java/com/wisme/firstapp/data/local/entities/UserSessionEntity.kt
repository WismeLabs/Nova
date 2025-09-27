package com.wisme.firstapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Room entity for storing user session information locally
 */
@Entity(
    tableName = "user_sessions",
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserSessionEntity(
    @PrimaryKey
    val userId: String, // Firebase UID
    val currentJourneyId: String? = null,
    val currentEpisodeId: String? = null,
    val playbackPosition: Long = 0L, // Current playback position in seconds
    val isPlaying: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val audioQuality: String = "high", // high, medium, low
    val lastActivityAt: Long = System.currentTimeMillis(),
    val sessionStartedAt: Long = System.currentTimeMillis(),
    val totalSessionTimeSeconds: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Int = 1
)