package com.wisme.nova.utils

/**
 * Application constants
 * 
 * Store app-wide constants here for easy maintenance.
 * 
 * @author Wisme Labs
 * @since 1.0.0
 */
object Constants {
    
    // Networking
    const val NETWORK_TIMEOUT = 30L
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    
    // Database
    const val DATABASE_NAME = "nova_database"
    const val DATABASE_VERSION = 1
    
    // Shared Preferences
    const val PREFS_NAME = "nova_prefs"
    const val PREFS_AUTH_TOKEN = "auth_token"
    const val PREFS_USER_ID = "user_id"
    const val PREFS_THEME_MODE = "theme_mode"
    
    // Media Player
    const val MEDIA_NOTIFICATION_ID = 1001
    const val MEDIA_CHANNEL_ID = "media_playback_channel"
    
    // Download
    const val DOWNLOAD_NOTIFICATION_ID = 2001
    const val DOWNLOAD_CHANNEL_ID = "download_channel"
    
    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_EMAIL_LENGTH = 255
    const val MAX_DISPLAY_NAME_LENGTH = 50
    
    // UI
    const val SPLASH_SCREEN_DURATION = 2000L
    const val ANIMATION_DURATION_SHORT = 200L
    const val ANIMATION_DURATION_MEDIUM = 400L
    const val ANIMATION_DURATION_LONG = 600L
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val PREFETCH_DISTANCE = 5
    
    // Cache
    const val CACHE_MAX_AGE = 60 * 60 * 24 // 24 hours in seconds
    const val CACHE_MAX_STALE = 60 * 60 * 24 * 7 // 7 days in seconds
}

/**
 * API endpoints
 * 
 * Define your API endpoints here for consistency
 */
object ApiEndpoints {
    // Auth endpoints
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
    const val LOGOUT = "auth/logout"
    const val CURRENT_USER = "auth/me"
    
    // Podcast endpoints
    const val PODCASTS_SEARCH = "podcasts/search"
    const val PODCASTS_BY_ID = "podcasts/{id}"
    const val PODCASTS_SUBSCRIPTIONS = "podcasts/subscriptions"
    const val PODCASTS_SUBSCRIBE = "podcasts/{id}/subscribe"
    
    // Episode endpoints
    const val EPISODES_BY_PODCAST = "podcasts/{podcastId}/episodes"
    const val EPISODES_BY_ID = "episodes/{id}"
    const val EPISODES_MARK_PLAYED = "episodes/{id}/played"
    const val EPISODES_UPDATE_POSITION = "episodes/{id}/position"
}

/**
 * Error messages
 * 
 * Centralize error messages for consistency and easy localization
 */
object ErrorMessages {
    const val NETWORK_ERROR = "Network error. Please check your connection."
    const val SERVER_ERROR = "Server error. Please try again later."
    const val LOGIN_FAILED = "Login failed. Please check your credentials."
    const val REGISTRATION_FAILED = "Registration failed. Please try again."
    const val INVALID_EMAIL = "Please enter a valid email address."
    const val INVALID_PASSWORD = "Password must be at least 6 characters long."
    const val INVALID_DISPLAY_NAME = "Display name cannot be empty."
    const val PLAYBACK_ERROR = "Playback error occurred."
    const val DOWNLOAD_ERROR = "Download failed. Please try again."
    const val UNKNOWN_ERROR = "An unexpected error occurred."
}