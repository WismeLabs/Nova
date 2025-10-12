package com.wisme.research.domain

data class JourneysDataClass(
    val JourneyName: String,
    val JourneyDescription: String,
    val JourneyImg: String,
    val journeyId: String, // Database ID for API calls (e.g., "personal_finance")
    val totalDurationMinutes: Int = 0, // Dynamic duration calculated from episodes
    val episodes: List<EpisodeDataClass> = emptyList()
)
