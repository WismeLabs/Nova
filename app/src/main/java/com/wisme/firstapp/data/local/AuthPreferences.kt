package com.wisme.firstapp.data.local

import android.content.Context
import android.content.SharedPreferences

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
    }
}

enum class NavigationState {
    FIRST_LAUNCH,
    ONBOARDING,
    LOGIN,
    PROFILE_SETUP,
    HOME
}
