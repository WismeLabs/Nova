package com.wisme.research.domain

data class EpisodeDataClass(
    val episodeNumber: Int,
    val title: String,
    val description: String,
    val durationMinutes: Int = 0, // Dynamic duration from audio file
    val audioUrl: String = "", // URL to audio file from Aura backend
    val isCompleted: Boolean = false // Track user progress
)
