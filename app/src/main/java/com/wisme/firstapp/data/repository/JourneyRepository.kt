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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JourneyRepository @Inject constructor(
    private val apiService: AuraApiService,
    private val connectivityRepository: ConnectivityRepository,
    private val authPrefs: AuthPreferences,
    private val firebaseAuth: FirebaseAuth
) {
    
    /**
     * Refresh Firebase token when 401 error occurs
     */
    private suspend fun refreshTokenAndRetry(): String? {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val freshToken = currentUser.getIdToken(true).await().token
                if (freshToken != null) {
                    authPrefs.firebaseToken = freshToken
                    Logger.d("JourneyRepository: Token refreshed successfully", "JOURNEY_REPO")
                    freshToken
                } else {
                    Logger.e("JourneyRepository: Failed to get fresh token - null returned", "JOURNEY_REPO")
                    null
                }
            } else {
                Logger.e("JourneyRepository: No current user for token refresh", "JOURNEY_REPO")
                null
            }
        } catch (e: Exception) {
            Logger.e("JourneyRepository: Token refresh failed - ${e.message}", "JOURNEY_REPO")
            null
        }
    }
    
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
                // Handle 401 errors with automatic token refresh
                if (response.code() == 401 && !useTestEndpoint) {
                    Logger.d("JourneyRepository: 401 error - attempting token refresh", "JOURNEY_REPO")
                    val refreshedToken = refreshTokenAndRetry()
                    if (refreshedToken != null) {
                        // Retry the request with refreshed token
                        val retryResponse = apiService.getAllJourneys("Bearer $refreshedToken")
                        if (retryResponse.isSuccessful && retryResponse.body()?.success == true) {
                            val journeys = retryResponse.body()!!.journeys.map { journeyWrapper ->
                                journeyWrapper.journey.toDomainModel()
                            }
                            Logger.d("JourneyRepository: Token refresh retry successful", "JOURNEY_REPO")
                            return Result.success(journeys)
                        }
                    }
                }
                
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
            } else if (response.code() == 401) {
                // Token expired, try to refresh
                firebaseAuth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
                    // This will be handled in the retry mechanism
                }
                
                try {
                    val refreshedToken = firebaseAuth.currentUser?.getIdToken(true)?.result?.token
                    if (refreshedToken != null) {
                        val retryResponse = apiService.getJourneyById("Bearer $refreshedToken", journeyId)
                        if (retryResponse.isSuccessful && retryResponse.body()?.success == true) {
                            val journeyData = retryResponse.body()!!
                            val journey = mapApiJourneyToDataClass(journeyData.journey)
                            val episodes = journeyData.episodes.map { apiEpisode ->
                                mapApiEpisodeToDataClass(apiEpisode)
                            }
                            
                            val totalDuration = episodes.sumOf { it.durationMinutes }
                            val updatedJourney = journey.copy(
                                episodes = episodes,
                                totalDurationMinutes = totalDuration
                            )
                            
                            return Result.success(updatedJourney)
                        }
                    }
                } catch (e: Exception) {
                    // Token refresh failed, return original error
                }
                
                Result.failure(Exception("Failed to fetch journey details: ${response.message()}"))
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
            } else if (response.code() == 401) {
                // Token expired, try to refresh
                try {
                    val refreshedToken = firebaseAuth.currentUser?.getIdToken(true)?.result?.token
                    if (refreshedToken != null) {
                        val retryResponse = apiService.getJourneyEpisodes("Bearer $refreshedToken", journeyId)
                        if (retryResponse.isSuccessful && retryResponse.body()?.success == true) {
                            val episodes = retryResponse.body()!!.episodes.map { apiEpisode ->
                                mapApiEpisodeToDataClass(apiEpisode)
                            }
                            return Result.success(episodes)
                        }
                    }
                } catch (e: Exception) {
                    // Token refresh failed, return original error
                }
                
                Result.failure(Exception("Failed to fetch episodes: ${response.message()}"))
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
            } else if (response.code() == 401) {
                // Token expired, try to refresh
                try {
                    val refreshedToken = firebaseAuth.currentUser?.getIdToken(true)?.result?.token
                    if (refreshedToken != null) {
                        val retryResponse = apiService.getEpisodeAudioDuration("Bearer $refreshedToken", episodeId)
                        if (retryResponse.isSuccessful && retryResponse.body()?.success == true) {
                            return Result.success(retryResponse.body()!!.durationMinutes)
                        }
                    }
                } catch (e: Exception) {
                    // Token refresh failed, return original error
                }
                
                Result.failure(Exception("Failed to fetch audio duration: ${response.message()}"))
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
            journeyId = apiJourney.journey_id, // Database ID for API calls
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
                val rawAudioUrl = body.audio_url ?: ""
                
                // Convert relative path to full URL
                val audioUrl = if (rawAudioUrl.isNotEmpty() && !rawAudioUrl.startsWith("http")) {
                    "https://aura-backend-ok92.onrender.com/$rawAudioUrl"
                } else {
                    rawAudioUrl
                }
                
                println("JourneyRepository: Raw audio URL from API: '$rawAudioUrl'")
                println("JourneyRepository: Full audio URL: '$audioUrl'")
                
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
            } else if (!useTestEndpoint && response.code() == 401) {
                // Token expired, try to refresh
                try {
                    val refreshedToken = firebaseAuth.currentUser?.getIdToken(true)?.result?.token
                    if (refreshedToken != null) {
                        val retryResponse = apiService.startEpisode("Bearer $refreshedToken", journeyId, episodeId)
                        if (retryResponse.isSuccessful && retryResponse.body()?.success == true) {
                            val body = retryResponse.body()!!
                            val rawAudioUrl = body.audio_url ?: ""
                            
                            // Convert relative path to full URL
                            val audioUrl = if (rawAudioUrl.isNotEmpty() && !rawAudioUrl.startsWith("http")) {
                                "https://aura-backend-ok92.onrender.com/$rawAudioUrl"
                            } else {
                                rawAudioUrl
                            }
                            
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
                            
                            return Result.success(Pair(audioUrl, progress))
                        }
                    }
                } catch (e: Exception) {
                    // Token refresh failed, return original error
                }
                
                val errorMessage = "Failed to start episode: ${response.code()} ${response.message()}"
                Logger.logJourney(
                    operation = "Start Episode",
                    journeyName = journeyId,
                    success = false,
                    errorMessage = errorMessage
                )
                Result.failure(Exception(errorMessage))
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
     * Start episode and get streaming URL from backend
     */
    suspend fun getEpisodeStreamingUrl(
        token: String, 
        journeyId: String, 
        episodeId: String,
        useTestEndpoint: Boolean = false
    ): Result<String> {
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
                val audioUrl = response.body()!!.audio_url
                println("JourneyRepository: Got streaming URL: $audioUrl")
                Result.success(audioUrl)
            } else if (!useTestEndpoint && response.code() == 401) {
                // Token expired, try to refresh
                try {
                    val refreshedToken = firebaseAuth.currentUser?.getIdToken(true)?.result?.token
                    if (refreshedToken != null) {
                        val retryResponse = apiService.startEpisode("Bearer $refreshedToken", journeyId, episodeId)
                        if (retryResponse.isSuccessful && retryResponse.body()?.success == true) {
                            val audioUrl = retryResponse.body()!!.audio_url
                            println("JourneyRepository: Got streaming URL after token refresh: $audioUrl")
                            return Result.success(audioUrl)
                        }
                    }
                } catch (e: Exception) {
                    // Token refresh failed, return original error
                }
                
                val errorMessage = "Failed to get streaming URL: ${response.code()} ${response.message()}"
                println("JourneyRepository: $errorMessage")
                Result.failure(Exception(errorMessage))
            } else {
                val errorMessage = "Failed to get streaming URL: ${response.code()} ${response.message()}"
                println("JourneyRepository: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            println("JourneyRepository: Exception getting streaming URL: ${e.message}")
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
                    // CRITICAL: Return failure when no token available - backend sync impossible
                    Logger.logJourney(
                        operation = "Update Progress",
                        journeyName = journeyId,
                        success = false,
                        errorMessage = "No authentication token available"
                    )
                    return Result.failure(Exception("Backend sync failed: No authentication token. Progress saved locally but not synced to server."))
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
                // Handle 401 errors with automatic token refresh
                if (response.code() == 401 && !useTestEndpoint) {
                    Logger.d("JourneyRepository: 401 error on progress update - attempting token refresh", "JOURNEY_REPO")
                    val refreshedToken = refreshTokenAndRetry()
                    if (refreshedToken != null) {
                        // Retry the request with refreshed token
                        val retryResponse = apiService.updateEpisodeProgress("Bearer $refreshedToken", journeyId, episodeId, requestBody)
                        if (retryResponse.isSuccessful) {
                            Logger.logJourney(
                                operation = "Update Progress",
                                journeyName = journeyId,
                                success = true,
                                errorMessage = "Token refresh retry successful"
                            )
                            return Result.success(progress)
                        }
                    }
                }
                
                Logger.logJourney(
                    operation = "Update Progress",
                    journeyName = journeyId,
                    success = false,
                    errorMessage = "Backend sync failed: ${response.code()}"
                )
                
                // CRITICAL: Return failure when backend sync fails!
                // Local storage succeeded but backend sync failed = partial failure
                Result.failure(Exception("Backend sync failed: HTTP ${response.code()}. Progress saved locally but not synced to server."))
            }
        } catch (e: Exception) {
            Logger.logJourney(
                operation = "Update Progress",
                journeyName = journeyId,
                success = false,
                errorMessage = "Exception: ${e.message}"
            )
            
            // CRITICAL: Return failure when exception occurs!
            // Local storage may have succeeded but we can't guarantee backend sync
            Result.failure(Exception("Progress sync failed: ${e.message}. Progress may be saved locally but backend sync uncertain."))
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
            audioUrl = "", // Don't use relative path - get signed URL from start episode endpoint
            isCompleted = progress?.isCompleted ?: false
        )
    }
    
    /**
     * Record listen event analytics (including completion) to backend
     */
    suspend fun recordListenEvent(
        token: String,
        episodeId: String,
        eventType: String,
        timestamp: String,
        playPositionSeconds: Int,
        sessionId: String?
    ): Result<Unit> {
        return try {
            val request = com.wisme.firstapp.data.api.ListenAnalyticsRequest(
                event_type = eventType,
                timestamp = timestamp,
                play_position_seconds = playPositionSeconds,
                session_id = sessionId
            )
            
            val response = apiService.recordListenEvent(episodeId, request)
            
            if (response.isSuccessful) {
                Logger.logRepository(
                    repository = "JourneyRepository",
                    operation = "recordListenEvent",
                    additionalData = mapOf(
                        "episodeId" to episodeId,
                        "eventType" to eventType,
                        "success" to true
                    )
                )
                Result.success(Unit)
            } else {
                // Handle 401 errors with automatic token refresh
                if (response.code() == 401) {
                    Logger.d("JourneyRepository: 401 error on listen event - attempting token refresh", "JOURNEY_REPO")
                    val refreshedToken = refreshTokenAndRetry()
                    if (refreshedToken != null) {
                        // Retry the request with refreshed token
                        val retryResponse = apiService.recordListenEvent(episodeId, request)
                        if (retryResponse.isSuccessful) {
                            Logger.logRepository(
                                repository = "JourneyRepository",
                                operation = "recordListenEvent",
                                additionalData = mapOf(
                                    "episodeId" to episodeId,
                                    "eventType" to eventType,
                                    "success" to true,
                                    "tokenRefreshRetry" to true
                                )
                            )
                            return Result.success(Unit)
                        }
                    }
                }
                
                Logger.logRepository(
                    repository = "JourneyRepository",
                    operation = "recordListenEvent",
                    additionalData = mapOf(
                        "episodeId" to episodeId,
                        "eventType" to eventType,
                        "success" to false,
                        "error" to "HTTP ${response.code()}"
                    )
                )
                Result.failure(Exception("Failed to record listen event: ${response.code()}"))
            }
        } catch (e: Exception) {
            Logger.logRepository(
                repository = "JourneyRepository",
                operation = "recordListenEvent",
                additionalData = mapOf(
                    "episodeId" to episodeId,
                    "eventType" to eventType,
                    "success" to false,
                    "error" to e.message
                )
            )
            Result.failure(e)
        }
    }

    /**
     * Map API episode model to domain data class
     */
    private fun mapApiEpisodeToDataClass(apiEpisode: EpisodeApiModel): EpisodeDataClass {
        return mapApiEpisodeToDataClass(apiEpisode, null)
    }
}
