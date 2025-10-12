package com.wisme.research.util

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Response
import okhttp3.Response as OkHttpResponse

/**
 * Utility class for parsing and understanding backend error responses
 * Helps developers understand what needs to be changed based on backend errors
 */
object ErrorResponseParser {
    
    private val gson = Gson()
    
    /**
     * Parse and analyze backend error response (Retrofit Response)
     */
    fun parseErrorResponse(response: Response<*>): ErrorAnalysis {
        val errorBody = response.errorBody()?.string()
        val statusCode = response.code()
        val statusMessage = response.message()
        
        Logger.e("Backend Error Response Analysis", "ERROR_PARSER")
        Logger.e("Status Code: $statusCode", "ERROR_PARSER")
        Logger.e("Status Message: $statusMessage", "ERROR_PARSER")
        Logger.e("Error Body: $errorBody", "ERROR_PARSER")
        
        return ErrorAnalysis(
            statusCode = statusCode,
            statusMessage = statusMessage,
            errorBody = errorBody,
            parsedError = tryParseErrorBody(errorBody),
            suggestions = generateSuggestions(statusCode, errorBody)
        )
    }
    
    /**
     * Parse and analyze backend error response (OkHttp Response)
     */
    fun parseErrorResponse(response: OkHttpResponse): ErrorAnalysis {
        val errorBody = try {
            response.body?.string()
        } catch (e: Exception) {
            "Could not read response body: ${e.message}"
        }
        val statusCode = response.code
        val statusMessage = response.message
        
        Logger.e("Backend Error Response Analysis", "ERROR_PARSER")
        Logger.e("Status Code: $statusCode", "ERROR_PARSER")
        Logger.e("Status Message: $statusMessage", "ERROR_PARSER")
        Logger.e("Error Body: $errorBody", "ERROR_PARSER")
        
        return ErrorAnalysis(
            statusCode = statusCode,
            statusMessage = statusMessage,
            errorBody = errorBody,
            parsedError = tryParseErrorBody(errorBody),
            suggestions = generateSuggestions(statusCode, errorBody)
        )
    }
    
    /**
     * Try to parse error body as JSON
     */
    private fun tryParseErrorBody(errorBody: String?): ParsedError? {
        if (errorBody.isNullOrEmpty()) return null
        
        return try {
            // Try standard error format first
            gson.fromJson(errorBody, StandardErrorResponse::class.java)?.let { standardError ->
                ParsedError.Standard(standardError)
            } ?: run {
                // Try Aura API specific format
                gson.fromJson(errorBody, AuraErrorResponse::class.java)?.let { auraError ->
                    ParsedError.Aura(auraError)
                } ?: run {
                    // Fallback to raw text
                    ParsedError.Raw(errorBody)
                }
            }
        } catch (e: JsonSyntaxException) {
            Logger.w("Could not parse error response as JSON: ${e.message}", "ERROR_PARSER")
            ParsedError.Raw(errorBody)
        } catch (e: Exception) {
            Logger.w("Unexpected error parsing response: ${e.message}", "ERROR_PARSER")
            ParsedError.Raw(errorBody)
        }
    }
    
    /**
     * Generate suggestions based on error code and message
     */
    private fun generateSuggestions(statusCode: Int, errorBody: String?): List<String> {
        val suggestions = mutableListOf<String>()
        
        when (statusCode) {
            400 -> {
                suggestions.add("❌ BAD REQUEST - Check request format and required fields")
                if (errorBody?.contains("token", ignoreCase = true) == true) {
                    suggestions.add("🔑 Token issue - Verify Firebase token format and validity")
                }
                if (errorBody?.contains("avatar", ignoreCase = true) == true) {
                    suggestions.add("🖼️ Avatar issue - Check avatar_id field and valid values")
                }
                if (errorBody?.contains("validation", ignoreCase = true) == true) {
                    suggestions.add("📝 Validation error - Check all required fields are present and correctly formatted")
                }
            }
            
            401 -> {
                suggestions.add("🔐 UNAUTHORIZED - Authentication required or invalid")
                suggestions.add("🔑 Check Authorization header format: 'Bearer <firebase_token>'")
                suggestions.add("🔄 Try refreshing Firebase token")
            }
            
            403 -> {
                suggestions.add("🚫 FORBIDDEN - Valid authentication but insufficient permissions")
                suggestions.add("👤 Check if user has required permissions for this action")
            }
            
            404 -> {
                suggestions.add("🔍 NOT FOUND - Endpoint or resource doesn't exist")
                suggestions.add("🌐 Verify API endpoint URL is correct")
                if (errorBody?.contains("journey", ignoreCase = true) == true) {
                    suggestions.add("📚 Journey not found - Check journey ID is valid")
                }
            }
            
            422 -> {
                suggestions.add("❌ UNPROCESSABLE ENTITY - Request format is correct but data is invalid")
                suggestions.add("📋 Check data validation requirements")
                suggestions.add("🔍 Review required field formats and constraints")
            }
            
            500 -> {
                suggestions.add("💥 INTERNAL SERVER ERROR - Backend issue")
                suggestions.add("🔧 Check backend server logs")
                suggestions.add("⏳ Try again later - temporary server issue")
            }
            
            502, 503, 504 -> {
                suggestions.add("🌐 SERVICE UNAVAILABLE - Backend server not responding")
                suggestions.add("🔍 Check if backend server is running")
                suggestions.add("🌍 Verify ngrok tunnel or server URL is correct")
            }
            
            else -> {
                suggestions.add("❓ UNKNOWN ERROR - Review backend documentation for status code $statusCode")
            }
        }
        
        // Add general suggestions
        suggestions.add("📖 Check Aura API documentation for correct request format")
        suggestions.add("🔬 Review full request/response logs above for details")
        
        return suggestions
    }
    
    /**
     * Log detailed error analysis
     */
    fun logErrorAnalysis(analysis: ErrorAnalysis, context: String = "") {
        Logger.e("🚨 ERROR ANALYSIS ${if (context.isNotEmpty()) "- $context" else ""}", "ERROR_ANALYSIS")
        Logger.e("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "ERROR_ANALYSIS")
        Logger.e("Status: ${analysis.statusCode} ${analysis.statusMessage}", "ERROR_ANALYSIS")
        
        analysis.parsedError?.let { parsedError ->
            when (parsedError) {
                is ParsedError.Standard -> {
                    Logger.e("Error: ${parsedError.error.error}", "ERROR_ANALYSIS")
                    Logger.e("Message: ${parsedError.error.message}", "ERROR_ANALYSIS")
                }
                is ParsedError.Aura -> {
                    Logger.e("Success: ${parsedError.error.success}", "ERROR_ANALYSIS")
                    Logger.e("Message: ${parsedError.error.message}", "ERROR_ANALYSIS")
                    parsedError.error.error?.let {
                        Logger.e("Error Details: $it", "ERROR_ANALYSIS")
                    }
                }
                is ParsedError.Raw -> {
                    Logger.e("Raw Error Body: ${parsedError.body}", "ERROR_ANALYSIS")
                }
            }
        }
        
        Logger.e("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "ERROR_ANALYSIS")
        Logger.e("💡 SUGGESTIONS:", "ERROR_ANALYSIS")
        analysis.suggestions.forEachIndexed { index, suggestion ->
            Logger.e("${index + 1}. $suggestion", "ERROR_ANALYSIS")
        }
        Logger.e("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "ERROR_ANALYSIS")
    }
}

/**
 * Data class representing error analysis results
 */
data class ErrorAnalysis(
    val statusCode: Int,
    val statusMessage: String,
    val errorBody: String?,
    val parsedError: ParsedError?,
    val suggestions: List<String>
)

/**
 * Sealed class for different types of parsed errors
 */
sealed class ParsedError {
    data class Standard(val error: StandardErrorResponse) : ParsedError()
    data class Aura(val error: AuraErrorResponse) : ParsedError()
    data class Raw(val body: String) : ParsedError()
}

/**
 * Standard HTTP error response format
 */
data class StandardErrorResponse(
    val error: String,
    val message: String,
    val statusCode: Int? = null
)

/**
 * Aura API specific error response format
 */
data class AuraErrorResponse(
    val success: Boolean,
    val message: String,
    val error: String? = null,
    val details: Any? = null
)
