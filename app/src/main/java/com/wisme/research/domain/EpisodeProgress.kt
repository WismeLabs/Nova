package com.wisme.research.domain

/**
 * Data class to represent episode progress
 */
data class EpisodeProgress(
    val progressPercentage: Float = 0f,
    val playPositionSeconds: Long = 0L,
    val status: String = "not_started", // not_started, in_progress, completed
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_NOT_STARTED = "not_started"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_COMPLETED = "completed"
        
        // Consider episode completed when it reaches 90% or higher
        const val COMPLETION_THRESHOLD = 90f
    }
    
    val isCompleted: Boolean
        get() = progressPercentage >= COMPLETION_THRESHOLD
    
    val isStarted: Boolean
        get() = status != STATUS_NOT_STARTED
}
