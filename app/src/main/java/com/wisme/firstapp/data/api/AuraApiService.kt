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
    
    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<UserProfileResponse>
    
    @GET("auth/claims")
    suspend fun getUserClaims(@Header("Authorization") token: String): Response<UserClaimsResponse>
    
    @POST("auth/revoke-tokens")
    suspend fun revokeTokens(
        @Header("Authorization") token: String,
        @Body request: RevokeTokensRequest
    ): Response<RevokeTokensResponse>
    
    @POST("auth/login")
    suspend fun loginInfo(@Body request: LoginInfoRequest): Response<LoginInfoResponse>
    
    // User Management endpoints
    @POST("users/profile")
    suspend fun createUserProfile(
        @Header("Authorization") token: String,
        @Body profile: CreateUserProfileRequest
    ): Response<CreateUserProfileResponse>
    
    @GET("users/profile/me")
    suspend fun getMyProfile(@Header("Authorization") token: String): Response<UserProfile>
    
    @PUT("users/profile/me")
    suspend fun updateMyProfile(
        @Header("Authorization") token: String,
        @Body profile: UpdateUserProfileRequest
    ): Response<UserProfileResponse>
    
    @GET("users/user-id")
    suspend fun getUserId(@Header("Authorization") token: String): Response<UserIdResponse>
    
    @GET("users/verify/{user_id}")
    suspend fun verifyUserId(@Path("user_id") userId: String): Response<UserVerificationResponse>
    
    @GET("users/{user_id}")
    suspend fun getUserProfile(@Path("user_id") userId: String): Response<UserProfileResponse>
    
    @GET("users/{user_id}/progress")
    suspend fun getUserProgress(
        @Header("Authorization") token: String,
        @Path("user_id") userId: String
    ): Response<UserProgressResponse>
    
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
    
    @PUT("journeys/test/{journeyId}/episodes/{episodeId}/progress")
    suspend fun updateEpisodeProgressTest(
        @Path("journeyId") journeyId: String,
        @Path("episodeId") episodeId: String,
        @Body progress: EpisodeProgressRequest
    ): Response<EpisodeProgressResponse>
    
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
    
    // Topic Request endpoints
    @POST("topics/requests")
    suspend fun submitTopicRequest(
        @Header("Authorization") token: String,
        @Body request: TopicRequestSubmission
    ): Response<TopicRequestResponse>
    
    @GET("topics/requests/popular")
    suspend fun getPopularTopicRequests(): Response<PopularTopicsResponse>
    
    // Feedback System endpoints
    @POST("feedback/submit")
    suspend fun submitFeedback(
        @Header("Authorization") token: String,
        @Body feedback: FeedbackSubmissionRequest
    ): Response<FeedbackSubmissionResponse>
    
    @GET("feedback/questions/{feedback_type}")
    suspend fun getFeedbackQuestions(
        @Path("feedback_type") feedbackType: String
    ): Response<List<FeedbackQuestionResponse>>
    
    @GET("feedback/my-submissions")
    suspend fun getMyFeedbackSubmissions(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<List<FeedbackSubmissionResponse>>
    
    @DELETE("feedback/my-submissions/{submission_id}")
    suspend fun deleteFeedbackSubmission(
        @Header("Authorization") token: String,
        @Path("submission_id") submissionId: String
    ): Response<FeedbackDeleteResponse>
    
    @GET("feedback/analytics/{feedback_type}")
    suspend fun getFeedbackAnalytics(
        @Path("feedback_type") feedbackType: String,
        @Query("context_id") contextId: String? = null
    ): Response<FeedbackAnalyticsResponse>
    
    @GET("feedback/by-context/{feedback_type}/{context_id}")
    suspend fun getFeedbackByContext(
        @Path("feedback_type") feedbackType: String,
        @Path("context_id") contextId: String
    ): Response<FeedbackByContextResponse>
    
    // Podcast Management endpoints
    @GET("podcasts/")
    suspend fun getPodcasts(
        @Query("limit") limit: Int = 10
    ): Response<PodcastsResponse>
    
    @GET("podcasts/{podcast_id}")
    suspend fun getPodcastDetails(
        @Path("podcast_id") podcastId: String
    ): Response<PodcastDetailsResponse>
    
    @GET("podcasts/{podcast_id}/episodes")
    suspend fun getPodcastEpisodes(
        @Path("podcast_id") podcastId: String,
        @Query("limit") limit: Int = 10
    ): Response<PodcastEpisodesResponse>
    
    @POST("podcasts/feedback")
    suspend fun submitPodcastFeedback(
        @Body feedback: PodcastFeedbackRequest
    ): Response<PodcastFeedbackResponse>
    
    @GET("podcasts/recommendations")
    suspend fun getPodcastRecommendations(): Response<PodcastRecommendationsResponse>
    
    // Suggested Topics endpoints (Backend specification)
    @GET("suggested-topics/")
    suspend fun getSuggestedTopics(
        @Query("limit") limit: Int = 5
    ): Response<SuggestedTopicsResponse>
    
    @POST("suggested-topics/increment/{topic_name}")
    suspend fun incrementTopicCount(
        @Header("Authorization") token: String,
        @Path("topic_name") topicName: String,
        @Query("increment") increment: Int = 1
    ): Response<TopicIncrementResponse>
    
    @POST("suggested-topics/admin/create")
    suspend fun createSuggestedTopic(
        @Header("Authorization") token: String,
        @Body topic: CreateSuggestedTopicRequest
    ): Response<CreateTopicResponse>
    
    @PUT("suggested-topics/admin/{topic_id}")
    suspend fun updateSuggestedTopic(
        @Header("Authorization") token: String,
        @Path("topic_id") topicId: String,
        @Body topic: UpdateSuggestedTopicRequest
    ): Response<UpdateTopicResponse>
    
    // Playback & Analytics endpoints
    @GET("playback/episodes/{episode_id}/stream")
    suspend fun getEpisodeStreamUrl(
        @Path("episode_id") episodeId: String
    ): Response<StreamUrlResponse>
    
    @POST("playback/episodes/{episode_id}/progress")
    suspend fun updatePlaybackProgress(
        @Path("episode_id") episodeId: String,
        @Body progress: PlaybackProgressRequest
    ): Response<PlaybackProgressResponse>
    
    @GET("playback/continue-listening")
    suspend fun getContinueListening(): Response<ContinueListeningResponse>
    
    @POST("playback/episodes/{episode_id}/analytics")
    suspend fun recordListenEvent(
        @Path("episode_id") episodeId: String,
        @Body analytics: ListenAnalyticsRequest
    ): Response<ListenAnalyticsResponse>
}

/**
 * Separate API service for health checks at root level
 */
interface HealthApiService {
    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
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

data class UserClaimsResponse(
    val success: Boolean,
    val claims: Map<String, Any>,
    val user_id: String
)

data class RevokeTokensRequest(
    val revoke_all_sessions: Boolean = false
)

data class RevokeTokensResponse(
    val success: Boolean,
    val message: String,
    val revoked_tokens_count: Int
)

data class LoginInfoRequest(
    val email: String,
    val provider: String? = "wisme-mvpv1"
)

data class LoginInfoResponse(
    val success: Boolean,
    val message: String,
    val login_info: LoginInfo?
)

data class LoginInfo(
    val last_login: String?,
    val login_count: Int,
    val provider: String
)

// User Progress Tracking data classes
data class UserVerificationResponse(
    val success: Boolean,
    val user_exists: Boolean,
    val user_id: String,
    val message: String
)

data class UserProgressResponse(
    val success: Boolean,
    val user_id: String,
    val total_journeys: Int,
    val completed_journeys: Int,
    val in_progress_journeys: Int,
    val total_episodes_completed: Int,
    val total_listening_time_minutes: Int,
    val journey_progress: List<JourneyProgressDetail>
)

data class JourneyProgressDetail(
    val journey_id: String,
    val title: String,
    val completion_percentage: Double,
    val episodes_completed: Int,
    val total_episodes: Int,
    val last_accessed: String
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
    val name: String?,
    val display_name: String?,
    val date_of_birth: String?,
    val gender: String?,
    val profession: String?,
    val avatar_id: Int?
)

data class UserProfileResponse(
    val success: Boolean,
    val message: String,
    val user: UserProfile,
    val user_id: String
)

data class UserProfile(
    val user_id: String,
    val avatar_id: Int,
    val name: String,
    val display_name: String,
    val email: String?,
    val date_of_birth: String,
    val age: Int,
    val gender: String,
    val profession: String,
    val created_at: String,
    val updated_at: String?, 
    val is_profile_complete: Boolean
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

// Topic Request models
data class TopicRequestSubmission(
    val topic_name: String,  // Backend expects topic_name
    val description: String? = null
)

data class TopicRequestResponse(
    val success: Boolean,
    val message: String,
    val request_id: String? = null
)

data class TopicRequestInfo(
    val id: String,
    val topic: String,
    val request_count: Int,
    val created_at: String
)

data class PopularTopicsResponse(
    val success: Boolean,
    val topics: List<TopicRequestInfo>
)

// Feedback System data classes
data class FeedbackSubmissionRequest(
    val feedback_type: String,
    val context_id: String,
    val responses: List<FeedbackResponseItem>
)

data class FeedbackResponseItem(
    val question_id: String,
    val response_value: String
)

data class FeedbackSubmissionResponse(
    val id: String?, // Backend uses 'id' not 'submission_id'  
    val feedback_type: String,
    val context_id: String?,
    val user_id: String,
    val responses: List<FeedbackResponseItem>,
    val submitted_at: String,
    val created_at: String,
    val updated_at: String?
)

data class FeedbackQuestionResponse(
    val question_id: String,
    val question_text: String,
    val response_type: String,
    val options: List<String>?
)

data class FeedbackSubmissionsResponse(
    val submissions: List<FeedbackSubmissionResponse>,
    val total_count: Int,
    val limit: Int,
    val offset: Int
)

data class FeedbackDeleteResponse(
    val success: Boolean,
    val message: String
)

data class FeedbackAnalyticsResponse(
    val success: Boolean,
    val feedback_type: String,
    val context_id: String?,
    val analytics: FeedbackAnalyticsData
)

data class FeedbackAnalyticsData(
    val total_submissions: Int,
    val average_rating: Double?,
    val rating_distribution: Map<String, Int>?
)

data class FeedbackByContextResponse(
    val success: Boolean,
    val feedback_type: String,
    val context_id: String,
    val submissions: List<FeedbackSubmissionResponse>
)

// Podcast Management data classes
data class PodcastsResponse(
    val success: Boolean,
    val podcasts: List<PodcastInfo>,
    val total_count: Int
)

data class PodcastInfo(
    val podcast_id: String,
    val title: String,
    val description: String,
    val category: String,
    val total_episodes: Int,
    val latest_episode_date: String?
)

data class PodcastDetailsResponse(
    val success: Boolean,
    val podcast: PodcastDetail
)

data class PodcastDetail(
    val podcast_id: String,
    val title: String,
    val description: String,
    val category: String,
    val host: String?,
    val total_episodes: Int,
    val latest_episode_date: String?,
    val thumbnail_url: String?
)

data class PodcastEpisodesResponse(
    val success: Boolean,
    val episodes: List<PodcastEpisodeInfo>,
    val total_count: Int
)

data class PodcastEpisodeInfo(
    val episode_id: String,
    val title: String,
    val description: String,
    val duration_minutes: Int,
    val published_date: String,
    val audio_url: String?
)

data class PodcastFeedbackRequest(
    val podcast_id: String,
    val episode_id: String?,
    val rating: Int,
    val comments: String?
)

data class PodcastFeedbackResponse(
    val success: Boolean,
    val message: String,
    val feedback_id: String
)

data class PodcastRecommendationsResponse(
    val success: Boolean,
    val recommendations: List<PodcastInfo>
)

// Suggested Topics data classes (Backend specification)
data class SuggestedTopicsResponse(
    val success: Boolean,
    val topics: List<SuggestedTopicInfo>,
    val total_count: Int,
    val is_dummy_data: Boolean
)

data class SuggestedTopicInfo(
    val name: String,
    val count: Int,
    val last_updated: String
)

data class TopicIncrementResponse(
    val success: Boolean,
    val message: String,
    val topic: SuggestedTopicInfo
)

data class CreateSuggestedTopicRequest(
    val name: String,
    val count: Int = 0
)

data class CreateTopicResponse(
    val success: Boolean,
    val message: String,
    val topic: SuggestedTopicInfo
)

data class UpdateSuggestedTopicRequest(
    val name: String?,
    val count: Int?
)

data class UpdateTopicResponse(
    val success: Boolean,
    val message: String,
    val topic: SuggestedTopicInfo
)

// Playback & Analytics data classes
data class StreamUrlResponse(
    val success: Boolean,
    val stream_url: String,
    val expires_at: String?
)

data class PlaybackProgressRequest(
    val progress_percentage: Double,
    val play_position_seconds: Int,
    val session_id: String?
)

data class PlaybackProgressResponse(
    val success: Boolean,
    val message: String,
    val updated_progress: PlaybackProgress
)

data class PlaybackProgress(
    val episode_id: String,
    val progress_percentage: Double,
    val play_position_seconds: Int,
    val last_updated: String
)

data class ContinueListeningResponse(
    val success: Boolean,
    val episodes: List<ContinueListeningEpisode>
)

data class ContinueListeningEpisode(
    val episode_id: String,
    val journey_id: String,
    val title: String,
    val progress_percentage: Double,
    val play_position_seconds: Int,
    val last_played: String
)

data class ListenAnalyticsRequest(
    val event_type: String, // "start", "pause", "resume", "complete", "skip"
    val timestamp: String,
    val play_position_seconds: Int,
    val session_id: String?
)

data class ListenAnalyticsResponse(
    val success: Boolean,
    val message: String,
    val event_id: String
)
