package com.wisme.firstapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisme.firstapp.data.repository.TopicRepository
import com.wisme.firstapp.data.local.AuthPreferences
import com.wisme.firstapp.data.api.TopicRequestInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val authPrefs: AuthPreferences
) : ViewModel() {
    
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()
    
    private val _submitResult = MutableStateFlow<Result<String>?>(null)
    val submitResult: StateFlow<Result<String>?> = _submitResult.asStateFlow()
    
    private val _popularTopics = MutableStateFlow<List<TopicRequestInfo>>(emptyList())
    val popularTopics: StateFlow<List<TopicRequestInfo>> = _popularTopics.asStateFlow()
    
    private val _isLoadingPopularTopics = MutableStateFlow(false)
    val isLoadingPopularTopics: StateFlow<Boolean> = _isLoadingPopularTopics.asStateFlow()
    
    init {
        loadPopularTopics()
    }
    
    /**
     * Validate topic input according to backend requirements
     */
    private fun validateTopicInput(topic: String): String? {
        val trimmedTopic = topic.trim()
        
        // Check if empty after trimming
        if (trimmedTopic.isEmpty()) {
            return "Topic cannot be empty"
        }
        
        // Check minimum length
        if (trimmedTopic.length < 3) {
            return "Topic must be at least 3 characters long"
        }
        
        // Check maximum length
        if (trimmedTopic.length > 200) {
            return "Topic cannot exceed 200 characters"
        }
        
        // Check for potentially dangerous content
        val dangerousPatterns = listOf(
            "<script", "</script>", "javascript:", "vbscript:",
            "<iframe", "<object", "<embed", "<link", "<meta",
            "SELECT ", "INSERT ", "UPDATE ", "DELETE ", "DROP ", "CREATE ", "ALTER ",
            "UNION ", "WHERE ", "FROM ", "JOIN ", "HAVING ", "ORDER BY",
            "exec(", "eval(", "setTimeout(", "setInterval("
        )
        
        val topicLower = trimmedTopic.lowercase()
        for (pattern in dangerousPatterns) {
            if (topicLower.contains(pattern.lowercase())) {
                return "Topic contains invalid characters or content"
            }
        }
        
        return null // Valid input
    }
    
    /**
     * Submit a topic request
     */
    fun submitTopicRequest(topic: String, description: String? = null) {
        println("TopicViewModel: Starting topic request submission for topic: '$topic'")
        viewModelScope.launch {
            _isSubmitting.value = true
            _submitResult.value = null
            
            try {
                // Validate input first
                val validationError = validateTopicInput(topic)
                if (validationError != null) {
                    println("TopicViewModel: Validation failed - $validationError")
                    _submitResult.value = Result.failure(Exception(validationError))
                    return@launch
                }
                
                val token = authPrefs.firebaseToken
                val userId = authPrefs.userId
                println("TopicViewModel: Token present: ${!token.isNullOrEmpty()}, UserId: $userId")
                
                if (token.isNullOrEmpty()) {
                    println("TopicViewModel: Authentication failed - no token")
                    _submitResult.value = Result.failure(Exception("Authentication required"))
                    return@launch
                }
                
                val trimmedTopic = topic.trim()
                println("TopicViewModel: Validation passed. Submitting trimmed topic: '$trimmedTopic'")
                val result = topicRepository.submitTopicRequest(token, trimmedTopic, description)
                
                if (result.isSuccess) {
                    println("TopicViewModel: Topic request submitted successfully")
                    _submitResult.value = Result.success("Topic request submitted successfully!")
                    // Refresh popular topics after successful submission
                    loadPopularTopics()
                } else {
                    val error = result.exceptionOrNull()
                    println("TopicViewModel: Topic request failed - ${error?.message}")
                    _submitResult.value = Result.failure(
                        result.exceptionOrNull() ?: Exception("Failed to submit topic request")
                    )
                }
            } catch (e: Exception) {
                println("TopicViewModel: Exception during topic request: ${e.message}")
                _submitResult.value = Result.failure(e)
            } finally {
                _isSubmitting.value = false
            }
        }
    }
    
    /**
     * Load popular topic requests
     */
    fun loadPopularTopics() {
        viewModelScope.launch {
            _isLoadingPopularTopics.value = true
            
            try {
                val result = topicRepository.getPopularTopicRequests()
                
                if (result.isSuccess) {
                    _popularTopics.value = result.getOrNull() ?: emptyList()
                } else {
                    // Silently fail for popular topics - it's not critical
                    _popularTopics.value = emptyList()
                }
            } catch (e: Exception) {
                // Silently fail for popular topics
                _popularTopics.value = emptyList()
            } finally {
                _isLoadingPopularTopics.value = false
            }
        }
    }
    
    /**
     * Clear submit result
     */
    fun clearSubmitResult() {
        _submitResult.value = null
    }
}
