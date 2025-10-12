package com.wisme.research.util

import com.wisme.research.util.Logger
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * HTTP interceptor for logging all API requests and responses
 * Automatically captures and logs detailed information about every API call
 */
class HttpLoggingInterceptor : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        
        // Extract request information
        val method = request.method
        val url = request.url.toString()
        val headers = request.headers.toMultimap().mapValues { it.value.joinToString(", ") }
        
        // Extract request body
        var requestBodyString: String? = null
        request.body?.let { requestBody ->
            try {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                val charset: Charset = requestBody.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                requestBodyString = buffer.readString(charset)
            } catch (e: Exception) {
                Logger.w("Could not read request body: ${e.message}", "HTTP_INTERCEPTOR")
            }
        }
        
        // Log the request
        Logger.logApiRequest(
            method = method,
            url = url,
            headers = headers,
            body = requestBodyString
        )
        
        // Execute the request
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            val endTime = System.currentTimeMillis()
            Logger.logApiError(
                method = method,
                url = url,
                error = e
            )
            Logger.e("Request failed after ${endTime - startTime}ms", "HTTP_INTERCEPTOR", e)
            throw e
        }
        
        val endTime = System.currentTimeMillis()
        val responseTime = endTime - startTime
        
        // Extract response body
        var responseBodyString: String? = null
        response.body?.let { responseBody ->
            try {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE)
                val buffer = source.buffer
                val charset: Charset = responseBody.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                responseBodyString = buffer.clone().readString(charset)
            } catch (e: Exception) {
                Logger.w("Could not read response body: ${e.message}", "HTTP_INTERCEPTOR")
            }
        }
        
        // Log the response
        Logger.logApiResponse(
            method = method,
            url = url,
            response = response,
            responseBody = responseBodyString
        )
        
        // If there's an error, parse and analyze it
        if (!response.isSuccessful) {
            val errorAnalysis = ErrorResponseParser.parseErrorResponse(response)
            ErrorResponseParser.logErrorAnalysis(errorAnalysis, "$method $url")
        }
        
        Logger.d("Request completed in ${responseTime}ms", "HTTP_TIMING")
        
        // Create a new response body since we consumed the original
        responseBodyString?.let { bodyString ->
            val newResponseBody = bodyString.toResponseBody(response.body?.contentType())
            return response.newBuilder()
                .body(newResponseBody)
                .build()
        }
        
        return response
    }
}
