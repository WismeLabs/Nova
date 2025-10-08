package com.wisme.firstapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Room entity for storing user progress with episodes and journeys
 */
@Entity(
    tableName = "user_progress",
    indices = [
        Index(value = ["userId", "journeyId", "episodeId"], unique = true),
        Index(value = ["userId"]),
        Index(value = ["journeyId"]),
        Index(value = ["episodeId"])
    ]
)
data class UserProgressEntity(
    @PrimaryKey
    val progressId: String, // UUID generated locally
    val userId: String, // Firebase UID
    val journeyId: String,
    val episodeId: String,
    val progressPercentage: Float = 0f,
    val playPositionSeconds: Long = 0L,
    val status: String = "not_started", // not_started, in_progress, completed
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val totalTimeSpentSeconds: Long = 0L, // Total time spent listening to this episode
    val replayCount: Int = 0, // How many times user replayed this episode
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Int = 1
) {
    companion object {
        const val STATUS_NOT_STARTED = "not_started"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_COMPLETED = "completed"
        
        // Consider episode completed when it reaches 90% or higher
        const val COMPLETION_THRESHOLD = 90f
    }
    
    val isCompleted: Boolean
        get() = progressPercentage >= COMPLETION_THRESHOLD || status == STATUS_COMPLETED
    
    val isStarted: Boolean
        get() = status != STATUS_NOT_STARTED
}
