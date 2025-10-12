package com.wisme.firstapp.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Response
import okhttp3.Response as OkHttpResponse
import java.text.SimpleDateFormat
import java.util.*

/**
 * Centralized logging utility for the Nova app
 * Provides structured logging for API calls, errors, and debugging
 */
object Logger {
    
    private const val TAG = "NovaApp"
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    /**
     * Log debug messages
     */
    fun d(message: String, tag: String = TAG) {
        Log.d(tag, "${getTimestamp()} - $message")
    }
    
    /**
     * Log info messages
     */
    fun i(message: String, tag: String = TAG) {
        Log.i(tag, "${getTimestamp()} - $message")
    }
    
    /**
     * Log warning messages
     */
    fun w(message: String, tag: String = TAG, throwable: Throwable? = null) {
        Log.w(tag, "${getTimestamp()} - $message", throwable)
    }
    
    /**
     * Log error messages
     */
    fun e(message: String, tag: String = TAG, throwable: Throwable? = null) {
        Log.e(tag, "${getTimestamp()} - $message", throwable)
    }
    
    /**
     * Log API requests with detailed information
     */
    fun logApiRequest(
        method: String,
        url: String,
        headers: Map<String, String>? = null,
        body: Any? = null,
        tag: String = "API_REQUEST"
    ) {
        val logMessage = buildString {
            appendLine("🚀 API REQUEST")
            appendLine("Method: $method")
            appendLine("URL: $url")
            
            headers?.let {
                appendLine("Headers:")
                it.forEach { (key, value) ->
                    // Mask sensitive headers
                    val maskedValue = if (key.lowercase().contains("authorization") || 
                                        key.lowercase().contains("token")) {
                        "***MASKED***"
                    } else {
                        value
                    }
                    appendLine("  $key: $maskedValue")
                }
            }
            
            body?.let {
                appendLine("Request Body:")
                appendLine(gson.toJson(it))
            }
        }
        
        d(logMessage, tag)
    }
    
    /**
     * Log API responses with detailed information
     */
    fun logApiResponse(
        method: String,
        url: String,
        response: Response<*>,
        responseBody: String? = null,
        tag: String = "API_RESPONSE"
    ) {
        val logMessage = buildString {
            appendLine("📥 API RESPONSE")
            appendLine("Method: $method")
            appendLine("URL: $url")
            appendLine("Status Code: ${response.code()}")
            appendLine("Status Message: ${response.message()}")
            
            // Log response headers
            appendLine("Response Headers:")
            response.headers().forEach { (key, value) ->
                appendLine("  $key: $value")
            }
            
            // Log response body
            responseBody?.let {
                appendLine("Response Body:")
                try {
                    // Try to pretty print JSON
                    val jsonObject = gson.fromJson(it, Any::class.java)
                    appendLine(gson.toJson(jsonObject))
                } catch (e: Exception) {
                    // If not JSON, log as-is
                    appendLine(it)
                }
            }
            
            if (response.isSuccessful) {
                appendLine("✅ SUCCESS")
            } else {
                appendLine("❌ FAILED")
                response.errorBody()?.let { errorBody ->
                    try {
                        val errorString = errorBody.string()
                        appendLine("Error Body:")
                        appendLine(errorString)
                    } catch (e: Exception) {
                        appendLine("Could not read error body: ${e.message}")
                    }
                }
            }
        }
        
        if (response.isSuccessful) {
            i(logMessage, tag)
        } else {
            e(logMessage, tag)
        }
    }
    
    /**
     * Log API responses with detailed information (OkHttp Response)
     */
    fun logApiResponse(
        method: String,
        url: String,
        response: OkHttpResponse,
        responseBody: String? = null,
        tag: String = "API_RESPONSE"
    ) {
        val logMessage = buildString {
            appendLine("📥 API RESPONSE")
            appendLine("Method: $method")
            appendLine("URL: $url")
            appendLine("Status Code: ${response.code}")
            appendLine("Status Message: ${response.message}")
            
            // Log response headers
            appendLine("Response Headers:")
            response.headers.forEach { (key, value) ->
                appendLine("  $key: $value")
            }
            
            // Log response body
            responseBody?.let {
                appendLine("Response Body:")
                try {
                    // Try to pretty print JSON
                    val jsonObject = gson.fromJson(it, Any::class.java)
                    appendLine(gson.toJson(jsonObject))
                } catch (e: Exception) {
                    // If not JSON, log as-is
                    appendLine(it)
                }
            }
            
            if (response.isSuccessful) {
                appendLine("✅ SUCCESS")
            } else {
                appendLine("❌ FAILED")
            }
        }
        
        if (response.isSuccessful) {
            i(logMessage, tag)
        } else {
            e(logMessage, tag)
        }
    }
    
    /**
     * Log API errors with detailed information
     */
    fun logApiError(
        method: String,
        url: String,
        error: Throwable,
        tag: String = "API_ERROR"
    ) {
        val logMessage = buildString {
            appendLine("💥 API ERROR")
            appendLine("Method: $method")
            appendLine("URL: $url")
            appendLine("Error Type: ${error.javaClass.simpleName}")
            appendLine("Error Message: ${error.message}")
            
            // Log stack trace for debugging
            appendLine("Stack Trace:")
            error.stackTrace.take(10).forEach { stackTraceElement ->
                appendLine("  at $stackTraceElement")
            }
        }
        
        e(logMessage, tag, error)
    }
    
    /**
     * Log backend health check results
     */
    fun logHealthCheck(
        serviceName: String,
        isHealthy: Boolean,
        responseTime: Long? = null,
        errorMessage: String? = null,
        tag: String = "HEALTH_CHECK"
    ) {
        val logMessage = buildString {
            appendLine("🏥 HEALTH CHECK - $serviceName")
            appendLine("Status: ${if (isHealthy) "✅ HEALTHY" else "❌ UNHEALTHY"}")
            responseTime?.let {
                appendLine("Response Time: ${it}ms")
            }
            errorMessage?.let {
                appendLine("Error: $it")
            }
        }
        
        if (isHealthy) {
            i(logMessage, tag)
        } else {
            w(logMessage, tag)
        }
    }
    
    /**
     * Log authentication events
     */
    fun logAuth(
        event: String,
        userId: String? = null,
        success: Boolean = true,
        errorMessage: String? = null,
        tag: String = "AUTH"
    ) {
        val logMessage = buildString {
            appendLine("🔐 AUTH EVENT - $event")
            userId?.let {
                appendLine("User ID: $it")
            }
            appendLine("Status: ${if (success) "✅ SUCCESS" else "❌ FAILED"}")
            errorMessage?.let {
                appendLine("Error: $it")
            }
        }
        
        if (success) {
            i(logMessage, tag)
        } else {
            e(logMessage, tag)
        }
    }
    
    /**
     * Log journey operations
     */
    fun logJourney(
        operation: String,
        journeyName: String? = null,
        success: Boolean = true,
        errorMessage: String? = null,
        tag: String = "JOURNEY"
    ) {
        val logMessage = buildString {
            appendLine("🗺️ JOURNEY EVENT - $operation")
            journeyName?.let {
                appendLine("Journey: $it")
            }
            appendLine("Status: ${if (success) "✅ SUCCESS" else "❌ FAILED"}")
            errorMessage?.let {
                appendLine("Error: $it")
            }
        }
        
        if (success) {
            i(logMessage, tag)
        } else {
            e(logMessage, tag)
        }
    }
    
    /**
     * Log repository operations
     */
    fun logRepository(
        repository: String,
        operation: String,
        success: Boolean = true,
        errorMessage: String? = null,
        additionalData: Map<String, Any>? = null,
        tag: String = "REPOSITORY"
    ) {
        val logMessage = buildString {
            appendLine("🗃️ REPOSITORY - $repository")
            appendLine("Operation: $operation")
            appendLine("Status: ${if (success) "✅ SUCCESS" else "❌ FAILED"}")
            errorMessage?.let {
                appendLine("Error: $it")
            }
            additionalData?.let { data ->
                appendLine("Additional Data:")
                data.forEach { (key, value) ->
                    appendLine("  $key: $value")
                }
            }
        }
        
        if (success) {
            d(logMessage, tag)
        } else {
            e(logMessage, tag)
        }
    }
    
    /**
     * Log view model operations
     */
    fun logViewModel(
        viewModel: String,
        operation: String,
        success: Boolean = true,
        errorMessage: String? = null,
        tag: String = "VIEWMODEL"
    ) {
        val logMessage = buildString {
            appendLine("📱 VIEWMODEL - $viewModel")
            appendLine("Operation: $operation")
            appendLine("Status: ${if (success) "✅ SUCCESS" else "❌ FAILED"}")
            errorMessage?.let {
                appendLine("Error: $it")
            }
        }
        
        if (success) {
            d(logMessage, tag)
        } else {
            e(logMessage, tag)
        }
    }
    
    private fun getTimestamp(): String {
        return dateFormatter.format(Date())
    }
}
