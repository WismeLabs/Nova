package com.wisme.nova.domain.model

/**
 * User data model representing authenticated user
 * 
 * This is a domain model - it represents business entities
 * and should be independent of data sources.
 * 
 * @param id Unique user identifier
 * @param email User's email address
 * @param displayName User's display name
 * @param avatarUrl URL to user's profile picture
 * @param isEmailVerified Whether email is verified
 * @param createdAt User creation timestamp
 * 
 * @author Wisme Labs
 * @since 1.0.0
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Podcast data model
 * 
 * Represents a podcast show with its metadata
 */
data class Podcast(
    val id: String,
    val title: String,
    val description: String,
    val author: String,
    val imageUrl: String,
    val categories: List<String> = emptyList(),
    val episodeCount: Int = 0,
    val isSubscribed: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Episode data model
 * 
 * Represents a single podcast episode
 */
data class Episode(
    val id: String,
    val podcastId: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val imageUrl: String? = null,
    val duration: Long, // Duration in milliseconds
    val publishedAt: Long,
    val isDownloaded: Boolean = false,
    val isPlayed: Boolean = false,
    val playbackPosition: Long = 0L // Current playback position in milliseconds
)