package com.wisme.nova.utils

import com.wisme.nova.domain.model.*
import com.wisme.nova.data.remote.dto.*

/**
 * Mapper functions to convert between DTOs and Domain models
 * 
 * These functions convert data from external sources (API, Database)
 * to internal domain models that the app uses.
 * 
 * Keep these functions simple and focused on data transformation.
 * 
 * @author Wisme Labs
 * @since 1.0.0
 */

/**
 * Convert UserDto from API to User domain model
 */
fun UserDto.toDomainModel(): User {
    return User(
        id = this.id,
        email = this.email,
        displayName = this.displayName,
        avatarUrl = this.avatarUrl,
        isEmailVerified = this.emailVerified,
        createdAt = this.createdAt
    )
}

/**
 * Convert PodcastDto from API to Podcast domain model
 */
fun PodcastDto.toDomainModel(): Podcast {
    return Podcast(
        id = this.id,
        title = this.title,
        description = this.description,
        author = this.author,
        imageUrl = this.imageUrl,
        categories = this.categories,
        episodeCount = this.episodeCount,
        isSubscribed = false, // This will be determined by local data
        lastUpdated = this.lastUpdated
    )
}

/**
 * Convert EpisodeDto from API to Episode domain model
 */
fun EpisodeDto.toDomainModel(): Episode {
    return Episode(
        id = this.id,
        podcastId = this.podcastId,
        title = this.title,
        description = this.description,
        audioUrl = this.audioUrl,
        imageUrl = this.imageUrl,
        duration = this.duration,
        publishedAt = this.publishedAt,
        isDownloaded = false, // This will be determined by local data
        isPlayed = false, // This will be determined by local data
        playbackPosition = 0L // This will be determined by local data
    )
}

/**
 * Convert list of DTOs to domain models
 */
fun List<PodcastDto>.toDomainModels(): List<Podcast> {
    return this.map { it.toDomainModel() }
}

fun List<EpisodeDto>.toDomainModels(): List<Episode> {
    return this.map { it.toDomainModel() }
}