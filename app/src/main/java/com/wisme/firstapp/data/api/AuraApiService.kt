package com.wisme.firstapp.data.api

import retrofit2.Response
import retrofit2.http.*

/**
 * Aura API service interface based on backend documentation
 */
interface AuraApiService {
    
    // Health check endpoints
    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
    
    @GET("auth/health")
    suspend fun authHealthCheck(): Response<HealthResponse>
    
    @GET("users/health/check")
    suspend fun usersHealthCheck(): Response<HealthResponse>
    
    // Authentication endpoints
    @POST("auth/verify-token")
    suspend fun verifyToken(@Body request: VerifyTokenRequest): Response<VerifyTokenResponse>
    
    @GET("auth/profile")
    suspend fun getAuthProfile(@Header("Authorization") token: String): Response<UserProfileResponse>
    
    @GET("auth/status")
    suspend fun getAuthStatus(@Header("Authorization") token: String): Response<AuthStatusResponse>
    
    // User Management endpoints
    @POST("users/profile")
    suspend fun createUserProfile(
        @Header("Authorization") token: String,
        @Body profile: CreateUserProfileRequest
    ): Response<CreateUserProfileResponse>
    
    @GET("users/profile/me")
    suspend fun getMyProfile(@Header("Authorization") token: String): Response<UserProfileResponse>
    
    @PUT("users/profile/me")
    suspend fun updateMyProfile(
        @Header("Authorization") token: String,
        @Body profile: UpdateUserProfileRequest
    ): Response<UserProfileResponse>
    
    @GET("users/user-id")
    suspend fun getUserId(@Header("Authorization") token: String): Response<UserIdResponse>
    
    // Journey endpoints (Production - requires authentication)
    @GET("journeys")
    suspend fun getAllJourneys(@Header("Authorization") token: String): Response<JourneysResponse>
    
    @GET("journeys/{journeyId}")
    suspend fun getJourneyById(
        @Header("Authorization") token: String,
        @Path("journeyId") journeyId: String
    ): Response<JourneyDetailResponse>
    
    @POST("journeys/{journeyId}/episodes/{episodeId}/start")
    suspend fun startEpisode(
        @Header("Authorization") token: String,
        @Path("journeyId") journeyId: String,
        @Path("episodeId") episodeId: String
    ): Response<EpisodeStartResponse>
    
    @PUT("journeys/{journeyId}/episodes/{episodeId}/progress")
    suspend fun updateEpisodeProgress(
        @Header("Authorization") token: String,
        @Path("journeyId") journeyId: String,
        @Path("episodeId") episodeId: String,
        @Body progress: EpisodeProgressRequest
    ): Response<EpisodeProgressResponse>
    
    // Journey endpoints (Test - no authentication required)
    @GET("journeys/test")
    suspend fun getAllJourneysTest(): Response<JourneysResponse>
    
    @GET("journeys/test/{journeyId}")
    suspend fun getJourneyByIdTest(@Path("journeyId") journeyId: String): Response<JourneyDetailResponse>
    
    @POST("journeys/test/{journeyId}/episodes/{episodeId}/start")
    suspend fun startEpisodeTest(
        @Path("journeyId") journeyId: String,
        @Path("episodeId") episodeId: String
    ): Response<EpisodeStartResponse>
    
    @GET("journeys/{journeyId}/episodes")
    suspend fun getJourneyEpisodes(
        @Header("Authorization") token: String,
        @Path("journeyId") journeyId: String
    ): Response<EpisodesResponse>
    
    @GET("episodes/{episodeId}/audio-duration")
    suspend fun getEpisodeAudioDuration(
        @Header("Authorization") token: String,
        @Path("episodeId") episodeId: String
    ): Response<AudioDurationResponse>
}

// Request/Response data classes based on API documentation
data class VerifyTokenRequest(val token: String)

data class VerifyTokenResponse(
    val success: Boolean,
    val message: String,
    val user: UserInfo?
)

data class UserInfo(
    val uid: String,
    val email: String,
    val email_verified: Boolean
)

data class AuthStatusResponse(
    val authenticated: Boolean,
    val user_id: String?,
    val email: String?,
    val email_verified: Boolean?
)

data class CreateUserProfileRequest(
    val avatar_id: Int,
    val name: String,
    val display_name: String,
    val date_of_birth: String,
    val gender: String,
    val profession: String
)

data class CreateUserProfileResponse(
    val success: Boolean,
    val message: String,
    val user_id: String,
    val profile: UserProfile
)

data class UpdateUserProfileRequest(
    val display_name: String?,
    val profession: String?,
    val avatar_id: Int?
)

data class UserProfileResponse(
    val user_id: String,
    val avatar_id: Int,
    val name: String,
    val display_name: String,
    val date_of_birth: String,
    val gender: String,
    val profession: String,
    val created_at: String?
)

data class UserProfile(
    val user_id: String,
    val avatar_id: Int,
    val name: String,
    val display_name: String,
    val date_of_birth: String,
    val gender: String,
    val profession: String,
    val created_at: String
)

data class UserIdResponse(
    val user_id: String,
    val profile_exists: Boolean,
    val profile_complete: Boolean,
    val message: String
)

// Journey API response data classes
data class JourneysResponse(
    val success: Boolean,
    val message: String,
    val journeys: List<JourneyWrapper>,
    val total_count: Int
)

data class JourneyWrapper(
    val journey: JourneyApiModel,
    val user_progress: JourneyProgress
)

data class JourneyApiModel(
    val journey_id: String,
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String,
    val total_duration_minutes: Int,
    val episodes: List<EpisodeApiModel>? = null
)

data class JourneyProgress(
    val completion_percentage: Double,
    val episodes_completed: Int,
    val total_episodes: Int
)

data class JourneyDetailResponse(
    val success: Boolean,
    val journey: JourneyApiModel,
    val episodes: List<EpisodeApiModel>
)

data class EpisodesResponse(
    val success: Boolean,
    val episodes: List<EpisodeApiModel>
)

data class EpisodeApiModel(
    val episode_id: String,
    val title: String,
    val description: String,
    val duration_minutes: Int,
    val order_index: Int,
    val audio_file_path: String
)

data class AudioDurationResponse(
    val success: Boolean,
    val durationMinutes: Int,
    val durationSeconds: Int
)

// Health check response
data class HealthResponse(
    val status: String,
    val timestamp: String? = null
)

// Episode progress and start responses
data class EpisodeStartResponse(
    val success: Boolean,
    val message: String,
    val episode: EpisodeDetail,
    val audio_url: String,
    val user_progress: UserProgress
)

data class EpisodeDetail(
    val episode_id: String,
    val title: String,
    val description: String,
    val duration_minutes: Int,
    val order_index: Int,
    val audio_file_path: String
)

data class UserProgress(
    val user_id: String,
    val journey_id: String,
    val episode_id: String,
    val status: String,
    val progress_percentage: Double,
    val play_position_seconds: Int,
    val started_at: String
)

data class EpisodeProgressRequest(
    val progress_percentage: Double,
    val play_position_seconds: Int
)

data class EpisodeProgressResponse(
    val success: Boolean,
    val message: String,
    val user_progress: UserProgress
)
