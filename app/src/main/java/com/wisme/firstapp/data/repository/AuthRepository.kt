package com.wisme.firstapp.data.repository

import com.wisme.firstapp.data.api.*
import com.wisme.firstapp.data.local.AuthPreferences
import com.wisme.firstapp.util.Logger
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling authentication and user profile operations
 * Manages both local storage and backend API synchronization
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: AuraApiService,
    private val authPrefs: AuthPreferences
) {
    
    /**
     * Verify Firebase token with backend
     */
    suspend fun verifyToken(firebaseToken: String): Result<VerifyTokenResponse> {
        Logger.logAuth("Verify Token", userId = null)
        Logger.logRepository("AuthRepository", "verifyToken - Starting token verification")
        
        return try {
            val response = apiService.verifyToken(VerifyTokenRequest(firebaseToken))
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Logger.logAuth(
                    event = "Token Verification",
                    userId = responseBody.user?.uid,
                    success = true
                )
                Logger.logRepository(
                    repository = "AuthRepository",
                    operation = "verifyToken",
                    success = true,
                    additionalData = mapOf("userId" to (responseBody.user?.uid ?: "unknown"))
                )
                Result.success(responseBody)
            } else {
                val errorMessage = "Token verification failed: ${response.code()} ${response.message()}"
                Logger.logAuth(
                    event = "Token Verification",
                    success = false,
                    errorMessage = errorMessage
                )
                Logger.logRepository(
                    repository = "AuthRepository",
                    operation = "verifyToken",
                    success = false,
                    errorMessage = errorMessage
                )
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.logAuth(
                event = "Token Verification",
                success = false,
                errorMessage = "Exception: ${e.message}"
            )
            Logger.logRepository(
                repository = "AuthRepository",
                operation = "verifyToken",
                success = false,
                errorMessage = "Exception: ${e.message}",
                additionalData = mapOf("exception" to e.javaClass.simpleName)
            )
            Result.failure(e)
        }
    }
    
    /**
     * Create user profile on backend
     */
    suspend fun createUserProfile(
        firebaseToken: String,
        avatarId: Int,
        name: String,
        displayName: String,
        dateOfBirth: String,
        gender: String,
        profession: String
    ): Result<CreateUserProfileResponse> {
        Logger.logAuth("Create Profile", userId = null)
        Logger.logRepository(
            repository = "AuthRepository",
            operation = "createUserProfile",
            additionalData = mapOf(
                "name" to name,
                "displayName" to displayName,
                "avatarId" to avatarId,
                "gender" to gender,
                "profession" to profession
            )
        )
        
        return try {
            val request = CreateUserProfileRequest(
                avatar_id = avatarId,
                name = name,
                display_name = displayName,
                date_of_birth = dateOfBirth,
                gender = gender,
                profession = profession
            )
            
            println("AuthRepository: Calling createUserProfile with token: Bearer ${firebaseToken.take(50)}...")
            println("AuthRepository: Request data: $request")
            val response = apiService.createUserProfile("Bearer $firebaseToken", request)
            println("AuthRepository: Response code: ${response.code()}")
            println("AuthRepository: Response message: ${response.message()}")
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Logger.logAuth(
                    event = "Profile Creation",
                    userId = responseBody.user_id,
                    success = true
                )
                Logger.logRepository(
                    repository = "AuthRepository",
                    operation = "createUserProfile",
                    success = true,
                    additionalData = mapOf("userId" to responseBody.user_id)
                )
                Result.success(responseBody)
            } else {
                val errorMessage = "Profile creation failed: ${response.code()} ${response.message()}"
                Logger.logAuth(
                    event = "Profile Creation",
                    success = false,
                    errorMessage = errorMessage
                )
                Logger.logRepository(
                    repository = "AuthRepository",
                    operation = "createUserProfile",
                    success = false,
                    errorMessage = errorMessage
                )
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.logAuth(
                event = "Profile Creation",
                success = false,
                errorMessage = "Exception: ${e.message}"
            )
            Logger.logRepository(
                repository = "AuthRepository",
                operation = "createUserProfile",
                success = false,
                errorMessage = "Exception: ${e.message}",
                additionalData = mapOf("exception" to e.javaClass.simpleName)
            )
            Result.failure(e)
        }
    }
    
    /**
     * Get user profile from backend
     */
    suspend fun getMyProfile(firebaseToken: String): Result<UserProfile> {
        return try {
            val response = apiService.getMyProfile("Bearer $firebaseToken")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user profile on backend
     */
    suspend fun updateMyProfile(
        firebaseToken: String,
        name: String?,
        displayName: String?,
        dateOfBirth: String?,
        gender: String?,
        profession: String?,
        avatarId: Int?
    ): Result<UserProfile> {
        return try {
            val request = UpdateUserProfileRequest(
                name = name,
                display_name = displayName,
                date_of_birth = dateOfBirth,
                gender = gender,
                profession = profession,
                avatar_id = avatarId
            )
            
            val response = apiService.updateMyProfile("Bearer $firebaseToken", request)
            if (response.isSuccessful && response.body() != null) {
                val userProfileResponse = response.body()!!
                Result.success(userProfileResponse.user)
            } else {
                Result.failure(Exception("Profile update failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check authentication status with backend
     */
    suspend fun getAuthStatus(firebaseToken: String): Result<AuthStatusResponse> {
        return try {
            val response = apiService.getAuthStatus("Bearer $firebaseToken")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Auth status check failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sync local profile data with backend after successful authentication
     */
    suspend fun syncProfileWithBackend(firebaseToken: String): Result<Boolean> {
        return try {
            // First check if profile exists on backend
            val profileResult = getMyProfile(firebaseToken)
            
            if (profileResult.isSuccess) {
                // Profile exists on backend - just mark as completed, DON'T overwrite local data
                // This prevents overwriting fresh edits with stale backend data
                println("AuthRepository: Profile exists on backend, marking as completed locally")
                authPrefs.hasCompletedProfile = true
                Result.success(true)
            } else {
                // Profile doesn't exist on backend, check if we have local data to sync
                if (authPrefs.hasCompletedProfile) {
                    val createResult = createUserProfile(
                        firebaseToken = firebaseToken,
                        avatarId = authPrefs.userAvatarId,
                        name = authPrefs.userName ?: "",
                        displayName = authPrefs.userDisplayName ?: "",
                        dateOfBirth = authPrefs.userDateOfBirth ?: "",
                        gender = authPrefs.userGender ?: "",
                        profession = authPrefs.userProfession ?: ""
                    )
                    Result.success(createResult.isSuccess)
                } else {
                    // No profile data to sync
                    Result.success(false)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send login information to backend with provider ID
     */
    suspend fun sendLoginInfo(email: String): Result<LoginInfoResponse> {
        Logger.logRepository("AuthRepository", "sendLoginInfo - Sending login info with provider ID")
        
        return try {
            val request = LoginInfoRequest(
                email = email,
                provider = "wisme-mvpv1"  // Custom provider ID from backend team
            )
            
            val response = apiService.loginInfo(request)
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Logger.logRepository(
                    repository = "AuthRepository",
                    operation = "sendLoginInfo",
                    success = true,
                    additionalData = mapOf("provider" to "wisme-mvpv1")
                )
                Result.success(responseBody)
            } else {
                val errorMessage = "Login info failed: ${response.code()} ${response.message()}"
                Logger.logRepository(
                    repository = "AuthRepository",
                    operation = "sendLoginInfo",
                    success = false,
                    errorMessage = errorMessage
                )
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.logRepository(
                repository = "AuthRepository",
                operation = "sendLoginInfo",
                success = false,
                errorMessage = e.message ?: "Unknown error"
            )
            Result.failure(e)
        }
    }
}
