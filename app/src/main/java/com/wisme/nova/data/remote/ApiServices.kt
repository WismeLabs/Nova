package com.wisme.nova.data.remote

import com.wisme.nova.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interfaces
 * 
 * These define your HTTP endpoints using Retrofit annotations.
 * Update the base URL and endpoints to match your backend API.
 * 
 * API Documentation: [Add link to your API docs here]
 * Base URL: Configure in local.properties as API_BASE_URL
 * 
 * @author Wisme Labs
 * @since 1.0.0
 */

/**
 * Authentication API endpoints
 * 
 * Base path: /auth/
 */
interface AuthApiService {
    
    /**
     * Login user
     * 
     * POST /auth/login
     * Backend API: POST /auth/login
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>
    
    /**
     * Register new user
     * 
     * POST /auth/register
     * Backend API: POST /auth/register
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>
    
    /**
     * Get current user profile
     * 
     * GET /auth/me
     * Backend API: GET /auth/me
     * Requires: Authorization header with JWT token
     */
    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<UserDto>
    
    /**
     * Logout (optional - for server-side token invalidation)
     * 
     * POST /auth/logout
     * Backend API: POST /auth/logout
     */
    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>
}

/**
 * Podcast API endpoints
 * 
 * Base path: /podcasts/
 * TODO: Implement when building podcast features
 */
interface PodcastApiService {
    
    /**
     * Search podcasts
     * 
     * GET /podcasts/search?q={query}
     * Backend API: GET /podcasts/search
     */
    @GET("podcasts/search")
    suspend fun searchPodcasts(@Query("q") query: String): Response<List<PodcastDto>>
    
    /**
     * Get podcast by ID
     * 
     * GET /podcasts/{id}
     * Backend API: GET /podcasts/{id}
     */
    @GET("podcasts/{id}")
    suspend fun getPodcastById(@Path("id") podcastId: String): Response<PodcastDto>
    
    /**
     * Get user's subscribed podcasts
     * 
     * GET /podcasts/subscriptions
     * Backend API: GET /podcasts/subscriptions
     * Requires: Authorization header
     */
    @GET("podcasts/subscriptions")
    suspend fun getSubscribedPodcasts(@Header("Authorization") token: String): Response<List<PodcastDto>>
    
    /**
     * Subscribe to podcast
     * 
     * POST /podcasts/{id}/subscribe
     * Backend API: POST /podcasts/{id}/subscribe
     */
    @POST("podcasts/{id}/subscribe")
    suspend fun subscribeToPodcast(
        @Path("id") podcastId: String,
        @Header("Authorization") token: String
    ): Response<Unit>
    
    /**
     * Unsubscribe from podcast
     * 
     * DELETE /podcasts/{id}/subscribe
     * Backend API: DELETE /podcasts/{id}/subscribe
     */
    @DELETE("podcasts/{id}/subscribe")
    suspend fun unsubscribeFromPodcast(
        @Path("id") podcastId: String,
        @Header("Authorization") token: String
    ): Response<Unit>
}

/**
 * Episode API endpoints
 * 
 * Base path: /episodes/
 * TODO: Implement when building episode features
 */
interface EpisodeApiService {
    
    /**
     * Get episodes for a podcast
     * 
     * GET /podcasts/{podcastId}/episodes
     * Backend API: GET /podcasts/{podcastId}/episodes
     */
    @GET("podcasts/{podcastId}/episodes")
    suspend fun getEpisodesForPodcast(@Path("podcastId") podcastId: String): Response<List<EpisodeDto>>
    
    /**
     * Get episode by ID
     * 
     * GET /episodes/{id}
     * Backend API: GET /episodes/{id}
     */
    @GET("episodes/{id}")
    suspend fun getEpisodeById(@Path("id") episodeId: String): Response<EpisodeDto>
    
    /**
     * Mark episode as played
     * 
     * POST /episodes/{id}/played
     * Backend API: POST /episodes/{id}/played
     */
    @POST("episodes/{id}/played")
    suspend fun markEpisodeAsPlayed(
        @Path("id") episodeId: String,
        @Header("Authorization") token: String
    ): Response<Unit>
    
    /**
     * Update playback position
     * 
     * PUT /episodes/{id}/position
     * Backend API: PUT /episodes/{id}/position
     */
    @PUT("episodes/{id}/position")
    suspend fun updatePlaybackPosition(
        @Path("id") episodeId: String,
        @Body position: Map<String, Long>, // {"position": 12345}
        @Header("Authorization") token: String
    ): Response<Unit>
}