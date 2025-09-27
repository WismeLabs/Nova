package com.wisme.firstapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisme.firstapp.data.repository.JourneyRepository
import com.wisme.firstapp.data.repository.ConnectivityRepository
import com.wisme.firstapp.data.local.AuthPreferences
import com.wisme.firstapp.domain.JourneysDataClass
import com.wisme.firstapp.domain.EpisodeDataClass
import com.wisme.firstapp.domain.EpisodeProgress
import com.wisme.firstapp.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JourneyViewModel @Inject constructor(
    private val journeyRepository: JourneyRepository,
    private val authPrefs: AuthPreferences,
    private val connectivityRepository: ConnectivityRepository
) : ViewModel() {

    private val _journeys = MutableStateFlow<List<JourneysDataClass>>(emptyList())
    val journeys: StateFlow<List<JourneysDataClass>> = _journeys.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedJourney = MutableStateFlow<JourneysDataClass?>(null)
    val selectedJourney: StateFlow<JourneysDataClass?> = _selectedJourney.asStateFlow()

    // Progress tracking state
    private val _continueLearning = MutableStateFlow<Pair<String, String>?>(null)
    val continueLearning: StateFlow<Pair<String, String>?> = _continueLearning.asStateFlow()

    private val _hasStartedAnyEpisode = MutableStateFlow(false)
    val hasStartedAnyEpisode: StateFlow<Boolean> = _hasStartedAnyEpisode.asStateFlow()

    private val _episodeProgress = MutableStateFlow<Map<String, EpisodeProgress>>(emptyMap())
    val episodeProgress: StateFlow<Map<String, EpisodeProgress>> = _episodeProgress.asStateFlow()

    init {
        loadJourneys()
        loadProgressState()
    }

    /**
     * Load all available journeys from the API
     */
    fun loadJourneys() {
        Logger.logViewModel("JourneyViewModel", "loadJourneys - Starting journey loading")
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val token = authPrefs.firebaseToken
            if (token.isNullOrEmpty()) {
                val errorMessage = "Authentication token not found"
                _errorMessage.value = errorMessage
                Logger.logViewModel(
                    viewModel = "JourneyViewModel",
                    operation = "loadJourneys",
                    success = false,
                    errorMessage = errorMessage
                )
                _isLoading.value = false
                return@launch
            }

            Logger.d("Loading journeys with token authentication", "JOURNEY_VM")

            journeyRepository.getAllJourneys(token)
                .onSuccess { journeysList ->
                    _journeys.value = journeysList
                    Logger.logJourney(
                        operation = "Load Journeys",
                        success = true
                    )
                    Logger.logViewModel(
                        viewModel = "JourneyViewModel",
                        operation = "loadJourneys",
                        success = true
                    )
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "Failed to load journeys"
                    _errorMessage.value = errorMessage
                    Logger.logJourney(
                        operation = "Load Journeys",
                        success = false,
                        errorMessage = errorMessage
                    )
                    Logger.logViewModel(
                        viewModel = "JourneyViewModel",
                        operation = "loadJourneys",
                        success = false,
                        errorMessage = errorMessage
                    )
                }

            _isLoading.value = false
        }
    }

    /**
     * Load detailed journey with episodes
     */
    fun loadJourneyDetails(journeyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val token = authPrefs.firebaseToken
            if (token.isNullOrEmpty()) {
                _errorMessage.value = "Authentication token not found"
                _isLoading.value = false
                return@launch
            }

            journeyRepository.getJourneyWithEpisodes(token, journeyId)
                .onSuccess { journey ->
                    _selectedJourney.value = journey
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load journey details"
                }

            _isLoading.value = false
        }
    }

    /**
     * Retry loading journeys
     */
    fun retryLoadJourneys() {
        loadJourneys()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Load progress state from preferences
     */
    private fun loadProgressState() {
        viewModelScope.launch {
            try {
                // Load continue learning data
                journeyRepository.getContinueLearning()
                    .onSuccess { continueLearningData ->
                        _continueLearning.value = continueLearningData
                    }
                    .onFailure { exception ->
                        Logger.logViewModel(
                            viewModel = "JourneyViewModel",
                            operation = "loadProgressState",
                            success = false,
                            errorMessage = "Failed to load continue learning: ${exception.message}"
                        )
                    }

                // Check if user has started any episode
                _hasStartedAnyEpisode.value = authPrefs.hasStartedAnyEpisode

                // Load all episode progress
                val allProgress = authPrefs.getAllEpisodeProgress()
                _episodeProgress.value = allProgress

                Logger.logViewModel(
                    viewModel = "JourneyViewModel",
                    operation = "loadProgressState",
                    success = true
                )
            } catch (e: Exception) {
                Logger.logViewModel(
                    viewModel = "JourneyViewModel",
                    operation = "loadProgressState",
                    success = false,
                    errorMessage = "Exception: ${e.message}"
                )
            }
        }
    }

    /**
     * Start an episode and register progress tracking
     */
    fun startEpisode(journeyId: String, episodeId: String) {
        Logger.logViewModel(
            viewModel = "JourneyViewModel",
            operation = "startEpisode - Starting episode: $journeyId/$episodeId"
        )
        
        viewModelScope.launch {
            try {
                val token = authPrefs.firebaseToken
                if (token.isNullOrEmpty()) {
                    Logger.logViewModel(
                        viewModel = "JourneyViewModel",
                        operation = "startEpisode",
                        success = false,
                        errorMessage = "Authentication token not found"
                    )
                    return@launch
                }
                
                journeyRepository.startEpisode(token, journeyId, episodeId)
                    .onSuccess {
                        // Update local state
                        _hasStartedAnyEpisode.value = true
                        _continueLearning.value = Pair(journeyId, episodeId)
                        
                        // Refresh progress state
                        loadProgressState()
                        
                        Logger.logViewModel(
                            viewModel = "JourneyViewModel",
                            operation = "startEpisode",
                            success = true
                        )
                    }
                    .onFailure { exception ->
                        Logger.logViewModel(
                            viewModel = "JourneyViewModel",
                            operation = "startEpisode",
                            success = false,
                            errorMessage = "Failed to start episode: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                Logger.logViewModel(
                    viewModel = "JourneyViewModel",
                    operation = "startEpisode",
                    success = false,
                    errorMessage = "Exception: ${e.message}"
                )
            }
        }
    }

    /**
     * Update episode progress
     */
    fun updateEpisodeProgress(
        journeyId: String,
        episodeId: String,
        progressPercentage: Float,
        playPositionSeconds: Long
    ) {
        viewModelScope.launch {
            try {
                val token = authPrefs.firebaseToken
                if (token.isNullOrEmpty()) {
                    Logger.logViewModel(
                        viewModel = "JourneyViewModel",
                        operation = "updateEpisodeProgress",
                        success = false,
                        errorMessage = "Authentication token not found"
                    )
                    return@launch
                }
                
                journeyRepository.updateEpisodeProgress(
                    token = token,
                    journeyId = journeyId,
                    episodeId = episodeId,
                    progressPercentage = progressPercentage,
                    playPositionSeconds = playPositionSeconds
                ).onSuccess { progress ->
                    // Update local state
                    val currentProgress = _episodeProgress.value.toMutableMap()
                    val key = "${journeyId}_${episodeId}"
                    currentProgress[key] = progress
                    _episodeProgress.value = currentProgress
                    
                    // Update continue learning if this is the latest played
                    _continueLearning.value = Pair(journeyId, episodeId)
                    
                    Logger.logViewModel(
                        viewModel = "JourneyViewModel",
                        operation = "updateEpisodeProgress",
                        success = true
                    )
                }.onFailure { exception ->
                    Logger.logViewModel(
                        viewModel = "JourneyViewModel",
                        operation = "updateEpisodeProgress",
                        success = false,
                        errorMessage = "Failed to update progress: ${exception.message}"
                    )
                }
            } catch (e: Exception) {
                Logger.logViewModel(
                    viewModel = "JourneyViewModel",
                    operation = "updateEpisodeProgress",
                    success = false,
                    errorMessage = "Exception: ${e.message}"
                )
            }
        }
    }

    /**
     * Get episode progress for a specific episode
     */
    fun getEpisodeProgress(journeyId: String, episodeId: String): EpisodeProgress? {
        val key = "${journeyId}_${episodeId}"
        return _episodeProgress.value[key]
    }

    /**
     * Calculate completion rate for a journey
     */
    fun getJourneyCompletionRate(journeyId: String): Float {
        val journey = _selectedJourney.value
        if (journey == null || journey.episodes.isNullOrEmpty()) return 0f
        
        val totalEpisodes = journey.episodes.size
        var completedEpisodes = 0
        
        journey.episodes.forEach { episode ->
            val episodeId = "episode_${episode.episodeNumber}"
            val progress = getEpisodeProgress(journeyId, episodeId)
            if (progress?.isCompleted == true) {
                completedEpisodes++
            }
        }
        
        return if (totalEpisodes > 0) (completedEpisodes.toFloat() / totalEpisodes) * 100f else 0f
    }

    /**
     * Refresh continue learning data
     */
    fun refreshContinueLearning() {
        viewModelScope.launch {
            journeyRepository.getContinueLearning()
                .onSuccess { continueLearningData ->
                    _continueLearning.value = continueLearningData
                }
                .onFailure { exception ->
                    Logger.logViewModel(
                        viewModel = "JourneyViewModel",
                        operation = "refreshContinueLearning",
                        success = false,
                        errorMessage = "Failed to refresh continue learning: ${exception.message}"
                    )
                }
        }
    }


}