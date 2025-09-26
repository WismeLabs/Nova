package com.wisme.firstapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisme.firstapp.data.repository.JourneyRepository
import com.wisme.firstapp.data.repository.ConnectivityRepository
import com.wisme.firstapp.data.local.AuthPreferences
import com.wisme.firstapp.domain.JourneysDataClass
import com.wisme.firstapp.domain.EpisodeDataClass
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

    init {
        loadJourneys()
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
                    // Load sample data as fallback
                    Logger.d("Loading sample journeys as fallback", "JOURNEY_VM")
                    loadSampleJourneys()
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
     * Load sample journey data as fallback based on the markdown content
     */
    private fun loadSampleJourneys() {
        val sampleJourneys = listOf(
            JourneysDataClass(
                JourneyName = "DSA / Cracking Coding Interviews",
                JourneyDescription = "Help students crack coding interviews, from basics to advanced topics. Master arrays, recursion, dynamic programming and interview strategies.",
                JourneyImg = "journey1", // This will be replaced with actual image URLs from backend
                totalDurationMinutes = 0, // Will be calculated dynamically from audio files
                episodes = listOf(
                    EpisodeDataClass(1, "Why companies ask DSA questions", "Understanding the reasoning behind algorithmic interviews", 0), // Duration from audio file
                    EpisodeDataClass(2, "Arrays & Strings refresher – common pitfalls", "Master the most common data structures and avoid typical mistakes", 0), // Duration from audio file
                    EpisodeDataClass(3, "Recursion & Backtracking basics", "Build intuition for recursive problem solving", 0), // Duration from audio file
                    EpisodeDataClass(4, "Dynamic Programming (Intro + patterns)", "Learn the most important optimization technique", 0), // Duration from audio file
                    EpisodeDataClass(5, "Tricks to approach any coding problem", "Develop systematic problem-solving strategies", 0), // Duration from audio file
                    EpisodeDataClass(6, "Mock interview mindset & time management", "Ace the interview with the right mental approach", 0) // Duration from audio file
                )
            ),
            JourneysDataClass(
                JourneyName = "Personal Finance",
                JourneyDescription = "Teach foundational personal finance and modern investment trends. From budgeting basics to crypto and tax planning.",
                JourneyImg = "journey2",
                totalDurationMinutes = 0, // Will be calculated dynamically from audio files
                episodes = listOf(
                    EpisodeDataClass(1, "Budgeting & Saving basics", "Foundation of financial health", 0), // Duration from audio file
                    EpisodeDataClass(2, "Emergency funds & debt management", "Building financial security", 0), // Duration from audio file
                    EpisodeDataClass(3, "Understanding credit scores & loans", "Navigate the credit system", 0), // Duration from audio file
                    EpisodeDataClass(4, "Investing 101 – stocks, mutual funds, ETFs", "Start your investment journey", 0), // Duration from audio file
                    EpisodeDataClass(5, "Trends & alternatives – crypto, fractional investing, SIP automation", "Modern investment strategies", 0), // Duration from audio file
                    EpisodeDataClass(6, "Tax planning basics & common mistakes", "Optimize your tax strategy", 0), // Duration from audio file
                    EpisodeDataClass(7, "Actionable plan – first 3 months of financial health", "Your roadmap to financial success", 0) // Duration from audio file
                )
            ),
            JourneysDataClass(
                JourneyName = "How to win Hackathons",
                JourneyDescription = "Equip students with proven strategies to consistently perform and win hackathons. From team building to pitching.",
                JourneyImg = "journey3",
                totalDurationMinutes = 0, // Will be calculated dynamically from audio files
                episodes = listOf(
                    EpisodeDataClass(1, "The Winning Mindset", "Develop the psychology of hackathon winners", 0), // Duration from audio file
                    EpisodeDataClass(2, "Choosing the Right Problem", "Select problems that judge well", 0), // Duration from audio file
                    EpisodeDataClass(3, "Team Building & Role Clarity", "Assemble and organize your dream team", 0), // Duration from audio file
                    EpisodeDataClass(4, "Execution Strategy – MVP, Tools & Time Management", "Build efficiently under pressure", 0), // Duration from audio file
                    EpisodeDataClass(5, "Pitching & Presentation to Judges", "Sell your solution effectively", 0), // Duration from audio file
                    EpisodeDataClass(6, "Common Mistakes & How to Avoid Them", "Learn from others' failures", 0), // Duration from audio file
                    EpisodeDataClass(7, "Action Plan – Preparing for Your Next Hackathon", "Your comprehensive preparation guide", 0) // Duration from audio file
                )
            )
        )
        _journeys.value = sampleJourneys
    }
}