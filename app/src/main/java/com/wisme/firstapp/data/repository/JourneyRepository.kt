package com.wisme.firstapp.data.repository

import com.wisme.firstapp.data.api.AuraApiService
import com.wisme.firstapp.data.api.EpisodeApiModel
import com.wisme.firstapp.data.api.JourneyApiModel
import com.wisme.firstapp.domain.EpisodeDataClass
import com.wisme.firstapp.domain.JourneysDataClass
import com.wisme.firstapp.util.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JourneyRepository @Inject constructor(
    private val apiService: AuraApiService,
    private val connectivityRepository: ConnectivityRepository
) {
    
    /**
     * Fetch all available journeys from the Aura backend
     */
    suspend fun getAllJourneys(token: String?, useTestEndpoint: Boolean = false): Result<List<JourneysDataClass>> {
        Logger.logJourney("Fetch All Journeys")
        Logger.logRepository(
            repository = "JourneyRepository",
            operation = "getAllJourneys",
            additionalData = mapOf(
                "useTestEndpoint" to useTestEndpoint,
                "hasToken" to !token.isNullOrEmpty()
            )
        )
        
        return try {
            // Check backend connectivity first
            val backendStatus = connectivityRepository.checkBackendHealth()
            if (!backendStatus.isHealthy) {
                val errorMessage = "Backend unavailable: ${backendStatus.message}"
                Logger.logJourney(
                    operation = "Fetch All Journeys",
                    success = false,
                    errorMessage = errorMessage
                )
                Logger.logRepository(
                    repository = "JourneyRepository",
                    operation = "getAllJourneys",
                    success = false,
                    errorMessage = errorMessage
                )
                return Result.failure(Exception(errorMessage))
            }
            
            val response = if (useTestEndpoint) {
                Logger.d("Using test endpoint for journeys", "JOURNEY_REPO")
                // Use test endpoint for development
                apiService.getAllJourneysTest()
            } else {
                // Use production endpoint with authentication
                if (token.isNullOrEmpty()) {
                    val errorMessage = "Authentication token required"
                    Logger.logJourney(
                        operation = "Fetch All Journeys",
                        success = false,
                        errorMessage = errorMessage
                    )
                    return Result.failure(Exception(errorMessage))
                }
                Logger.d("Using authenticated endpoint for journeys", "JOURNEY_REPO")
                apiService.getAllJourneys("Bearer $token")
            }
            
            if (response.isSuccessful && response.body()?.success == true) {
                val journeys = response.body()!!.journeys.map { journeyWrapper ->
                    mapApiJourneyToDataClass(journeyWrapper.journey)
                }
                Logger.logJourney(
                    operation = "Fetch All Journeys",
                    success = true
                )
                Logger.logRepository(
                    repository = "JourneyRepository",
                    operation = "getAllJourneys",
                    success = true,
                    additionalData = mapOf("journeyCount" to journeys.size)
                )
                Result.success(journeys)
            } else {
                val errorMessage = "Failed to fetch journeys: ${response.code()} ${response.message()}"
                Logger.logJourney(
                    operation = "Fetch All Journeys",
                    success = false,
                    errorMessage = errorMessage
                )
                Logger.logRepository(
                    repository = "JourneyRepository",
                    operation = "getAllJourneys",
                    success = false,
                    errorMessage = errorMessage
                )
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.logJourney(
                operation = "Fetch All Journeys",
                success = false,
                errorMessage = "Exception: ${e.message}"
            )
            Logger.logRepository(
                repository = "JourneyRepository",
                operation = "getAllJourneys",
                success = false,
                errorMessage = "Exception: ${e.message}",
                additionalData = mapOf("exception" to e.javaClass.simpleName)
            )
            Result.failure(e)
        }
    }
    
    /**
     * Fetch detailed journey information including episodes
     */
    suspend fun getJourneyWithEpisodes(token: String, journeyId: String): Result<JourneysDataClass> {
        return try {
            val response = apiService.getJourneyById("Bearer $token", journeyId)
            if (response.isSuccessful && response.body()?.success == true) {
                val journeyData = response.body()!!
                val journey = mapApiJourneyToDataClass(journeyData.journey)
                val episodes = journeyData.episodes.map { apiEpisode ->
                    mapApiEpisodeToDataClass(apiEpisode)
                }
                
                val totalDuration = episodes.sumOf { it.durationMinutes }
                val updatedJourney = journey.copy(
                    episodes = episodes,
                    totalDurationMinutes = totalDuration
                )
                
                Result.success(updatedJourney)
            } else {
                Result.failure(Exception("Failed to fetch journey details: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetch episodes for a specific journey
     */
    suspend fun getJourneyEpisodes(token: String, journeyId: String): Result<List<EpisodeDataClass>> {
        return try {
            val response = apiService.getJourneyEpisodes("Bearer $token", journeyId)
            if (response.isSuccessful && response.body()?.success == true) {
                val episodes = response.body()!!.episodes.map { apiEpisode ->
                    mapApiEpisodeToDataClass(apiEpisode)
                }
                Result.success(episodes)
            } else {
                Result.failure(Exception("Failed to fetch episodes: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get audio duration for a specific episode
     */
    suspend fun getEpisodeAudioDuration(token: String, episodeId: String): Result<Int> {
        return try {
            val response = apiService.getEpisodeAudioDuration("Bearer $token", episodeId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.durationMinutes)
            } else {
                Result.failure(Exception("Failed to fetch audio duration: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Map API journey model to domain data class
     */
    private fun mapApiJourneyToDataClass(apiJourney: JourneyApiModel): JourneysDataClass {
        val episodes = apiJourney.episodes?.map { mapApiEpisodeToDataClass(it) } ?: emptyList()
        return JourneysDataClass(
            JourneyName = apiJourney.title,
            JourneyDescription = apiJourney.description,
            JourneyImg = apiJourney.journey_id, // Using journey_id as image identifier
            totalDurationMinutes = apiJourney.total_duration_minutes,
            episodes = episodes
        )
    }
    
    /**
     * Map API episode model to domain data class
     */
    private fun mapApiEpisodeToDataClass(apiEpisode: EpisodeApiModel): EpisodeDataClass {
        return EpisodeDataClass(
            episodeNumber = apiEpisode.order_index,
            title = apiEpisode.title,
            description = apiEpisode.description,
            durationMinutes = apiEpisode.duration_minutes,
            audioUrl = "", // Will be populated when episode is started
            isCompleted = false // TODO: Fetch from user progress tracking
        )
    }
}