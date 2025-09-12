package com.wisme.nova.domain.usecase

import com.wisme.nova.domain.model.User
import com.wisme.nova.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case for user authentication
 * 
 * This represents business logic and orchestrates data operations.
 * Use cases should be small, focused, and testable.
 * 
 * @param authRepository Repository that handles auth data operations
 * 
 * @author Wisme Labs
 * @since 1.0.0
 */
class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    /**
     * Login user with email and password
     * 
     * @param email User's email
     * @param password User's password
     * @return Result with user data or error
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Validate input
            if (email.isBlank() || password.isBlank()) {
                Result.failure(IllegalArgumentException("Email and password cannot be empty"))
            } else {
                authRepository.login(email, password)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Register new user
     * 
     * @param email User's email
     * @param password User's password
     * @param displayName User's display name
     * @return Result with user data or error
     */
    suspend fun register(email: String, password: String, displayName: String): Result<User> {
        return try {
            // Validate input
            when {
                email.isBlank() -> Result.failure(IllegalArgumentException("Email cannot be empty"))
                password.length < 6 -> Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
                displayName.isBlank() -> Result.failure(IllegalArgumentException("Display name cannot be empty"))
                else -> authRepository.register(email, password, displayName)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current user
     * 
     * @return Flow of current user state
     */
    fun getCurrentUser(): Flow<User?> {
        return authRepository.getCurrentUser()
    }
    
    /**
     * Logout current user
     */
    suspend fun logout(): Result<Unit> {
        return try {
            authRepository.logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Example Use Case for podcast operations
 * 
 * TODO: Implement when building podcast features
 */
class PodcastUseCase @Inject constructor(
    // private val podcastRepository: PodcastRepository
) {
    // TODO: Add podcast-related business logic here
    // Example methods:
    // - searchPodcasts(query: String)
    // - subscribeToPodcast(podcastId: String)
    // - getSubscribedPodcasts()
    // - unsubscribeFromPodcast(podcastId: String)
}