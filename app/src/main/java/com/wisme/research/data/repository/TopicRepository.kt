package com.wisme.research.data.repository

import com.wisme.research.data.api.AuraApiService
import com.wisme.research.data.api.TopicRequestSubmission
import com.wisme.research.data.api.TopicRequestResponse
import com.wisme.research.data.api.PopularTopicsResponse
import com.wisme.research.data.api.TopicRequestInfo
import com.wisme.research.util.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling topic requests and popular topics
 */
@Singleton
class TopicRepository @Inject constructor(
    private val apiService: AuraApiService,
    private val connectivityRepository: ConnectivityRepository
) {
    
    /**
     * Submit a topic request to the backend
     */
    suspend fun submitTopicRequest(
        token: String,
        topic: String,
        description: String? = null
    ): Result<TopicRequestResponse> {
        Logger.logRepository(
            repository = "TopicRepository",
            operation = "submitTopicRequest",
            additionalData = mapOf(
                "topic" to topic,
                "hasDescription" to !description.isNullOrEmpty()
            )
        )
        
        return try {
            // Check backend connectivity first
            val backendStatus = connectivityRepository.checkBackendHealth()
            if (!backendStatus.isHealthy) {
                val errorMessage = "Backend unavailable: ${backendStatus.message}"
                Logger.logRepository(
                    repository = "TopicRepository",
                    operation = "submitTopicRequest",
                    success = false,
                    errorMessage = errorMessage
                )
                return Result.failure(Exception(errorMessage))
            }
            
            val request = TopicRequestSubmission(
                topic_name = topic,
                description = description
            )
            
            println("TopicRepository: Submitting request - Topic: '$topic', Description: '$description'")
            println("TopicRepository: Request object: $request")
            println("TopicRepository: Token: Bearer ${token.take(20)}...")
            
            val response = apiService.submitTopicRequest("Bearer $token", request)
            
            println("TopicRepository: Response code: ${response.code()}")
            println("TopicRepository: Response message: ${response.message()}")
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                println("TopicRepository: Error body: $errorBody")
            }
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Logger.logRepository(
                    repository = "TopicRepository",
                    operation = "submitTopicRequest",
                    success = true,
                    additionalData = mapOf(
                        "requestId" to (responseBody.request_id ?: "unknown")
                    )
                )
                Result.success(responseBody)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = when (response.code()) {
                    422 -> {
                        println("TopicRepository: 422 Validation Error - Error body: $errorBody")
                        if (errorBody?.contains("topic_name") == true) {
                            "Invalid topic format. Please check your input and try again."
                        } else {
                            "Request validation failed. Please check your input."
                        }
                    }
                    401 -> "Authentication failed. Please log in again."
                    403 -> "You don't have permission to submit topic requests."
                    429 -> "Too many requests. Please wait a moment and try again."
                    500 -> "Server error. Please try again later."
                    else -> "Topic request failed: ${response.code()} ${response.message()}"
                }
                
                Logger.logRepository(
                    repository = "TopicRepository",
                    operation = "submitTopicRequest",
                    success = false,
                    errorMessage = errorMessage,
                    additionalData = mapOf(
                        "responseCode" to response.code(),
                        "errorBody" to (errorBody ?: "null")
                    )
                )
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.logRepository(
                repository = "TopicRepository",
                operation = "submitTopicRequest",
                success = false,
                errorMessage = "Exception: ${e.message}",
                additionalData = mapOf("exception" to e.javaClass.simpleName)
            )
            Result.failure(e)
        }
    }
    
    /**
     * Get popular topic requests from all users
     */
    suspend fun getPopularTopicRequests(): Result<List<TopicRequestInfo>> {
        Logger.logRepository(
            repository = "TopicRepository",
            operation = "getPopularTopicRequests"
        )
        
        return try {
            // Check backend connectivity first
            val backendStatus = connectivityRepository.checkBackendHealth()
            if (!backendStatus.isHealthy) {
                val errorMessage = "Backend unavailable: ${backendStatus.message}"
                Logger.logRepository(
                    repository = "TopicRepository",
                    operation = "getPopularTopicRequests",
                    success = false,
                    errorMessage = errorMessage
                )
                return Result.failure(Exception(errorMessage))
            }
            
            val response = apiService.getPopularTopicRequests()
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Logger.logRepository(
                    repository = "TopicRepository",
                    operation = "getPopularTopicRequests",
                    success = true,
                    additionalData = mapOf("topicCount" to responseBody.topics.size)
                )
                Result.success(responseBody.topics)
            } else {
                val errorMessage = "Failed to fetch popular topics: ${response.code()} ${response.message()}"
                Logger.logRepository(
                    repository = "TopicRepository",
                    operation = "getPopularTopicRequests",
                    success = false,
                    errorMessage = errorMessage
                )
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.logRepository(
                repository = "TopicRepository",
                operation = "getPopularTopicRequests",
                success = false,
                errorMessage = "Exception: ${e.message}",
                additionalData = mapOf("exception" to e.javaClass.simpleName)
            )
            Result.failure(e)
        }
    }
}
