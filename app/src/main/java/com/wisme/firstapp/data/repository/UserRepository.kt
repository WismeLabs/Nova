package com.wisme.firstapp.data.repository

import android.util.Log
import com.wisme.firstapp.data.local.AuthPreferences
import com.wisme.firstapp.data.api.AuraApiService
import com.wisme.firstapp.data.api.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: AuraApiService,
    private val authPreferences: AuthPreferences
) {
    
    companion object {
        private const val TAG = "UserRepository"
    }
    
    /**
     * Get current user's profile from backend
     */
    suspend fun getMyProfile(): Result<UserProfile> {
        return try {
            val firebaseToken = authPreferences.firebaseToken
            if (firebaseToken.isNullOrBlank()) {
                return Result.failure(Exception("No Firebase token available"))
            }
            
            val response = withContext(Dispatchers.IO) {
                apiService.getMyProfile("Bearer $firebaseToken")
            }
            
            if (response.isSuccessful && response.body() != null) {
                val userProfile = response.body()!!
                Log.d(TAG, "Successfully fetched user profile: ${userProfile.display_name}")
                
                // Cache user info in preferences
                authPreferences.userName = userProfile.name
                authPreferences.userDisplayName = userProfile.display_name
                authPreferences.userEmail = userProfile.email
                authPreferences.userAvatarId = userProfile.avatar_id
                
                Result.success(userProfile)
            } else {
                val errorMsg = "Failed to fetch profile: ${response.code()} - ${response.errorBody()?.string()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get user profile from cache (preferences)
     */
    fun getCachedProfile(): UserProfile? {
        val userId = authPreferences.userId
        val userName = authPreferences.userName
        val displayName = authPreferences.userDisplayName
        val email = authPreferences.userEmail
        val avatarId = authPreferences.userAvatarId
        
        return if (userId != null && userName != null && displayName != null) {
            UserProfile(
                user_id = userId,
                avatar_id = avatarId ?: 1,
                name = userName,
                display_name = displayName,
                email = email,
                date_of_birth = "", // Not cached
                age = 0, // Not cached  
                gender = "", // Not cached
                profession = "", // Not cached
                created_at = "",
                updated_at = null,
                is_profile_complete = true
            )
        } else {
            null
        }
    }
    
    /**
     * Get display name for UI
     */
    fun getDisplayName(): String {
        return authPreferences.userDisplayName ?: "User"
    }
    
    /**
     * Get user name for UI
     */
    fun getUserName(): String {
        return authPreferences.userName ?: "User"
    }
    
    /**
     * Get avatar ID for UI
     */
    fun getAvatarId(): Int {
        return authPreferences.userAvatarId ?: 1
    }
}
