package com.wisme.firstapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.wisme.firstapp.domain.EpisodeProgress

/**
 * Local storage for authentication and user state
 * Handles storing user preferences and authentication state
 */
class AuthPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    // Authentication state
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()
    
    var firebaseToken: String?
        get() = prefs.getString(KEY_FIREBASE_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_FIREBASE_TOKEN, value).apply()
    
    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()
    
    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()
    
    // Onboarding state
    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()
    
    // Profile state
    var hasCompletedProfile: Boolean
        get() = prefs.getBoolean(KEY_PROFILE_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_PROFILE_COMPLETED, value).apply()
    
    // User profile data
    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()
    
    var userDisplayName: String?
        get() = prefs.getString(KEY_USER_DISPLAY_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_DISPLAY_NAME, value).apply()
    
    var userAvatarId: Int
        get() = prefs.getInt(KEY_USER_AVATAR_ID, 1)
        set(value) = prefs.edit().putInt(KEY_USER_AVATAR_ID, value).apply()
    
    var userGender: String?
        get() = prefs.getString(KEY_USER_GENDER, null)
        set(value) = prefs.edit().putString(KEY_USER_GENDER, value).apply()
    
    var userProfession: String?
        get() = prefs.getString(KEY_USER_PROFESSION, null)
        set(value) = prefs.edit().putString(KEY_USER_PROFESSION, value).apply()
    
    var userDateOfBirth: String?
        get() = prefs.getString(KEY_USER_DOB, null)
        set(value) = prefs.edit().putString(KEY_USER_DOB, value).apply()
    
    // Progress tracking
    var currentPlayingJourneyId: String?
        get() = prefs.getString(KEY_CURRENT_JOURNEY_ID, null)
        set(value) = prefs.edit().putString(KEY_CURRENT_JOURNEY_ID, value).apply()
    
    var currentPlayingEpisodeId: String?
        get() = prefs.getString(KEY_CURRENT_EPISODE_ID, null)
        set(value) = prefs.edit().putString(KEY_CURRENT_EPISODE_ID, value).apply()
    
    var hasStartedAnyEpisode: Boolean
        get() = prefs.getBoolean(KEY_HAS_STARTED_EPISODE, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_STARTED_EPISODE, value).apply()
    
    var lastPlayedTimestamp: Long
        get() = prefs.getLong(KEY_LAST_PLAYED_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_PLAYED_TIMESTAMP, value).apply()
    
    // Store episode progress as JSON string for multiple episodes
    fun getEpisodeProgress(journeyId: String, episodeId: String): EpisodeProgress? {
        val key = "${KEY_EPISODE_PROGRESS_PREFIX}${journeyId}_${episodeId}"
        val progressJson = prefs.getString(key, null)
        return progressJson?.let {
            try {
                // Parse JSON string to EpisodeProgress object
                // For now, we'll use a simple format: "progressPercentage,playPositionSeconds,status"
                val parts = it.split(",")
                if (parts.size >= 3) {
                    EpisodeProgress(
                        progressPercentage = parts[0].toFloat(),
                        playPositionSeconds = parts[1].toLong(),
                        status = parts[2],
                        lastUpdated = if (parts.size > 3) parts[3].toLong() else System.currentTimeMillis()
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun setEpisodeProgress(journeyId: String, episodeId: String, progress: EpisodeProgress) {
        val key = "${KEY_EPISODE_PROGRESS_PREFIX}${journeyId}_${episodeId}"
        val progressJson = "${progress.progressPercentage},${progress.playPositionSeconds},${progress.status},${progress.lastUpdated}"
        prefs.edit().putString(key, progressJson).apply()
        
        // Update global flags
        hasStartedAnyEpisode = true
        currentPlayingJourneyId = journeyId
        currentPlayingEpisodeId = episodeId
        lastPlayedTimestamp = System.currentTimeMillis()
    }
    
    fun getAllEpisodeProgress(): Map<String, EpisodeProgress> {
        val progressMap = mutableMapOf<String, EpisodeProgress>()
        val allPrefs = prefs.all
        
        allPrefs.keys.filter { it.startsWith(KEY_EPISODE_PROGRESS_PREFIX) }.forEach { key ->
            val episodeKey = key.removePrefix(KEY_EPISODE_PROGRESS_PREFIX)
            val progressJson = allPrefs[key] as? String
            progressJson?.let {
                try {
                    val parts = it.split(",")
                    if (parts.size >= 3) {
                        progressMap[episodeKey] = EpisodeProgress(
                            progressPercentage = parts[0].toFloat(),
                            playPositionSeconds = parts[1].toLong(),
                            status = parts[2],
                            lastUpdated = if (parts.size > 3) parts[3].toLong() else System.currentTimeMillis()
                        )
                    }
                } catch (e: Exception) {
                    // Ignore corrupted progress data
                }
            }
        }
        return progressMap
    }
    
    // App state
    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
    
    /**
     * Clear all user data (for logout)
     */
    fun clearUserData() {
        prefs.edit().apply {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_FIREBASE_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_PROFILE_COMPLETED)
            remove(KEY_USER_NAME)
            remove(KEY_USER_DISPLAY_NAME)
            remove(KEY_USER_AVATAR_ID)
            remove(KEY_USER_GENDER)
            remove(KEY_USER_PROFESSION)
            remove(KEY_USER_DOB)
            apply()
        }
    }
    
    /**
     * Get current user navigation state
     */
    fun getUserNavigationState(): NavigationState {
        return when {
            isFirstLaunch -> NavigationState.FIRST_LAUNCH
            !hasCompletedOnboarding -> NavigationState.ONBOARDING
            !isLoggedIn -> NavigationState.LOGIN
            !hasCompletedProfile -> NavigationState.PROFILE_SETUP
            else -> NavigationState.HOME
        }
    }
    
    companion object {
        private const val PREFS_NAME = "nova_auth_prefs"
        
        // Authentication keys
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_FIREBASE_TOKEN = "firebase_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        
        // Onboarding keys
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        
        // Profile keys
        private const val KEY_PROFILE_COMPLETED = "profile_completed"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_DISPLAY_NAME = "user_display_name"
        private const val KEY_USER_AVATAR_ID = "user_avatar_id"
        private const val KEY_USER_GENDER = "user_gender"
        private const val KEY_USER_PROFESSION = "user_profession"
        private const val KEY_USER_DOB = "user_dob"
        
        // App state keys
        private const val KEY_FIRST_LAUNCH = "first_launch"
        
        // Progress tracking keys
        private const val KEY_CURRENT_JOURNEY_ID = "current_journey_id"
        private const val KEY_CURRENT_EPISODE_ID = "current_episode_id"
        private const val KEY_HAS_STARTED_EPISODE = "has_started_episode"
        private const val KEY_LAST_PLAYED_TIMESTAMP = "last_played_timestamp"
        private const val KEY_EPISODE_PROGRESS_PREFIX = "episode_progress_"
    }
}

enum class NavigationState {
    FIRST_LAUNCH,
    ONBOARDING,
    LOGIN,
    PROFILE_SETUP,
    HOME
}
