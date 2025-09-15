package com.wisme.nova.data.repository

import com.wisme.nova.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 * 
 * This defines the contract for auth data operations.
 * Implementation will be in AuthRepositoryImpl.
 * 
 * Repository pattern abstracts data sources (API, Database, etc.)
 * and provides a clean API for the domain layer.
 * 
 * @author Wisme Labs
 * @since 1.0.0
 */
interface AuthRepository {
    
    /**
     * Login user with email and password
     * 
     * @param email User's email
     * @param password User's password
     * @return Result with user data or error
     */
    suspend fun login(email: String, password: String): Result<User>
    
    /**
     * Register new user
     * 
     * @param email User's email
     * @param password User's password
     * @param displayName User's display name
     * @return Result with user data or error
     */
    suspend fun register(email: String, password: String, displayName: String): Result<User>
    
    /**
     * Get current authenticated user
     * 
     * @return Flow of current user state (null if not authenticated)
     */
    fun getCurrentUser(): Flow<User?>
    
    /**
     * Logout current user
     */
    suspend fun logout()
}

/**
 * Repository interface for podcast operations
 * 
 * TODO: Implement when building podcast features
 */
interface PodcastRepository {
    // TODO: Add podcast repository methods
    // Example methods:
    // - searchPodcasts(query: String): Flow<List<Podcast>>
    // - getPodcastById(id: String): Flow<Podcast?>
    // - getSubscribedPodcasts(): Flow<List<Podcast>>
    // - subscribeToPodcast(podcastId: String)
    // - unsubscribeFromPodcast(podcastId: String)
}

/**
 * Repository interface for episode operations
 * 
 * TODO: Implement when building episode features
 */
interface EpisodeRepository {
    // TODO: Add episode repository methods
    // Example methods:
    // - getEpisodesForPodcast(podcastId: String): Flow<List<Episode>>
    // - getEpisodeById(id: String): Flow<Episode?>
    // - downloadEpisode(episodeId: String)
    // - markAsPlayed(episodeId: String)
    // - updatePlaybackPosition(episodeId: String, position: Long)
}