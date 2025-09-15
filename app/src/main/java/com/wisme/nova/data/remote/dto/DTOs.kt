package com.wisme.nova.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Objects (DTOs) for API communication
 * 
 * These represent the JSON structure from your backend API.
 * They should match your API documentation exactly.
 * 
 * DTOs are converted to domain models using mappers.
 * 
 * @author Wisme Labs
 * @since 1.0.0
 */

/**
 * Login request DTO
 * 
 * Example API call:
 * POST /auth/login
 * {
 *   "email": "user@example.com",
 *   "password": "password123"
 * }
 */
data class LoginRequestDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

/**
 * Registration request DTO
 * 
 * Example API call:
 * POST /auth/register
 * {
 *   "email": "user@example.com",
 *   "password": "password123",
 *   "display_name": "John Doe"
 * }
 */
data class RegisterRequestDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("display_name")
    val displayName: String
)

/**
 * User response DTO
 * 
 * Example API response:
 * {
 *   "id": "user_123",
 *   "email": "user@example.com",
 *   "display_name": "John Doe",
 *   "avatar_url": "https://example.com/avatar.jpg",
 *   "email_verified": true,
 *   "created_at": 1694567890000
 * }
 */
data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    @SerializedName("email_verified")
    val emailVerified: Boolean = false,
    @SerializedName("created_at")
    val createdAt: Long
)

/**
 * Authentication response DTO
 * 
 * Example API response:
 * {
 *   "token": "jwt_token_here",
 *   "user": { ... user object ... }
 * }
 */
data class AuthResponseDto(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: UserDto
)

/**
 * Podcast DTO
 * 
 * TODO: Update this based on your actual API response
 * Example structure:
 * {
 *   "id": "podcast_123",
 *   "title": "Awesome Podcast",
 *   "description": "A great podcast about...",
 *   "author": "Podcast Author",
 *   "image_url": "https://example.com/image.jpg",
 *   "categories": ["Technology", "Business"],
 *   "episode_count": 150,
 *   "last_updated": 1694567890000
 * }
 */
data class PodcastDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("categories")
    val categories: List<String> = emptyList(),
    @SerializedName("episode_count")
    val episodeCount: Int = 0,
    @SerializedName("last_updated")
    val lastUpdated: Long
)

/**
 * Episode DTO
 * 
 * TODO: Update this based on your actual API response
 */
data class EpisodeDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("podcast_id")
    val podcastId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("audio_url")
    val audioUrl: String,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("duration")
    val duration: Long,
    @SerializedName("published_at")
    val publishedAt: Long
)