package com.wisme.research.data.model

/**
 * Data class representing a learning journey
 */
data class Journey(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val episodes: List<Episode>,
    val totalDurationMinutes: Int? = null, // Will be calculated from episodes
    val progress: Float = 0f, // Progress percentage (0.0 to 1.0)
    val isCompleted: Boolean = false,
    val thumbnailUrl: String? = null
) {
    /**
     * Calculate total duration from all episodes
     */
    fun calculateTotalDuration(): Int {
        return episodes.sumOf { it.durationMinutes ?: 0 }
    }
    
    /**
     * Get formatted total duration string
     */
    fun getFormattedDuration(): String {
        val totalMinutes = totalDurationMinutes ?: calculateTotalDuration()
        return if (totalMinutes >= 60) {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
        } else {
            "${totalMinutes}m"
        }
    }
    
    /**
     * Check if journey has all episode durations loaded
     */
    fun hasAllDurationsLoaded(): Boolean {
        return episodes.all { it.durationMinutes != null }
    }
}
