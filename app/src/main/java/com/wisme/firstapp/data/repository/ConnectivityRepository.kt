package com.wisme.firstapp.data.repository

import com.wisme.firstapp.data.api.AuraApiService
import com.wisme.firstapp.data.api.HealthApiService
import com.wisme.firstapp.util.Logger
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityRepository @Inject constructor(
    private val apiService: AuraApiService,
    private val healthApiService: HealthApiService
) {
    
    /**
     * Check if the backend is reachable and healthy
     */
    suspend fun checkBackendHealth(): BackendStatus {
        val startTime = System.currentTimeMillis()
        Logger.logRepository("ConnectivityRepository", "checkBackendHealth - Starting health check")
        
        return try {
            // Set a timeout for health check
            withTimeout(5000) { // 5 seconds timeout
                val response = healthApiService.healthCheck()
                val responseTime = System.currentTimeMillis() - startTime
                
                if (response.isSuccessful) {
                    Logger.logHealthCheck(
                        serviceName = "Backend",
                        isHealthy = true,
                        responseTime = responseTime
                    )
                    Logger.logRepository("ConnectivityRepository", "checkBackendHealth", success = true)
                    BackendStatus.Healthy
                } else {
                    val errorMessage = "Backend returned ${response.code()}: ${response.message()}"
                    Logger.logHealthCheck(
                        serviceName = "Backend",
                        isHealthy = false,
                        responseTime = responseTime,
                        errorMessage = errorMessage
                    )
                    Logger.logRepository("ConnectivityRepository", "checkBackendHealth", success = false, errorMessage = errorMessage)
                    BackendStatus.Unhealthy(errorMessage)
                }
            }
        } catch (e: TimeoutCancellationException) {
            val responseTime = System.currentTimeMillis() - startTime
            val errorMessage = "Connection timeout after ${responseTime}ms"
            Logger.logHealthCheck(
                serviceName = "Backend",
                isHealthy = false,
                responseTime = responseTime,
                errorMessage = errorMessage
            )
            Logger.logRepository("ConnectivityRepository", "checkBackendHealth", success = false, errorMessage = errorMessage)
            BackendStatus.Unreachable(errorMessage)
        } catch (e: Exception) {
            val responseTime = System.currentTimeMillis() - startTime
            val errorMessage = "Network error: ${e.message}"
            Logger.logHealthCheck(
                serviceName = "Backend",
                isHealthy = false,
                responseTime = responseTime,
                errorMessage = errorMessage
            )
            Logger.logRepository("ConnectivityRepository", "checkBackendHealth", success = false, errorMessage = errorMessage, additionalData = mapOf("exception" to e.javaClass.simpleName))
            BackendStatus.Unreachable(errorMessage)
        }
    }
    
    /**
     * Check authentication service health
     */
    suspend fun checkAuthHealth(): BackendStatus {
        val startTime = System.currentTimeMillis()
        Logger.logRepository("ConnectivityRepository", "checkAuthHealth - Starting auth health check")
        
        return try {
            withTimeout(5000) {
                val response = apiService.authHealthCheck()
                val responseTime = System.currentTimeMillis() - startTime
                
                if (response.isSuccessful) {
                    Logger.logHealthCheck(
                        serviceName = "Auth Service",
                        isHealthy = true,
                        responseTime = responseTime
                    )
                    BackendStatus.Healthy
                } else {
                    val errorMessage = "Auth service unavailable: ${response.code()}"
                    Logger.logHealthCheck(
                        serviceName = "Auth Service",
                        isHealthy = false,
                        responseTime = responseTime,
                        errorMessage = errorMessage
                    )
                    BackendStatus.Unhealthy(errorMessage)
                }
            }
        } catch (e: TimeoutCancellationException) {
            val responseTime = System.currentTimeMillis() - startTime
            val errorMessage = "Auth service timeout after ${responseTime}ms"
            Logger.logHealthCheck(
                serviceName = "Auth Service",
                isHealthy = false,
                responseTime = responseTime,
                errorMessage = errorMessage
            )
            BackendStatus.Unreachable(errorMessage)
        } catch (e: Exception) {
            BackendStatus.Unreachable("Auth service error: ${e.message}")
        }
    }
    
    /**
     * Check users service health
     */
    suspend fun checkUsersHealth(): BackendStatus {
        return try {
            withTimeout(5000) {
                val response = apiService.usersHealthCheck()
                if (response.isSuccessful) {
                    BackendStatus.Healthy
                } else {
                    BackendStatus.Unhealthy("Users service unavailable")
                }
            }
        } catch (e: TimeoutCancellationException) {
            BackendStatus.Unreachable("Users service timeout")
        } catch (e: Exception) {
            BackendStatus.Unreachable("Users service error: ${e.message}")
        }
    }
    
    /**
     * Comprehensive backend health check
     */
    suspend fun checkAllServices(): Map<String, BackendStatus> {
        return mapOf(
            "main" to checkBackendHealth(),
            "auth" to checkAuthHealth(),
            "users" to checkUsersHealth()
        )
    }
}

sealed class BackendStatus {
    object Healthy : BackendStatus()
    data class Unhealthy(val reason: String) : BackendStatus()
    data class Unreachable(val reason: String) : BackendStatus()
    
    val isHealthy: Boolean
        get() = this is Healthy
        
    val message: String
        get() = when (this) {
            is Healthy -> "Service is healthy"
            is Unhealthy -> reason
            is Unreachable -> reason
        }
}