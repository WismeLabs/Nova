package com.wisme.firstapp.data.model

/**
 * Data class representing an episode within a journey
 */
data class Episode(
    val id: Int,
    val journeyId: Int,
    val title: String,
    val audioFileName: String,
    val audioUrl: String? = null, // Backend URL for the audio file
    val durationMinutes: Int? = null, // Will be fetched from backend
    val durationSeconds: Int? = null, // Precise duration in seconds
    val isCompleted: Boolean = false,
    val progress: Float = 0f, // Progress percentage (0.0 to 1.0)
    val episodeNumber: Int,
    val description: String? = null
) {
    /**
     * Get formatted duration string
     */
    fun getFormattedDuration(): String {
        return when {
            durationMinutes != null -> {
                if (durationMinutes >= 60) {
                    val hours = durationMinutes / 60
                    val minutes = durationMinutes % 60
                    if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
                } else {
                    "${durationMinutes}m"
                }
            }
            durationSeconds != null -> {
                val minutes = durationSeconds / 60
                val seconds = durationSeconds % 60
                if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
            }
            else -> "Loading..."
        }
    }
    
    /**
     * Check if duration is loaded from backend
     */
    fun isDurationLoaded(): Boolean {
        return durationMinutes != null || durationSeconds != null
    }
}