package com.wisme.firstapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisme.firstapp.data.repository.FeedbackRepository
import com.wisme.firstapp.data.repository.FeedbackResult
import com.wisme.firstapp.data.repository.FeedbackQuestion
import com.wisme.firstapp.data.repository.FeedbackResponse
import com.wisme.firstapp.ui.feedback.EpisodeFeedbackData
import com.wisme.firstapp.ui.feedback.JourneyFeedbackData
import com.wisme.firstapp.ui.feedback.GeneralFeedbackData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val questions: List<FeedbackQuestion> = emptyList(),
    val previousResponses: Map<String, String> = emptyMap()
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "FeedbackViewModel"
    }
    
    // Track episode completion states
    private val _hasCompletedEpisode = MutableStateFlow(false)
    val hasCompletedEpisode: StateFlow<Boolean> = _hasCompletedEpisode.asStateFlow()
    
    // Track journey completion states
    private val _hasCompletedJourney = MutableStateFlow(false)
    val hasCompletedJourney: StateFlow<Boolean> = _hasCompletedJourney.asStateFlow()
    
    // Track pending feedback requests
    private val _pendingEpisodeFeedback = MutableStateFlow<List<String>>(emptyList())
    val pendingEpisodeFeedback: StateFlow<List<String>> = _pendingEpisodeFeedback.asStateFlow()
    
    private val _pendingJourneyFeedback = MutableStateFlow<List<String>>(emptyList())
    val pendingJourneyFeedback: StateFlow<List<String>> = _pendingJourneyFeedback.asStateFlow()
    
    // Track feedback modal visibility
    private val _showEpisodeFeedbackModal = MutableStateFlow(false)
    val showEpisodeFeedbackModal: StateFlow<Boolean> = _showEpisodeFeedbackModal.asStateFlow()
    
    private val _showJourneyFeedbackModal = MutableStateFlow(false)
    val showJourneyFeedbackModal: StateFlow<Boolean> = _showJourneyFeedbackModal.asStateFlow()
    
    // Track completed episodes count to determine first episode
    private val _completedEpisodesCount = MutableStateFlow(0)
    val completedEpisodesCount: StateFlow<Int> = _completedEpisodesCount.asStateFlow()
    
    // Track current episode being processed for feedback
    private val _currentFeedbackEpisode = MutableStateFlow<String?>(null)
    val currentFeedbackEpisode: StateFlow<String?> = _currentFeedbackEpisode.asStateFlow()
    
    private val _currentFeedbackJourney = MutableStateFlow<String?>(null)
    val currentFeedbackJourney: StateFlow<String?> = _currentFeedbackJourney.asStateFlow()
    
    // Track feedback states for each type
    private val _episodeFeedbackState = MutableStateFlow(FeedbackState())
    val episodeFeedbackState: StateFlow<FeedbackState> = _episodeFeedbackState.asStateFlow()
    
    private val _journeyFeedbackState = MutableStateFlow(FeedbackState())
    val journeyFeedbackState: StateFlow<FeedbackState> = _journeyFeedbackState.asStateFlow()
    
    private val _generalFeedbackState = MutableStateFlow(FeedbackState())
    val generalFeedbackState: StateFlow<FeedbackState> = _generalFeedbackState.asStateFlow()
    
    // Track submitted feedback to control visibility
    private val _submittedEpisodeFeedbacks = MutableStateFlow<Set<String>>(emptySet())
    val submittedEpisodeFeedbacks: StateFlow<Set<String>> = _submittedEpisodeFeedbacks.asStateFlow()
    
    private val _submittedJourneyFeedbacks = MutableStateFlow<Set<String>>(emptySet())
    val submittedJourneyFeedbacks: StateFlow<Set<String>> = _submittedJourneyFeedbacks.asStateFlow()
    
    private val _hasSubmittedGeneralFeedback = MutableStateFlow(false)
    val hasSubmittedGeneralFeedback: StateFlow<Boolean> = _hasSubmittedGeneralFeedback.asStateFlow()
    
    // Convenience properties for UI access
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Previous responses for each feedback type
    private val _previousEpisodeResponses = MutableStateFlow<List<FeedbackResponse>?>(null)
    val previousEpisodeResponses: StateFlow<List<FeedbackResponse>?> = _previousEpisodeResponses.asStateFlow()
    
    private val _previousJourneyResponses = MutableStateFlow<List<FeedbackResponse>?>(null)
    val previousJourneyResponses: StateFlow<List<FeedbackResponse>?> = _previousJourneyResponses.asStateFlow()
    
    private val _previousGeneralResponses = MutableStateFlow<List<FeedbackResponse>?>(null)
    val previousGeneralResponses: StateFlow<List<FeedbackResponse>?> = _previousGeneralResponses.asStateFlow()
    
    init {
        loadFeedbackStates()
    }
    
    private fun loadFeedbackStates() {
        viewModelScope.launch {
            // Load submitted feedback to determine visibility
            loadSubmittedFeedbackHistory()
        }
    }
    
    /**
     * Load user's feedback submission history from backend
     */
    private suspend fun loadSubmittedFeedbackHistory() {
        when (val result = feedbackRepository.getMyFeedbackSubmissions()) {
            is FeedbackResult.Success -> {
                val episodeFeedbacks = mutableSetOf<String>()
                val journeyFeedbacks = mutableSetOf<String>()
                var hasGeneralFeedback = false
                
                result.data.submissions.forEach { submission ->
                    when (submission.feedback_type) {
                        "episode" -> episodeFeedbacks.add(submission.context_id)
                        "journey" -> journeyFeedbacks.add(submission.context_id)
                        "general" -> hasGeneralFeedback = true
                    }
                }
                
                _submittedEpisodeFeedbacks.value = episodeFeedbacks
                _submittedJourneyFeedbacks.value = journeyFeedbacks
                _hasSubmittedGeneralFeedback.value = hasGeneralFeedback
                
                Log.d(TAG, "Loaded feedback history: ${episodeFeedbacks.size} episodes, ${journeyFeedbacks.size} journeys, general: $hasGeneralFeedback")
            }
            is FeedbackResult.Error -> {
                Log.e(TAG, "Failed to load feedback history: ${result.message}")
            }
            else -> { /* Loading state */ }
        }
    }
    
    /**
     * Call this when an episode is completed
     * Episode feedback is triggered ONLY after the 1st episode completion
     */
    fun onEpisodeCompleted(episodeId: String) {
        viewModelScope.launch {
            // Increment completed episodes count
            val currentCount = _completedEpisodesCount.value
            _completedEpisodesCount.value = currentCount + 1
            
            _hasCompletedEpisode.value = true
            
            // Episode feedback is only triggered after the FIRST episode (count == 1)
            if (currentCount == 0) { // This will be the first episode completed
                // Add to pending feedback list
                val currentPending = _pendingEpisodeFeedback.value.toMutableList()
                if (!currentPending.contains(episodeId)) {
                    currentPending.add(episodeId)
                    _pendingEpisodeFeedback.value = currentPending
                }
                
                // Set current feedback episode and show modal
                _currentFeedbackEpisode.value = episodeId
                _showEpisodeFeedbackModal.value = true
            }
        }
    }
    
    /**
     * Call this when a journey is completed
     * Journey feedback is triggered after completing any full journey
     */
    fun onJourneyCompleted(journeyId: String, journeyName: String) {
        viewModelScope.launch {
            _hasCompletedJourney.value = true
            
            // Add to pending feedback list
            val currentPending = _pendingJourneyFeedback.value.toMutableList()
            if (!currentPending.contains(journeyId)) {
                currentPending.add(journeyId)
                _pendingJourneyFeedback.value = currentPending
            }
            
            // Set current feedback journey and show modal
            _currentFeedbackJourney.value = journeyName
            _showJourneyFeedbackModal.value = true
        }
    }
    
    /**
     * Dismiss episode feedback modal
     */
    fun dismissEpisodeFeedbackModal() {
        _showEpisodeFeedbackModal.value = false
    }
    
    /**
     * Dismiss journey feedback modal
     */
    fun dismissJourneyFeedbackModal() {
        _showJourneyFeedbackModal.value = false
    }
    
    /**
     * Mark episode feedback as completed
     */
    fun completeEpisodeFeedback(episodeId: String) {
        viewModelScope.launch {
            val currentPending = _pendingEpisodeFeedback.value.toMutableList()
            currentPending.remove(episodeId)
            _pendingEpisodeFeedback.value = currentPending
            
            // NOTE: Backend integration pending - feedback completion tracking
        }
    }
    
    /**
     * Mark journey feedback as completed
     */
    fun completeJourneyFeedback(journeyId: String) {
        viewModelScope.launch {
            val currentPending = _pendingJourneyFeedback.value.toMutableList()
            currentPending.remove(journeyId)
            _pendingJourneyFeedback.value = currentPending
            
            // NOTE: Backend integration pending - feedback completion tracking
        }
    }
    
    /**
     * Check if episode feedback is available for specific episode
     */
    fun isEpisodeFeedbackAvailable(episodeId: String): Boolean {
        return _pendingEpisodeFeedback.value.contains(episodeId)
    }
    
    /**
     * Check if journey feedback is available for specific journey
     */
    fun isJourneyFeedbackAvailable(journeyId: String): Boolean {
        return _pendingJourneyFeedback.value.contains(journeyId)
    }
    
    /**
     * Load feedback questions for episode feedback
     */
    fun loadEpisodeFeedbackQuestions() {
        viewModelScope.launch {
            _episodeFeedbackState.value = _episodeFeedbackState.value.copy(isLoading = true, error = null)
            
            when (val result = feedbackRepository.getFeedbackQuestions("episode")) {
                is FeedbackResult.Success -> {
                    _episodeFeedbackState.value = _episodeFeedbackState.value.copy(
                        isLoading = false,
                        questions = result.data
                    )
                }
                is FeedbackResult.Error -> {
                    _episodeFeedbackState.value = _episodeFeedbackState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Load previous responses for episode feedback
     */
    fun loadEpisodePreviousResponses(episodeId: String) {
        viewModelScope.launch {
            when (val result = feedbackRepository.getPreviousResponses("episode", episodeId)) {
                is FeedbackResult.Success -> {
                    val responseMap = result.data.associate { it.question_id to it.response_value }
                    _episodeFeedbackState.value = _episodeFeedbackState.value.copy(
                        previousResponses = responseMap
                    )
                }
                is FeedbackResult.Error -> {
                    Log.e(TAG, "Failed to load previous episode responses: ${result.message}")
                }
                else -> {}
            }
        }
    }

    /**
     * Submit episode feedback
     */
    fun submitEpisodeFeedback(episodeId: String, feedbackData: EpisodeFeedbackData) {
        viewModelScope.launch {
            _episodeFeedbackState.value = _episodeFeedbackState.value.copy(isLoading = true, error = null)
            
            when (val result = feedbackRepository.submitEpisodeFeedback(
                episodeId = episodeId,
                enjoyment = feedbackData.enjoyment,
                clarity = feedbackData.clarity
            )) {
                is FeedbackResult.Success -> {
                    // Add to submitted feedbacks list
                    val currentSubmitted = _submittedEpisodeFeedbacks.value.toMutableSet()
                    currentSubmitted.add(episodeId)
                    _submittedEpisodeFeedbacks.value = currentSubmitted
                    
                    // Remove from pending list
                    completeEpisodeFeedback(episodeId)
                    
                    // Clear current feedback episode
                    _currentFeedbackEpisode.value = null
                    
                    // Hide modal
                    dismissEpisodeFeedbackModal()
                    
                    _episodeFeedbackState.value = _episodeFeedbackState.value.copy(isLoading = false)
                    Log.d(TAG, "Successfully submitted episode feedback for $episodeId")
                }
                is FeedbackResult.Error -> {
                    _episodeFeedbackState.value = _episodeFeedbackState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    Log.e(TAG, "Failed to submit episode feedback: ${result.message}")
                }
                else -> {}
            }
        }
    }
    
    /**
     * Load feedback questions for journey feedback
     */
    fun loadJourneyFeedbackQuestions() {
        viewModelScope.launch {
            _journeyFeedbackState.value = _journeyFeedbackState.value.copy(isLoading = true, error = null)
            
            when (val result = feedbackRepository.getFeedbackQuestions("journey")) {
                is FeedbackResult.Success -> {
                    _journeyFeedbackState.value = _journeyFeedbackState.value.copy(
                        isLoading = false,
                        questions = result.data
                    )
                }
                is FeedbackResult.Error -> {
                    _journeyFeedbackState.value = _journeyFeedbackState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Load previous responses for journey feedback
     */
    fun loadJourneyPreviousResponses(journeyId: String) {
        viewModelScope.launch {
            when (val result = feedbackRepository.getPreviousResponses("journey", journeyId)) {
                is FeedbackResult.Success -> {
                    val responseMap = result.data.associate { it.question_id to it.response_value }
                    _journeyFeedbackState.value = _journeyFeedbackState.value.copy(
                        previousResponses = responseMap
                    )
                }
                is FeedbackResult.Error -> {
                    Log.e(TAG, "Failed to load previous journey responses: ${result.message}")
                }
                else -> {}
            }
        }
    }

    /**
     * Submit journey feedback
     */
    fun submitJourneyFeedback(journeyId: String, feedbackData: JourneyFeedbackData) {
        viewModelScope.launch {
            _journeyFeedbackState.value = _journeyFeedbackState.value.copy(isLoading = true, error = null)
            
            when (val result = feedbackRepository.submitJourneyFeedback(
                journeyId = journeyId,
                moocsComparison = feedbackData.moocsComparison,
                youtubeComparison = feedbackData.youtubeComparison,
                blogsComparison = feedbackData.blogsComparison
            )) {
                is FeedbackResult.Success -> {
                    // Add to submitted feedbacks list
                    val currentSubmitted = _submittedJourneyFeedbacks.value.toMutableSet()
                    currentSubmitted.add(journeyId)
                    _submittedJourneyFeedbacks.value = currentSubmitted
                    
                    // Remove from pending list
                    completeJourneyFeedback(journeyId)
                    
                    // Clear current feedback journey
                    _currentFeedbackJourney.value = null
                    
                    // Hide modal
                    dismissJourneyFeedbackModal()
                    
                    _journeyFeedbackState.value = _journeyFeedbackState.value.copy(isLoading = false)
                    Log.d(TAG, "Successfully submitted journey feedback for $journeyId")
                }
                is FeedbackResult.Error -> {
                    _journeyFeedbackState.value = _journeyFeedbackState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    Log.e(TAG, "Failed to submit journey feedback: ${result.message}")
                }
                else -> {}
            }
        }
    }
    
    /**
     * Load feedback questions for general feedback
     */
    fun loadGeneralFeedbackQuestions() {
        viewModelScope.launch {
            println("FeedbackViewModel: Loading general feedback questions with type 'general'")
            _generalFeedbackState.value = _generalFeedbackState.value.copy(isLoading = true, error = null)
            
            when (val result = feedbackRepository.getFeedbackQuestions("general")) {
                is FeedbackResult.Success -> {
                    _generalFeedbackState.value = _generalFeedbackState.value.copy(
                        isLoading = false,
                        questions = result.data
                    )
                }
                is FeedbackResult.Error -> {
                    _generalFeedbackState.value = _generalFeedbackState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Load previous responses for general feedback
     */
    fun loadGeneralPreviousResponses() {
        viewModelScope.launch {
            when (val result = feedbackRepository.getPreviousResponses("general", "general")) {
                is FeedbackResult.Success -> {
                    val responseMap = result.data.associate { it.question_id to it.response_value }
                    _generalFeedbackState.value = _generalFeedbackState.value.copy(
                        previousResponses = responseMap
                    )
                }
                is FeedbackResult.Error -> {
                    Log.e(TAG, "Failed to load previous general responses: ${result.message}")
                }
                else -> {}
            }
        }
    }

    /**
     * Submit general feedback
     */
    fun submitGeneralFeedback(feedbackData: GeneralFeedbackData) {
        viewModelScope.launch {
            _generalFeedbackState.value = _generalFeedbackState.value.copy(isLoading = true, error = null)
            
            when (val result = feedbackRepository.submitGeneralFeedback(
                wouldRevisit = feedbackData.wouldRevisit,
                wouldRecommend = feedbackData.wouldRecommend,
                willingnessToPay = feedbackData.willingnessToPay
            )) {
                is FeedbackResult.Success -> {
                    // Don't set _hasSubmittedGeneralFeedback to true - allow multiple submissions
                    _generalFeedbackState.value = _generalFeedbackState.value.copy(isLoading = false)
                    Log.d(TAG, "Successfully submitted general feedback - allowing multiple submissions")
                }
                is FeedbackResult.Error -> {
                    _generalFeedbackState.value = _generalFeedbackState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    Log.e(TAG, "Failed to submit general feedback: ${result.message}")
                }
                else -> {}
            }
        }
    }
    
    /**
     * Skip episode feedback
     */
    fun skipEpisodeFeedback() {
        viewModelScope.launch {
            _currentFeedbackEpisode.value = null
            dismissEpisodeFeedbackModal()
            // Keep in pending list so user can still access it later from feedback screen
        }
    }
    
    /**
     * Skip journey feedback
     */
    fun skipJourneyFeedback() {
        viewModelScope.launch {
            _currentFeedbackJourney.value = null
            dismissJourneyFeedbackModal()  
            // Keep in pending list so user can still access it later from feedback screen
        }
    }
    
    /**
     * Check if episode feedback should be visible on main feedback page
     * Only visible if there are pending feedbacks that haven't been submitted
     */
    fun shouldShowEpisodeFeedback(): Boolean {
        val pendingEpisodes = _pendingEpisodeFeedback.value
        val submittedEpisodes = _submittedEpisodeFeedbacks.value
        return pendingEpisodes.any { !submittedEpisodes.contains(it) }
    }
    
    /**
     * Check if journey feedback should be visible on main feedback page
     * Only visible if there are pending feedbacks that haven't been submitted
     */
    fun shouldShowJourneyFeedback(): Boolean {
        val pendingJourneys = _pendingJourneyFeedback.value
        val submittedJourneys = _submittedJourneyFeedbacks.value
        return pendingJourneys.any { !submittedJourneys.contains(it) }
    }
    
    /**
     * Get the first available episode that needs feedback
     */
    fun getAvailableEpisodeForFeedback(): String? {
        val pendingEpisodes = _pendingEpisodeFeedback.value
        val submittedEpisodes = _submittedEpisodeFeedbacks.value
        return pendingEpisodes.firstOrNull { !submittedEpisodes.contains(it) }
    }
    
    /**
     * Get the first available journey that needs feedback
     */
    fun getAvailableJourneyForFeedback(): String? {
        val pendingJourneys = _pendingJourneyFeedback.value
        val submittedJourneys = _submittedJourneyFeedbacks.value
        return pendingJourneys.firstOrNull { !submittedJourneys.contains(it) }
    }
    
    /**
     * Clear error states
     */
    fun clearEpisodeFeedbackError() {
        _episodeFeedbackState.value = _episodeFeedbackState.value.copy(error = null)
    }
    
    fun clearJourneyFeedbackError() {
        _journeyFeedbackState.value = _journeyFeedbackState.value.copy(error = null)
    }
    
    fun clearGeneralFeedbackError() {
        _generalFeedbackState.value = _generalFeedbackState.value.copy(error = null)
    }
    
    /**
     * Refresh feedback states
     */
    fun refreshFeedbackStates() {
        viewModelScope.launch {
            loadSubmittedFeedbackHistory()
        }
    }
    
    /**
     * Load previous episode responses
     */
    fun loadPreviousEpisodeResponses(episodeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            when (val result = feedbackRepository.getPreviousResponses("episode", episodeId)) {
                is FeedbackResult.Success -> {
                    _previousEpisodeResponses.value = result.data
                    _isLoading.value = false
                }
                is FeedbackResult.Error -> {
                    _errorMessage.value = result.message
                    _isLoading.value = false
                }
                else -> {
                    _isLoading.value = false
                }
            }
        }
    }
    
    /**
     * Load previous journey responses
     */
    fun loadPreviousJourneyResponses(journeyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            when (val result = feedbackRepository.getPreviousResponses("journey", journeyId)) {
                is FeedbackResult.Success -> {
                    _previousJourneyResponses.value = result.data
                    _isLoading.value = false
                }
                is FeedbackResult.Error -> {
                    _errorMessage.value = result.message
                    _isLoading.value = false
                }
                else -> {
                    _isLoading.value = false
                }
            }
        }
    }
    
    /**
     * Load previous general responses
     */
    fun loadPreviousGeneralResponses() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            when (val result = feedbackRepository.getPreviousResponses("general", "general")) {
                is FeedbackResult.Success -> {
                    _previousGeneralResponses.value = result.data
                    _isLoading.value = false
                }
                is FeedbackResult.Error -> {
                    _errorMessage.value = result.message
                    _isLoading.value = false
                }
                else -> {
                    _isLoading.value = false
                }
            }
        }
    }
    
    /**
     * Submit journey feedback with individual parameters
     */
    suspend fun submitJourneyFeedback(
        journeyId: String,
        moocsComparison: String,
        youtubeComparison: String,
        blogsComparison: String
    ): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            when (val result = feedbackRepository.submitJourneyFeedback(
                journeyId = journeyId,
                moocsComparison = moocsComparison,
                youtubeComparison = youtubeComparison,
                blogsComparison = blogsComparison
            )) {
                is FeedbackResult.Success -> {
                    _isLoading.value = false
                    // Update submitted journeys
                    val currentSubmitted = _submittedJourneyFeedbacks.value.toMutableSet()
                    currentSubmitted.add(journeyId)
                    _submittedJourneyFeedbacks.value = currentSubmitted
                    true
                }
                is FeedbackResult.Error -> {
                    _errorMessage.value = result.message
                    _isLoading.value = false
                    false
                }
                else -> {
                    _isLoading.value = false
                    false
                }
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Unknown error occurred"
            _isLoading.value = false
            false
        }
    }
    
    /**
     * Submit general feedback with individual parameters
     */
    suspend fun submitGeneralFeedback(
        wouldRevisit: String,
        wouldRecommend: String,
        willingnessToPay: String
    ): Boolean {
        println("FeedbackViewModel: submitGeneralFeedback called with wouldRevisit=$wouldRevisit, wouldRecommend=$wouldRecommend, willingnessToPay=$willingnessToPay")
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            println("FeedbackViewModel: Calling feedbackRepository.submitGeneralFeedback")
            when (val result = feedbackRepository.submitGeneralFeedback(
                wouldRevisit = wouldRevisit,
                wouldRecommend = wouldRecommend,
                willingnessToPay = willingnessToPay
            )) {
                is FeedbackResult.Success -> {
                    println("FeedbackViewModel: General feedback submission successful")
                    _isLoading.value = false
                    // Don't set _hasSubmittedGeneralFeedback to true - allow multiple submissions
                    true
                }
                is FeedbackResult.Error -> {
                    println("FeedbackViewModel: General feedback submission failed: ${result.message}")
                    _errorMessage.value = result.message
                    _isLoading.value = false
                    false
                }
                else -> {
                    println("FeedbackViewModel: General feedback submission returned unknown result")
                    _isLoading.value = false
                    false
                }
            }
        } catch (e: Exception) {
            println("FeedbackViewModel: Exception during general feedback submission: ${e.message}")
            _errorMessage.value = e.message ?: "Unknown error occurred"
            _isLoading.value = false
            false
        }
    }
}
