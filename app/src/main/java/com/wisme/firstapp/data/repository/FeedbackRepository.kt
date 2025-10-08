package com.wisme.firstapp.data.repository

import android.util.Log
import com.wisme.firstapp.data.local.AuthPreferences
import com.wisme.firstapp.data.api.AuraApiService
import com.wisme.firstapp.data.api.FeedbackSubmissionRequest
import com.wisme.firstapp.data.api.FeedbackResponseItem
import com.wisme.firstapp.ui.feedback.EpisodeFeedbackData
import com.wisme.firstapp.ui.feedback.JourneyFeedbackData
import com.wisme.firstapp.ui.feedback.GeneralFeedbackData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.wisme.firstapp.data.local.entities.*
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class FeedbackQuestion(
    val question_id: String,
    val question_text: String,
    val response_type: String, // "scale", "text", "multiple_choice"
    val options: List<String>? = null
)

@Serializable
data class FeedbackResponse(
    val question_id: String,
    val response_value: String
)

@Serializable
data class FeedbackSubmission(
    val feedback_type: String, // "episode", "journey", "general"
    val context_id: String, // episode_id or journey_id
    val responses: List<FeedbackResponse>
)

@Serializable
data class FeedbackSubmissionResponse(
    val submission_id: String?,
    val feedback_type: String,
    val context_id: String,
    val user_id: String,
    val responses: List<FeedbackResponse>,
    val submitted_at: String
)

@Serializable
data class MyFeedbackSubmissionsResponse(
    val submissions: List<FeedbackSubmissionResponse>,
    val total_count: Int
)

sealed class FeedbackResult<T> {
    data class Success<T>(val data: T) : FeedbackResult<T>()
    data class Error<T>(val message: String, val throwable: Throwable? = null) : FeedbackResult<T>()
    data class Loading<T>(val message: String = "Loading...") : FeedbackResult<T>()
}

@Singleton
class FeedbackRepository @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val apiService: AuraApiService,
    private val client: OkHttpClient,
    private val json: Json,
    private val feedbackQuestionDao: com.wisme.firstapp.data.local.dao.FeedbackQuestionDao,
    private val feedbackSubmissionDao: com.wisme.firstapp.data.local.dao.FeedbackSubmissionDao,
    private val networkConnectivityManager: com.wisme.firstapp.data.network.NetworkConnectivityManager,
    private val feedbackSyncManager: com.wisme.firstapp.data.sync.FeedbackSyncManager
) {
    companion object {
        private const val TAG = "FeedbackRepository"
        private const val BASE_URL = "https://aura-backend-ok92.onrender.com/api/v1"
        private const val FEEDBACK_TYPE_EPISODE = "episode"
        private const val FEEDBACK_TYPE_JOURNEY = "journey" 
        private const val FEEDBACK_TYPE_GENERAL = "general" // Using general for general feedback
    }

    /**
     * Get feedback questions for a specific feedback type (OFFLINE-FIRST)
     * 1. Try to get from local database first
     * 2. If network available, fetch from API and update local cache
     * 3. If offline, return cached data
     */
    suspend fun getFeedbackQuestions(feedbackType: String): FeedbackResult<List<FeedbackQuestion>> {
        println("FeedbackRepository: Requesting feedback questions for type: '$feedbackType'")
        return try {
            // First, try to get from local cache
            val cachedQuestions = feedbackQuestionDao.getQuestionsByType(feedbackType)
            
            if (networkConnectivityManager.isCurrentlyOnline()) {
                // Online: fetch from API and update cache
                try {
                    val response = withContext(Dispatchers.IO) {
                        apiService.getFeedbackQuestions(feedbackType)
                    }

                    if (response.isSuccessful && response.body() != null) {
                        val apiQuestions = response.body()!!.map { apiQuestion ->
                            FeedbackQuestion(
                                question_id = apiQuestion.question_id,
                                question_text = apiQuestion.question_text,
                                response_type = apiQuestion.response_type,
                                options = apiQuestion.options
                            )
                        }
                        
                        // Update local cache
                        val entities = apiQuestions.map { it.toEntity(feedbackType) }
                        feedbackQuestionDao.deleteQuestionsByType(feedbackType) // Clear old cache
                        feedbackQuestionDao.insertQuestions(entities)
                        
                        Log.d(TAG, "Successfully fetched and cached ${apiQuestions.size} questions for $feedbackType")
                        FeedbackResult.Success(apiQuestions)
                    } else {
                        // API failed, return cached data if available
                        if (cachedQuestions.isNotEmpty()) {
                            Log.w(TAG, "API failed, returning cached questions for $feedbackType")
                            FeedbackResult.Success(cachedQuestions.map { it.toApiModel() })
                        } else {
                            val errorMsg = "Failed to fetch questions: ${response.code()} - ${response.errorBody()?.string()}"
                            Log.e(TAG, errorMsg)
                            FeedbackResult.Error(errorMsg)
                        }
                    }
                } catch (e: Exception) {
                    // Network error, return cached data if available
                    if (cachedQuestions.isNotEmpty()) {
                        Log.w(TAG, "Network error, returning cached questions for $feedbackType", e)
                        FeedbackResult.Success(cachedQuestions.map { it.toApiModel() })
                    } else {
                        Log.e(TAG, "Network error and no cached data for $feedbackType", e)
                        FeedbackResult.Error("Network error: ${e.message}", e)
                    }
                }
            } else {
                // Offline: return cached data
                if (cachedQuestions.isNotEmpty()) {
                    Log.d(TAG, "Offline: returning ${cachedQuestions.size} cached questions for $feedbackType")
                    FeedbackResult.Success(cachedQuestions.map { it.toApiModel() })
                } else {
                    Log.w(TAG, "Offline and no cached questions available for $feedbackType")
                    FeedbackResult.Error("No internet connection and no cached data available")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in getFeedbackQuestions", e)
            FeedbackResult.Error("Unexpected error: ${e.message}", e)
        }
    }

    /**
     * Submit episode feedback
     */
    suspend fun submitEpisodeFeedback(
        episodeId: String,
        enjoyment: String,
        clarity: String
    ): FeedbackResult<FeedbackSubmissionResponse> {
        return submitFeedback(
            feedbackType = FEEDBACK_TYPE_EPISODE,
            contextId = episodeId,
            responses = listOf(
                FeedbackResponse("episode_enjoyment", enjoyment),
                FeedbackResponse("episode_clarity", clarity)
            )
        )
    }

    /**
     * Submit journey feedback
     */
    suspend fun submitJourneyFeedback(
        journeyId: String,
        moocsComparison: String,
        youtubeComparison: String,
        blogsComparison: String
    ): FeedbackResult<FeedbackSubmissionResponse> {
        return submitFeedback(
            feedbackType = FEEDBACK_TYPE_JOURNEY,
            contextId = journeyId,
            responses = listOf(
                FeedbackResponse("journey_moocs_comparison", moocsComparison),
                FeedbackResponse("journey_youtube_comparison", youtubeComparison),
                FeedbackResponse("journey_blogs_comparison", blogsComparison)
            )
        )
    }

    /**
     * Submit general feedback
     */
    suspend fun submitGeneralFeedback(
        wouldRevisit: String,
        wouldRecommend: String,
        willingnessToPay: String
    ): FeedbackResult<FeedbackSubmissionResponse> {
        return submitFeedback(
            feedbackType = FEEDBACK_TYPE_GENERAL,
            contextId = "general", // General feedback doesn't have specific context
            responses = listOf(
                FeedbackResponse("general_would_revisit", wouldRevisit),
                FeedbackResponse("general_would_recommend", wouldRecommend),
                FeedbackResponse("general_willingness_to_pay", willingnessToPay)
            )
        )
    }

    /**
     * Generic feedback submission method (OFFLINE-FIRST)
     * 1. Always save to local database first
     * 2. If online, try to sync immediately 
     * 3. If offline, mark for later sync
     */
    private suspend fun submitFeedback(
        feedbackType: String,
        contextId: String,
        responses: List<FeedbackResponse>
    ): FeedbackResult<FeedbackSubmissionResponse> {
        return try {
            val firebaseToken = authPreferences.firebaseToken
            val userId = authPreferences.userId ?: "unknown_user"
            
            // Generate unique submission ID
            val submissionId = UUID.randomUUID().toString()
            val currentTime = System.currentTimeMillis()
            
            // Create local submission entity
            val localSubmission = FeedbackSubmissionEntity(
                submissionId = submissionId,
                feedbackType = feedbackType,
                contextId = contextId,
                userId = userId,
                responses = responses.map { it.toLocal() },
                submittedAt = currentTime,
                createdAt = currentTime,
                lastModified = currentTime,
                syncStatus = if (networkConnectivityManager.isCurrentlyOnline() && !firebaseToken.isNullOrBlank()) 
                    SyncStatus.PENDING else SyncStatus.PENDING,
                version = 1
            )
            
            // Always save to local database first
            feedbackSubmissionDao.insertSubmission(localSubmission)
            Log.d(TAG, "Saved feedback locally: $feedbackType for $contextId")
            
            // Try immediate sync if online and authenticated
            println("FeedbackRepository: Checking sync conditions - Online: ${networkConnectivityManager.isCurrentlyOnline()}, HasToken: ${!firebaseToken.isNullOrBlank()}")
            if (networkConnectivityManager.isCurrentlyOnline() && !firebaseToken.isNullOrBlank()) {
                println("FeedbackRepository: Attempting immediate sync to backend...")
                try {
                    val syncResult = syncSubmissionToBackend(localSubmission, firebaseToken)
                    println("FeedbackRepository: Sync result - Success: ${syncResult.isSuccess}, Error: ${syncResult.error}")
                    if (syncResult.isSuccess) {
                        // Update sync status to SYNCED
                        feedbackSubmissionDao.updateSyncStatusWithTimestamp(
                            submissionId, 
                            SyncStatus.SYNCED, 
                            System.currentTimeMillis()
                        )
                        Log.d(TAG, "Successfully synced $feedbackType feedback for $contextId")
                        
                        // Return success with backend response
                        val backendResponse = syncResult.response!!
                        return FeedbackResult.Success(backendResponse)
                    } else {
                        // Sync failed, mark for retry
                        feedbackSubmissionDao.updateSyncStatus(submissionId, SyncStatus.FAILED)
                        Log.w(TAG, "Immediate sync failed for $feedbackType, will retry later: ${syncResult.error}")
                    }
                } catch (e: Exception) {
                    // Sync failed, mark for retry
                    feedbackSubmissionDao.updateSyncStatus(submissionId, SyncStatus.FAILED)
                    Log.w(TAG, "Exception during immediate sync for $feedbackType, will retry later", e)
                }
                
                // Schedule background sync for failed submissions
                feedbackSyncManager.scheduleImmediateSync()
            } else {
                // Offline or not authenticated - schedule sync for when conditions are met
                println("FeedbackRepository: Cannot sync immediately - scheduling for later")
                println("FeedbackRepository: Online: ${networkConnectivityManager.isCurrentlyOnline()}, Firebase token present: ${!firebaseToken.isNullOrBlank()}")
                feedbackSyncManager.scheduleImmediateSync()
                Log.d(TAG, "Offline submission - sync scheduled for when network is available")
            }
            
            // Return success even if sync failed - data is saved locally
            val mockResponse = FeedbackSubmissionResponse(
                submission_id = submissionId,
                feedback_type = feedbackType,
                context_id = contextId,
                user_id = userId,
                responses = responses,
                submitted_at = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                    .format(java.util.Date(currentTime))
            )
            
            Log.d(TAG, "Feedback saved locally (sync will happen later): $feedbackType for $contextId")
            FeedbackResult.Success(mockResponse)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving feedback locally", e)
            FeedbackResult.Error("Failed to save feedback: ${e.message}", e)
        }
    }
    
    /**
     * Sync a single submission to backend using API service
     */
    private suspend fun syncSubmissionToBackend(
        submission: FeedbackSubmissionEntity, 
        firebaseToken: String
    ): SyncResult {
        return try {
            val apiSubmission = FeedbackSubmissionRequest(
                feedback_type = submission.feedbackType,
                context_id = submission.contextId,
                responses = submission.responses.map { 
                    FeedbackResponseItem(
                        question_id = it.questionId,
                        response_value = it.responseValue
                    )
                }
            )

            println("FeedbackRepository: Submitting feedback - Type: ${submission.feedbackType}, Context: ${submission.contextId}, Responses: ${apiSubmission.responses.size}")
            
            val response = withContext(Dispatchers.IO) {
                apiService.submitFeedback("Bearer $firebaseToken", apiSubmission)
            }

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                println("FeedbackRepository: Feedback submitted successfully - ID: ${apiResponse.id}")
                
                // Convert API response to local format
                val localResponse = FeedbackSubmissionResponse(
                    submission_id = apiResponse.id ?: "unknown", // Backend uses 'id' field
                    feedback_type = apiResponse.feedback_type,
                    context_id = apiResponse.context_id ?: "unknown",
                    user_id = apiResponse.user_id,
                    responses = apiResponse.responses.map { 
                        FeedbackResponse(
                            question_id = it.question_id,
                            response_value = it.response_value
                        )
                    },
                    submitted_at = apiResponse.submitted_at
                )
                
                SyncResult(isSuccess = true, response = localResponse, error = null)
            } else {
                val errorMessage = "HTTP ${response.code()}: ${response.errorBody()?.string()}"
                println("FeedbackRepository: Feedback submission failed - $errorMessage")
                SyncResult(isSuccess = false, response = null, error = errorMessage)
            }
        } catch (e: Exception) {
            println("FeedbackRepository: Exception during submission - ${e.message}")
            SyncResult(isSuccess = false, response = null, error = e.message ?: "Unknown error")
        }
    }
    
    /**
     * Helper class for sync results
     */
    private data class SyncResult(
        val isSuccess: Boolean,
        val response: FeedbackSubmissionResponse?,
        val error: String?
    )

    /**
     * Get user's previous feedback submissions
     */
    /**
     * Get user's feedback submissions (OFFLINE-FIRST)
     * 1. Always return local submissions first
     * 2. If online, sync with backend in background
     */
    suspend fun getMyFeedbackSubmissions(): FeedbackResult<MyFeedbackSubmissionsResponse> {
        return try {
            val userId = authPreferences.userId ?: return FeedbackResult.Error("User ID not found")
            
            // Always get local submissions first
            val localSubmissions = feedbackSubmissionDao.getSubmissionsByUser(userId)
            
            // Convert local entities to API format
            val apiSubmissions = localSubmissions.map { entity ->
                FeedbackSubmissionResponse(
                    submission_id = entity.submissionId,
                    feedback_type = entity.feedbackType,
                    context_id = entity.contextId,
                    user_id = entity.userId,
                    responses = entity.responses.map { it.toApiModel() },
                    submitted_at = entity.submittedAt.toString()
                )
            }
            
            val localResponse = MyFeedbackSubmissionsResponse(
                submissions = apiSubmissions,
                total_count = apiSubmissions.size
            )
            
            // If online, sync with backend in background (fire and forget)
            if (networkConnectivityManager.isCurrentlyOnline()) {
                // Launch background sync in IO dispatcher to avoid NetworkOnMainThreadException
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        syncSubmissionsWithBackend(userId)
                    } catch (e: Exception) {
                        Log.w(TAG, "Background sync failed, continuing with local data", e)
                    }
                }
            }
            
            FeedbackResult.Success(localResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching feedback submissions", e)
            FeedbackResult.Error("Error fetching submissions: ${e.message}", e)
        }
    }
    
    /**
     * Background sync of submissions with backend using proper API service
     */
    private suspend fun syncSubmissionsWithBackend(userId: String) {
        try {
            val firebaseToken = authPreferences.firebaseToken
            if (firebaseToken.isNullOrBlank()) return
            
            // Use the proper API service instead of manual HTTP calls
            val response = apiService.getMyFeedbackSubmissions(
                token = "Bearer $firebaseToken",
                limit = 100,
                offset = 0
            )

            if (response.isSuccessful && response.body() != null) {
                val backendSubmissions = response.body()!!
                
                // Update local database with backend data
                val backendEntities = backendSubmissions.map { apiSubmission ->
                    com.wisme.firstapp.data.local.entities.FeedbackSubmissionEntity(
                        submissionId = apiSubmission.id ?: "unknown",
                        feedbackType = apiSubmission.feedback_type,
                        contextId = apiSubmission.context_id ?: "unknown",
                        userId = apiSubmission.user_id,
                        responses = apiSubmission.responses.map { 
                            com.wisme.firstapp.data.local.entities.FeedbackResponseLocal(
                                questionId = it.question_id,
                                responseValue = it.response_value
                            )
                        },
                        submittedAt = apiSubmission.submitted_at.toLongOrNull() ?: System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis(),
                        lastModified = System.currentTimeMillis(),
                        syncStatus = com.wisme.firstapp.data.local.entities.SyncStatus.SYNCED
                    )
                }
                
                // Insert or update backend submissions
                feedbackSubmissionDao.insertSubmissions(backendEntities)
                Log.d(TAG, "Successfully synced ${backendEntities.size} submissions from backend")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync submissions with backend", e)
        }
    }

    /**
     * Check if user has already submitted feedback for a specific context (OFFLINE-FIRST)
     */
    suspend fun hasFeedbackSubmission(feedbackType: String, contextId: String): Boolean {
        return try {
            val userId = authPreferences.userId ?: return false
            feedbackSubmissionDao.hasSubmission(feedbackType, contextId)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking feedback submission", e)
            false
        }
    }

    /**
     * Get previous responses for a specific feedback context
     */
    /**
     * Get previous responses for specific context (OFFLINE-FIRST)
     */
    suspend fun getPreviousResponses(
        feedbackType: String, 
        contextId: String
    ): FeedbackResult<List<FeedbackResponse>> {
        return try {
            val userId = authPreferences.userId ?: return FeedbackResult.Error("User ID not found")
            
            // Check local database first
            val localSubmissions = feedbackSubmissionDao.getSubmissionsByType(feedbackType)
            val matchingSubmission = localSubmissions.find { 
                it.contextId == contextId && it.userId == userId 
            }
            
            if (matchingSubmission != null) {
                val responses = matchingSubmission.responses.map { it.toApiModel() }
                FeedbackResult.Success(responses)
            } else {
                FeedbackResult.Success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting previous responses", e)
            FeedbackResult.Error("Error getting previous responses: ${e.message}", e)
        }
    }

    /**
     * Delete a feedback submission
     */
    suspend fun deleteFeedbackSubmission(submissionId: String): FeedbackResult<Unit> {
        return try {
            val firebaseToken = authPreferences.firebaseToken
            if (firebaseToken.isNullOrBlank()) {
                return FeedbackResult.Error("Authentication token not found")
            }

            val request = Request.Builder()
                .url("$BASE_URL/feedback/my-submissions/$submissionId")
                .addHeader("Authorization", "Bearer $firebaseToken")
                .delete()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d(TAG, "Successfully deleted feedback submission: $submissionId")
                FeedbackResult.Success(Unit)
            } else {
                val errorMsg = "Failed to delete submission: ${response.code}"
                Log.e(TAG, errorMsg)
                FeedbackResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting feedback submission", e)
            FeedbackResult.Error("Network error: ${e.message}", e)
        }
    }
}
