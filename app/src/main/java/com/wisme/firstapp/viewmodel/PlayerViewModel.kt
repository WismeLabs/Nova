package com.wisme.firstapp.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.wisme.firstapp.R
import com.wisme.firstapp.data.repository.JourneyRepository
import com.wisme.firstapp.domain.EpisodeProgress
import com.wisme.firstapp.util.Logger
import com.wisme.firstapp.utils.PlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application, 
    private val journeyRepository: JourneyRepository
) : AndroidViewModel(application) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration = _totalDuration.asStateFlow()

    // Progress tracking state
    private val _progressPercentage = MutableStateFlow(0f)
    val progressPercentage = _progressPercentage.asStateFlow()

    private val _episodeProgress = MutableStateFlow<EpisodeProgress?>(null)
    val episodeProgress = _episodeProgress.asStateFlow()

    // Current episode tracking
    private var currentJourneyId: String? = null
    private var currentEpisodeId: String? = null

    private var positionJob: Job? = null
    private var progressSyncJob: Job? = null
    
    // Media3 MediaController for actual audio playback
    private var mediaController: MediaController? = null
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null

    init {
        Logger.d("PlayerViewModel: Initializing with MediaController", "PLAYER_VM")
        println("PlayerViewModel: Setting up MediaController connection")
        initializeMediaController()
    }
    
    private fun initializeMediaController() {
        try {
            val context = getApplication<Application>()
            val sessionToken = SessionToken(
                context,
                ComponentName(context, PlayerService::class.java)
            )
            
            mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            mediaControllerFuture?.addListener({
                try {
                    mediaController = mediaControllerFuture?.get()
                    setupPlayerListener()
                    println("PlayerViewModel: MediaController connected successfully")
                } catch (e: Exception) {
                    println("PlayerViewModel: Failed to connect MediaController: ${e.message}")
                    Logger.e("PlayerViewModel: MediaController connection failed - ${e.message}", "PLAYER_VM")
                }
            }, { /* executor */ })
        } catch (e: Exception) {
            println("PlayerViewModel: Error initializing MediaController: ${e.message}")
            Logger.e("PlayerViewModel: Error initializing MediaController - ${e.message}", "PLAYER_VM")
        }
    }
    
    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        val duration = mediaController?.duration ?: 0L
                        if (duration > 0) {
                            _totalDuration.value = duration
                            println("PlayerViewModel: Audio duration set to ${duration}ms (${duration/60000} minutes)")
                        }
                    }
                    Player.STATE_ENDED -> {
                        _isPlaying.value = false
                        forceSyncProgress()
                    }
                }
            }
        })
    }

    fun playPause() {
        try {
            val controller = mediaController
            if (controller != null) {
                // Use actual MediaController
                if (controller.isPlaying) {
                    controller.pause()
                    println("PlayerViewModel: Pausing audio playback")
                } else {
                    controller.play()
                    println("PlayerViewModel: Starting audio playback")
                }
            } else {
                // Fallback to simulated playback if MediaController not ready
                println("PlayerViewModel: MediaController not ready, using simulated playback")
                _isPlaying.value = !_isPlaying.value
                
                if (_isPlaying.value) {
                    Logger.d("PlayerViewModel: Starting simulated playback", "PLAYER_VM")
                    startPositionUpdates()
                } else {
                    Logger.d("PlayerViewModel: Pausing simulated playback", "PLAYER_VM")
                    stopPositionUpdates()
                    forceSyncProgress()
                }
            }
        } catch (e: Exception) {
            Logger.e("PlayerViewModel: Error in playPause - ${e.message}", "PLAYER_VM")
            e.printStackTrace()
        }
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionJob = viewModelScope.launch {
            while (_isPlaying.value) {
                val controller = mediaController
                if (controller != null) {
                    // Get actual position from MediaController
                    val currentPos = controller.currentPosition
                    val totalDur = controller.duration.takeIf { it > 0 } ?: _totalDuration.value
                    
                    _currentPosition.value = currentPos
                    if (totalDur != _totalDuration.value) {
                        _totalDuration.value = totalDur
                    }
                    
                    // Calculate and update progress percentage
                    if (totalDur > 0) {
                        val progress = (currentPos.toFloat() / totalDur.toFloat()) * 100f
                        _progressPercentage.value = progress.coerceIn(0f, 100f)
                    }
                } else {
                    // Fallback to simulated progress
                    val currentPos = _currentPosition.value + 1000L // Advance by 1 second
                    val totalDur = _totalDuration.value
                    
                    _currentPosition.value = currentPos.coerceAtMost(totalDur)
                    
                    // Calculate and update progress percentage
                    if (totalDur > 0) {
                        val progress = (currentPos.toFloat() / totalDur.toFloat()) * 100f
                        _progressPercentage.value = progress.coerceIn(0f, 100f)
                    }
                    
                    // Stop when reaching the end
                    if (currentPos >= totalDur) {
                        _isPlaying.value = false
                        break
                    }
                }
                
                delay(1000) // Update every second
            }
        }
        
        // Start progress sync job for backend updates
        startProgressSync()
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
        stopProgressSync()
    }

    fun seekForward() {
        try {
            val newPosition = (_currentPosition.value + 10000).coerceAtMost(_totalDuration.value)
            _currentPosition.value = newPosition // Immediately update UI
            
            // Update progress percentage
            val totalDur = _totalDuration.value
            if (totalDur > 0) {
                _progressPercentage.value = (newPosition.toFloat() / totalDur.toFloat()) * 100f
            }
            
            // Sync progress after seeking
            forceSyncProgress()
            Logger.d("PlayerViewModel: Seeked forward 10 seconds to ${newPosition}ms", "PLAYER_VM")
        } catch (e: Exception) {
            Logger.e("PlayerViewModel: Error in seekForward - ${e.message}", "PLAYER_VM")
        }
    }

    fun seekBack() {
        try {
            val newPosition = (_currentPosition.value - 10000).coerceAtLeast(0)
            _currentPosition.value = newPosition // Immediately update UI
            
            // Update progress percentage
            val totalDur = _totalDuration.value
            if (totalDur > 0) {
                _progressPercentage.value = (newPosition.toFloat() / totalDur.toFloat()) * 100f
            }
            
            // Sync progress after seeking
            forceSyncProgress()
            Logger.d("PlayerViewModel: Seeked back 10 seconds to ${newPosition}ms", "PLAYER_VM")
        } catch (e: Exception) {
            Logger.e("PlayerViewModel: Error in seekBack - ${e.message}", "PLAYER_VM")
        }
    }

    fun seekTo(position: Float) {
        try {
            val newPosition = (position * (_totalDuration.value)).toLong()
            _currentPosition.value = newPosition // Immediately update UI for better UX
            _progressPercentage.value = position * 100f
            
            // Sync progress immediately on seek
            syncProgressToRepository()
            Logger.d("PlayerViewModel: Seeked to ${newPosition}ms (${position * 100}%)", "PLAYER_VM")
        } catch (e: Exception) {
            Logger.e("PlayerViewModel: Error in seekTo - ${e.message}", "PLAYER_VM")
        }
    }

    /**
     * Set the current episode being played and load audio
     */
    fun setCurrentEpisode(journeyId: String, episodeId: String, audioUrl: String? = null, durationMinutes: Int = 0) {
        currentJourneyId = journeyId
        currentEpisodeId = episodeId
        
        Logger.d("PlayerViewModel - Setting current episode: $journeyId/$episodeId", "PLAYER_VM")
        println("PlayerViewModel: Setting episode with audioUrl: $audioUrl, duration: $durationMinutes minutes")
        
        // Reset playback state when switching episodes
        _isPlaying.value = false
        _currentPosition.value = 0L
        _progressPercentage.value = 0f
        stopPositionUpdates()
        
        // Set duration from episode data if available
        if (durationMinutes > 0) {
            _totalDuration.value = durationMinutes * 60 * 1000L // Convert minutes to milliseconds
            println("PlayerViewModel: Set duration to ${durationMinutes} minutes (${_totalDuration.value}ms)")
        } else {
            // Fallback to default duration
            _totalDuration.value = 300000L // 5 minutes default
            println("PlayerViewModel: Using default duration (5 minutes)")
        }
        
        // Load actual audio file if audioUrl is provided
        if (!audioUrl.isNullOrEmpty()) {
            println("PlayerViewModel: Loading audio URL: $audioUrl")
            loadAudioFile(audioUrl)
        } else {
            // For testing: Use demo audio URLs
            val demoAudioUrl = getDemoAudioUrl(currentJourneyId, currentEpisodeId)
            if (demoAudioUrl != null) {
                println("PlayerViewModel: Using demo audio URL: $demoAudioUrl")
                loadAudioFile(demoAudioUrl)
            } else {
                println("PlayerViewModel: No audio URL provided, using simulated playback")
            }
        }
        
        // Load existing progress for this episode
        loadEpisodeProgress()
    }
    
    private fun getDemoAudioUrl(journeyId: String?, episodeId: String?): String? {
        // For testing purposes, use publicly available demo audio files
        // Replace with actual backend URLs when available
        return when {
            journeyId?.contains("DSA", ignoreCase = true) == true -> 
                "https://www.soundjay.com/misc/sounds/sound2.mp3" // Short test audio
            journeyId?.contains("Personal Finance", ignoreCase = true) == true ->
                "https://www.soundjay.com/misc/sounds/sound3.mp3" // Different test audio
            journeyId?.contains("Hackathon", ignoreCase = true) == true ->
                "https://www.soundjay.com/misc/sounds/sound4.mp3" // Another test audio
            else -> 
                "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3" // Generic demo audio
        }
    }
    
    private fun loadAudioFile(audioUrl: String) {
        try {
            val controller = mediaController
            if (controller != null) {
                println("PlayerViewModel: Creating MediaItem for URL: $audioUrl")
                val mediaItem = MediaItem.fromUri(Uri.parse(audioUrl))
                controller.setMediaItem(mediaItem)
                controller.prepare()
                println("PlayerViewModel: Audio file loaded and prepared")
            } else {
                println("PlayerViewModel: MediaController not available for loading audio")
            }
        } catch (e: Exception) {
            println("PlayerViewModel: Error loading audio file: ${e.message}")
            Logger.e("PlayerViewModel: Error loading audio - ${e.message}", "PLAYER_VM")
        }
    }

    /**
     * Load existing progress for the current episode
     */
    private fun loadEpisodeProgress() {
        val journeyId = currentJourneyId
        val episodeId = currentEpisodeId
        
        if (journeyId != null && episodeId != null && journeyRepository != null) {
            viewModelScope.launch {
                try {
                    // Get existing progress from repository (via AuthPreferences)
                    // For now, we'll create a basic progress object
                    val progress = EpisodeProgress(
                        progressPercentage = _progressPercentage.value,
                        playPositionSeconds = (_currentPosition.value / 1000),
                        status = EpisodeProgress.STATUS_IN_PROGRESS,
                        lastUpdated = System.currentTimeMillis()
                    )
                    _episodeProgress.value = progress
                    
                    Logger.d("PlayerViewModel - Loaded episode progress: ${progress.progressPercentage}%", "PLAYER_VM")
                } catch (e: Exception) {
                    Logger.e("PlayerViewModel - Failed to load episode progress: ${e.message}", "PLAYER_VM")
                }
            }
        }
    }

    /**
     * Start progress sync job for periodic backend updates
     */
    private fun startProgressSync() {
        stopProgressSync()
        progressSyncJob = viewModelScope.launch {
            while (_isPlaying.value) {
                delay(5000) // Sync every 5 seconds while playing
                syncProgressToRepository()
            }
        }
    }

    /**
     * Stop progress sync job
     */
    private fun stopProgressSync() {
        progressSyncJob?.cancel()
        progressSyncJob = null
    }

    /**
     * Sync current progress to repository
     */
    private fun syncProgressToRepository() {
        val journeyId = currentJourneyId
        val episodeId = currentEpisodeId
        
        if (journeyId != null && episodeId != null && journeyRepository != null) {
            viewModelScope.launch {
                try {
                    val currentPos = _currentPosition.value
                    val progress = _progressPercentage.value
                    
                    // Update progress in repository
                    // Note: This will require the JourneyViewModel to call this method
                    // as PlayerViewModel doesn't have direct access to authentication
                    
                    Logger.d("PlayerViewModel - Syncing progress: ${progress}% at ${currentPos/1000}s", "PLAYER_VM")
                    
                    // Update local episode progress
                    val updatedProgress = EpisodeProgress(
                        progressPercentage = progress,
                        playPositionSeconds = (currentPos / 1000),
                        status = if (progress >= EpisodeProgress.COMPLETION_THRESHOLD) {
                            EpisodeProgress.STATUS_COMPLETED
                        } else {
                            EpisodeProgress.STATUS_IN_PROGRESS
                        },
                        lastUpdated = System.currentTimeMillis()
                    )
                    _episodeProgress.value = updatedProgress
                    
                } catch (e: Exception) {
                    Logger.e("PlayerViewModel - Failed to sync progress: ${e.message}", "PLAYER_VM")
                }
            }
        }
    }

    /**
     * Get current episode progress for external access
     */
    fun getCurrentEpisodeProgress(): Triple<String?, String?, Float>? {
        return if (currentJourneyId != null && currentEpisodeId != null) {
            Triple(currentJourneyId!!, currentEpisodeId!!, _progressPercentage.value)
        } else null
    }

    /**
     * Force sync progress (called when pausing, seeking, etc.)
     */
    fun forceSyncProgress() {
        syncProgressToRepository()
    }

    override fun onCleared() {
        super.onCleared()
        stopPositionUpdates()
        // Final progress sync before clearing
        syncProgressToRepository()
        
        // Release MediaController
        try {
            mediaControllerFuture?.let { MediaController.releaseFuture(it) }
            mediaController = null
            println("PlayerViewModel: MediaController released")
        } catch (e: Exception) {
            println("PlayerViewModel: Error releasing MediaController: ${e.message}")
        }
        
        Logger.d("PlayerViewModel: Cleared and synced final progress", "PLAYER_VM")
    }
}
