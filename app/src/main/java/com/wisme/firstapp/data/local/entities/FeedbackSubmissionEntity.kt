package com.wisme.firstapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.wisme.firstapp.data.repository.FeedbackResponse

@Entity(tableName = "feedback_submissions")
data class FeedbackSubmissionEntity(
    @PrimaryKey
    val submissionId: String,
    val feedbackType: String, // "episode", "journey", "general"
    val contextId: String, // episode_id, journey_id, or "general"
    val userId: String,
    val responses: List<FeedbackResponseLocal>,
    val submittedAt: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING, // Track sync with backend
    val version: Int = 1 // For conflict resolution
)

/**
 * Local representation of FeedbackResponse for Room storage
 */
@kotlinx.serialization.Serializable
data class FeedbackResponseLocal(
    val questionId: String,
    val responseValue: String
)

/**
 * Sync status for offline-first functionality
 */
enum class SyncStatus {
    PENDING,    // Not yet synced to backend
    SYNCED,     // Successfully synced to backend
    FAILED,     // Sync failed, needs retry
    CONFLICT    // Conflict detected, needs resolution
}

/**
 * Convert API FeedbackResponse to local FeedbackResponseLocal
 */
fun FeedbackResponse.toLocal(): FeedbackResponseLocal {
    return FeedbackResponseLocal(
        questionId = this.question_id,
        responseValue = this.response_value
    )
}

/**
 * Convert local FeedbackResponseLocal to API FeedbackResponse
 */
fun FeedbackResponseLocal.toApiModel(): FeedbackResponse {
    return FeedbackResponse(
        question_id = this.questionId,
        response_value = this.responseValue
    )
}



/**
 * Updated type converter using serializable version
 */
class SafeFeedbackResponseListConverter {
    private val json = kotlinx.serialization.json.Json
    
    @androidx.room.TypeConverter
    fun fromResponseList(value: List<FeedbackResponseLocal>?): String? {
        return value?.let { responses ->
            json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(FeedbackResponseLocal.serializer()), 
                responses
            )
        }
    }
    
    @androidx.room.TypeConverter
    fun toResponseList(value: String?): List<FeedbackResponseLocal>? {
        return value?.let { 
            json.decodeFromString(
                kotlinx.serialization.builtins.ListSerializer(FeedbackResponseLocal.serializer()), 
                it
            )
        }
    }
}