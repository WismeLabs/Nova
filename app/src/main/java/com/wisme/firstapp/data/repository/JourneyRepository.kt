package com.wisme.firstapp.data.repository

import com.wisme.firstapp.data.api.AuraApiService
import com.wisme.firstapp.data.api.EpisodeApiModel
import com.wisme.firstapp.data.api.JourneyApiModel
import com.wisme.firstapp.data.api.EpisodeProgressRequest
import com.wisme.firstapp.domain.EpisodeDataClass
import com.wisme.firstapp.domain.JourneysDataClass
import com.wisme.firstapp.domain.EpisodeProgress
import com.wisme.firstapp.data.local.AuthPreferences
import com.wisme.firstapp.util.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JourneyRepository @Inject constructor(
    private val apiService: AuraApiService,
    private val connectivityRepository: ConnectivityRepository,
    private val authPrefs: AuthPreferences
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
     * Start an episode and register user progress
     */
    suspend fun startEpisode(
        token: String, 
        journeyId: String, 
        episodeId: String,
        useTestEndpoint: Boolean = false
    ): Result<Pair<String, EpisodeProgress>> {
        Logger.logJourney("Start Episode", journeyId)
        Logger.logRepository(
            repository = "JourneyRepository",
            operation = "startEpisode",
            additionalData = mapOf(
                "journeyId" to journeyId,
                "episodeId" to episodeId,
                "useTestEndpoint" to useTestEndpoint
            )
        )
        
        return try {
            val response = if (useTestEndpoint) {
                apiService.startEpisodeTest(journeyId, episodeId)
            } else {
                if (token.isEmpty()) {
                    return Result.failure(Exception("Authentication token required"))
                }
                apiService.startEpisode("Bearer $token", journeyId, episodeId)
            }
            
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                val audioUrl = body.audio_url ?: ""
                
                // Create progress object
                val progress = EpisodeProgress(
                    progressPercentage = 0f,
                    playPositionSeconds = 0L,
                    status = EpisodeProgress.STATUS_IN_PROGRESS,
                    lastUpdated = System.currentTimeMillis()
                )
                
                // Store progress locally
                authPrefs.setEpisodeProgress(journeyId, episodeId, progress)
                
                Logger.logJourney(
                    operation = "Start Episode",
                    journeyName = journeyId,
                    success = true
                )
                
                Result.success(Pair(audioUrl, progress))
            } else {
                val errorMessage = "Failed to start episode: ${response.code()} ${response.message()}"
                Logger.logJourney(
                    operation = "Start Episode",
                    journeyName = journeyId,
                    success = false,
                    errorMessage = errorMessage
                )
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.logJourney(
                operation = "Start Episode",
                journeyName = journeyId,
                success = false,
                errorMessage = "Exception: ${e.message}"
            )
            Result.failure(e)
        }
    }
    
    /**
     * Update episode progress both locally and on backend
     */
    suspend fun updateEpisodeProgress(
        token: String,
        journeyId: String,
        episodeId: String,
        progressPercentage: Float,
        playPositionSeconds: Long,
        useTestEndpoint: Boolean = false
    ): Result<EpisodeProgress> {
        Logger.logJourney(
            operation = "Update Progress",
            journeyName = journeyId
        )
        Logger.logRepository(
            repository = "JourneyRepository",
            operation = "updateEpisodeProgress",
            additionalData = mapOf(
                "journeyId" to journeyId,
                "episodeId" to episodeId,
                "progressPercentage" to progressPercentage,
                "playPositionSeconds" to playPositionSeconds
            )
        )
        
        // Determine episode status
        val status = when {
            progressPercentage >= EpisodeProgress.COMPLETION_THRESHOLD -> EpisodeProgress.STATUS_COMPLETED
            progressPercentage > 0f -> EpisodeProgress.STATUS_IN_PROGRESS
            else -> EpisodeProgress.STATUS_NOT_STARTED
        }
        
        val progress = EpisodeProgress(
            progressPercentage = progressPercentage,
            playPositionSeconds = playPositionSeconds,
            status = status,
            lastUpdated = System.currentTimeMillis()
        )
        
        return try {
            // Update local storage first (offline-first approach)
            authPrefs.setEpisodeProgress(journeyId, episodeId, progress)
            
            // Then sync with backend
            val requestBody = EpisodeProgressRequest(
                progress_percentage = progressPercentage.toDouble(),
                play_position_seconds = playPositionSeconds.toInt()
            )
            
            val response = if (useTestEndpoint) {
                apiService.updateEpisodeProgressTest(journeyId, episodeId, requestBody)
            } else {
                if (token.isEmpty()) {
                    // Return success since we stored locally
                    return Result.success(progress)
                }
                apiService.updateEpisodeProgress("Bearer $token", journeyId, episodeId, requestBody)
            }
            
            if (response.isSuccessful) {
                Logger.logJourney(
                    operation = "Update Progress",
                    journeyName = journeyId,
                    success = true
                )
                Result.success(progress)
            } else {
                Logger.logJourney(
                    operation = "Update Progress",
                    journeyName = journeyId,
                    success = false,
                    errorMessage = "Backend sync failed: ${response.code()}"
                )
                // Still return success since local storage succeeded
                Result.success(progress)
            }
        } catch (e: Exception) {
            Logger.logJourney(
                operation = "Update Progress",
                journeyName = journeyId,
                success = false,
                errorMessage = "Exception: ${e.message}"
            )
            // Still return success since local storage succeeded
            Result.success(progress)
        }
    }
    
    /**
     * Get continue listening data (latest played episode)
     */
    suspend fun getContinueLearning(): Result<Pair<String, String>?> {
        return try {
            val journeyId = authPrefs.currentPlayingJourneyId
            val episodeId = authPrefs.currentPlayingEpisodeId
            
            if (!journeyId.isNullOrEmpty() && !episodeId.isNullOrEmpty()) {
                Result.success(Pair(journeyId, episodeId))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Map API episode model to domain data class with progress information
     */
    private fun mapApiEpisodeToDataClass(apiEpisode: EpisodeApiModel, journeyId: String? = null): EpisodeDataClass {
        // Check local progress if journeyId is provided
        val progress = journeyId?.let { 
            authPrefs.getEpisodeProgress(it, apiEpisode.episode_id ?: "episode_${apiEpisode.order_index}")
        }
        
        return EpisodeDataClass(
            episodeNumber = apiEpisode.order_index,
            title = apiEpisode.title,
            description = apiEpisode.description,
            durationMinutes = apiEpisode.duration_minutes,
            audioUrl = "", // Will be populated when episode is started
            isCompleted = progress?.isCompleted ?: false
        )
    }
    
    /**
     * Map API episode model to domain data class
     */
    private fun mapApiEpisodeToDataClass(apiEpisode: EpisodeApiModel): EpisodeDataClass {
        return mapApiEpisodeToDataClass(apiEpisode, null)
    }
}