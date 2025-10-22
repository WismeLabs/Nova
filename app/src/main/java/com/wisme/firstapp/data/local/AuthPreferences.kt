package com.wisme.firstapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.wisme.firstapp.domain.EpisodeProgress

/**
 * Local storage for authentication and user state
 * Handles storing user preferences and authentication state
 */
class AuthPreferences(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    init {
        // Validate preferences and recover if corrupted
        validateAndRecover()
    }
    
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
        return progressJson?.let { json ->
            try {
                // Parse JSON format (new) or fall back to CSV format (legacy)
                if (json.trim().startsWith("{")) {
                    // New JSON format
                    parseProgressFromJson(json)
                } else {
                    // Legacy CSV format - for backward compatibility
                    parseProgressFromCsv(json)
                }
            } catch (e: Exception) {
                println("AuthPreferences: *** ERROR parsing progress: ${e.message} ***")
                null
            }
        }
    }
    
    private fun parseProgressFromJson(json: String): EpisodeProgress? {
        return try {
            // Simple JSON parsing without external library
            val progressPercentage = extractJsonFloat(json, "progressPercentage") ?: return null
            val playPositionSeconds = extractJsonLong(json, "playPositionSeconds") ?: return null
            val status = extractJsonString(json, "status") ?: return null
            val lastUpdated = extractJsonLong(json, "lastUpdated") ?: System.currentTimeMillis()
            
            EpisodeProgress(
                progressPercentage = progressPercentage,
                playPositionSeconds = playPositionSeconds,
                status = status,
                lastUpdated = lastUpdated
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseProgressFromCsv(csv: String): EpisodeProgress? {
        return try {
            val parts = csv.split(",")
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
    
    private fun extractJsonFloat(json: String, key: String): Float? {
        val pattern = """"$key":\s*([0-9.]+)""".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toFloatOrNull()
    }
    
    private fun extractJsonLong(json: String, key: String): Long? {
        val pattern = """"$key":\s*([0-9]+)""".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toLongOrNull()
    }
    
    private fun extractJsonString(json: String, key: String): String? {
        val pattern = """"$key":\s*"([^"]*)"?""".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }
    
    fun setEpisodeProgress(journeyId: String, episodeId: String, progress: EpisodeProgress) {
        val key = "${KEY_EPISODE_PROGRESS_PREFIX}${journeyId}_${episodeId}"
        
        // Use JSON format to prevent data corruption from special characters
        val progressJson = """
            {
                "progressPercentage": ${progress.progressPercentage},
                "playPositionSeconds": ${progress.playPositionSeconds},
                "status": "${progress.status.replace("\"", "\\\"")}",
                "lastUpdated": ${progress.lastUpdated}
            }
        """.trimIndent()
        
        try {
            prefs.edit().putString(key, progressJson).apply()
            
            // Update global flags
            hasStartedAnyEpisode = true
            currentPlayingJourneyId = journeyId
            currentPlayingEpisodeId = episodeId
            lastPlayedTimestamp = System.currentTimeMillis()
        } catch (e: Exception) {
            // Critical error - log and don't crash
            println("AuthPreferences: *** CRITICAL ERROR saving progress: ${e.message} ***")
            // TODO: Implement backup storage mechanism
        }
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
    
    // Episode and Journey completion tracking
    fun markEpisodeCompleted(journeyId: String, episodeId: String) {
        val key = "episode_completed_${journeyId}_${episodeId}"
        prefs.edit().putBoolean(key, true).apply()
        
        // Create backup after marking episode complete
        createBackup()
    }
    
    fun isEpisodeCompleted(journeyId: String, episodeId: String): Boolean {
        val key = "episode_completed_${journeyId}_${episodeId}"
        return prefs.getBoolean(key, false)
    }
    
    fun markJourneyCompleted(journeyId: String) {
        val key = "journey_completed_${journeyId}"
        prefs.edit().putBoolean(key, true).apply()
        
        // Create backup after marking journey complete
        createBackup()
    }
    
    fun isJourneyCompleted(journeyId: String): Boolean {
        val key = "journey_completed_${journeyId}"
        return prefs.getBoolean(key, false)
    }
    
    fun getCompletedEpisodes(): Set<String> {
        val completedEpisodes = mutableSetOf<String>()
        val allPrefs = prefs.all
        
        allPrefs.keys.filter { it.startsWith("episode_completed_") }.forEach { key ->
            if (prefs.getBoolean(key, false)) {
                // Extract journeyId_episodeId from episode_completed_journeyId_episodeId
                val episodeKey = key.removePrefix("episode_completed_")
                completedEpisodes.add(episodeKey)
            }
        }
        return completedEpisodes
    }
    
    fun getCompletedJourneys(): Set<String> {
        val completedJourneys = mutableSetOf<String>()
        val allPrefs = prefs.all
        
        allPrefs.keys.filter { it.startsWith("journey_completed_") }.forEach { key ->
            if (prefs.getBoolean(key, false)) {
                // Extract journeyId from journey_completed_journeyId
                val journeyId = key.removePrefix("journey_completed_")
                completedJourneys.add(journeyId)
            }
        }
        return completedJourneys
    }
    
    // CRITICAL: Feedback submission status persistence
    fun markEpisodeFeedbackSubmitted(episodeId: String) {
        val key = "feedback_submitted_episode_${episodeId}"
        prefs.edit().putBoolean(key, true).apply()
        println("AuthPreferences: *** FEEDBACK SUBMITTED AND PERSISTED: $episodeId ***")
    }
    
    fun isEpisodeFeedbackSubmitted(episodeId: String): Boolean {
        val key = "feedback_submitted_episode_${episodeId}"
        return prefs.getBoolean(key, false)
    }
    
    fun markJourneyFeedbackSubmitted(journeyId: String) {
        val key = "feedback_submitted_journey_${journeyId}"
        prefs.edit().putBoolean(key, true).apply()
        println("AuthPreferences: *** JOURNEY FEEDBACK SUBMITTED AND PERSISTED: $journeyId ***")
    }
    
    fun isJourneyFeedbackSubmitted(journeyId: String): Boolean {
        val key = "feedback_submitted_journey_${journeyId}"
        return prefs.getBoolean(key, false)
    }
    
    fun markGeneralFeedbackSubmitted() {
        prefs.edit().putBoolean("feedback_submitted_general", true).apply()
        println("AuthPreferences: *** GENERAL FEEDBACK SUBMITTED AND PERSISTED ***")
    }
    
    fun isGeneralFeedbackSubmitted(): Boolean {
        return prefs.getBoolean("feedback_submitted_general", false)
    }
    
    fun getSubmittedEpisodeFeedbacks(): Set<String> {
        val submittedFeedbacks = mutableSetOf<String>()
        val allPrefs = prefs.all
        
        allPrefs.keys.filter { it.startsWith("feedback_submitted_episode_") }.forEach { key ->
            if (prefs.getBoolean(key, false)) {
                val episodeId = key.removePrefix("feedback_submitted_episode_")
                submittedFeedbacks.add(episodeId)
            }
        }
        return submittedFeedbacks
    }
    
    fun getSubmittedJourneyFeedbacks(): Set<String> {
        val submittedFeedbacks = mutableSetOf<String>()
        val allPrefs = prefs.all
        
        allPrefs.keys.filter { it.startsWith("feedback_submitted_journey_") }.forEach { key ->
            if (prefs.getBoolean(key, false)) {
                val journeyId = key.removePrefix("feedback_submitted_journey_")
                submittedFeedbacks.add(journeyId)
            }
        }
        return submittedFeedbacks
    }

    /**
     * Add failed progress sync to queue for retry when network returns
     */
    fun addPendingProgressSync(journeyId: String, episodeId: String, progressData: String) {
        val existingQueue = getPendingProgressSyncs().toMutableSet()
        val syncItem = "$journeyId|$episodeId|$progressData|${System.currentTimeMillis()}"
        existingQueue.add(syncItem)
        
        val queueJson = JSONArray(existingQueue.toList()).toString()
        prefs.edit().putString(KEY_PENDING_SYNC_QUEUE, queueJson).apply()
    }
    
    /**
     * Get all pending progress syncs
     */
    fun getPendingProgressSyncs(): List<String> {
        val queueJson = prefs.getString(KEY_PENDING_SYNC_QUEUE, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(queueJson)
            (0 until jsonArray.length()).map { jsonArray.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Remove successfully synced progress from queue
     */
    fun removePendingProgressSync(journeyId: String, episodeId: String) {
        val existingQueue = getPendingProgressSyncs().toMutableSet()
        existingQueue.removeAll { syncItem ->
            syncItem.startsWith("$journeyId|$episodeId|")
        }
        
        val queueJson = if (existingQueue.isEmpty()) {
            null
        } else {
            JSONArray(existingQueue.toList()).toString()
        }
        prefs.edit().putString(KEY_PENDING_SYNC_QUEUE, queueJson).apply()
    }
    
    /**
     * Add failed feedback submission to queue for retry
     */
    fun addPendingFeedbackSubmission(feedbackData: String) {
        val existingQueue = getPendingFeedbackSubmissions().toMutableSet()
        val feedbackItem = "$feedbackData|${System.currentTimeMillis()}"
        existingQueue.add(feedbackItem)
        
        val queueJson = JSONArray(existingQueue.toList()).toString()
        prefs.edit().putString(KEY_PENDING_FEEDBACK_QUEUE, queueJson).apply()
    }
    
    /**
     * Get all pending feedback submissions
     */
    fun getPendingFeedbackSubmissions(): List<String> {
        val queueJson = prefs.getString(KEY_PENDING_FEEDBACK_QUEUE, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(queueJson)
            (0 until jsonArray.length()).map { jsonArray.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Remove successfully submitted feedback from queue
     */
    fun removePendingFeedbackSubmission(feedbackData: String) {
        val existingQueue = getPendingFeedbackSubmissions().toMutableSet()
        existingQueue.removeAll { feedbackItem ->
            feedbackItem.startsWith("$feedbackData|")
        }
        
        val queueJson = if (existingQueue.isEmpty()) {
            null
        } else {
            JSONArray(existingQueue.toList()).toString()
        }
        prefs.edit().putString(KEY_PENDING_FEEDBACK_QUEUE, queueJson).apply()
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
        private const val KEY_PENDING_SYNC_QUEUE = "pending_sync_queue"
        private const val KEY_PENDING_FEEDBACK_QUEUE = "pending_feedback_queue"
        
        // Backup/recovery keys
        private const val KEY_BACKUP_TIMESTAMP = "backup_timestamp"
        private const val BACKUP_PREFS_NAME = "${PREFS_NAME}_backup"
    }
    
    /**
     * Create backup of all SharedPreferences data
     */
    fun createBackup(): Boolean {
        return try {
            val backupPrefs = context.getSharedPreferences(BACKUP_PREFS_NAME, Context.MODE_PRIVATE)
            val allEntries = prefs.all
            
            val editor = backupPrefs.edit()
            
            // Copy all entries to backup
            allEntries.forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        editor.putStringSet(key, value as Set<String>)
                    }
                }
            }
            
            // Add backup timestamp
            editor.putLong(KEY_BACKUP_TIMESTAMP, System.currentTimeMillis())
            editor.apply()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Restore from backup if main preferences are corrupted
     */
    fun restoreFromBackup(): Boolean {
        return try {
            val backupPrefs = context.getSharedPreferences(BACKUP_PREFS_NAME, Context.MODE_PRIVATE)
            val backupTimestamp = backupPrefs.getLong(KEY_BACKUP_TIMESTAMP, 0L)
            
            // Only restore if backup exists and is less than 7 days old
            if (backupTimestamp > 0 && (System.currentTimeMillis() - backupTimestamp) < 7 * 24 * 60 * 60 * 1000L) {
                val allBackupEntries = backupPrefs.all
                val editor = prefs.edit()
                
                // Clear current preferences
                editor.clear()
                
                // Restore from backup (excluding backup timestamp)
                allBackupEntries.forEach { (key, value) ->
                    if (key != KEY_BACKUP_TIMESTAMP) {
                        when (value) {
                            is String -> editor.putString(key, value)
                            is Boolean -> editor.putBoolean(key, value)
                            is Int -> editor.putInt(key, value)
                            is Long -> editor.putLong(key, value)
                            is Float -> editor.putFloat(key, value)
                            is Set<*> -> {
                                @Suppress("UNCHECKED_CAST")
                                editor.putStringSet(key, value as Set<String>)
                            }
                        }
                    }
                }
                
                editor.apply()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if preferences are corrupted and attempt recovery
     */
    fun validateAndRecover(): Boolean {
        return try {
            // Test basic operations to detect corruption
            val testValue = prefs.getBoolean("test_corruption_check", false)
            prefs.edit().putBoolean("test_corruption_check", !testValue).apply()
            val verifyValue = prefs.getBoolean("test_corruption_check", false)
            
            if (verifyValue == !testValue) {
                // Preferences working normally
                true
            } else {
                // Corruption detected, attempt restore
                restoreFromBackup()
            }
        } catch (e: Exception) {
            // Exception indicates corruption, attempt restore
            restoreFromBackup()
        }
    }
}

enum class NavigationState {
    FIRST_LAUNCH,
    ONBOARDING,
    LOGIN,
    PROFILE_SETUP,
    HOME
}
