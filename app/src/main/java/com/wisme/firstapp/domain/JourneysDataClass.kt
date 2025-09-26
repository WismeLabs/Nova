package com.wisme.firstapp.domain

data class JourneysDataClass(
    val JourneyName: String,
    val JourneyDescription: String,
    val JourneyImg: String,
    val totalDurationMinutes: Int = 0, // Dynamic duration calculated from episodes
    val episodes: List<EpisodeDataClass> = emptyList()
)
