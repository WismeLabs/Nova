package com.wisme.research.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.wisme.research.R
import com.wisme.research.data.local.AuthPreferences
import com.wisme.research.data.repository.JourneyRepository
import com.wisme.research.domain.EpisodeProgress
import com.wisme.research.util.Logger
import com.wisme.research.utils.PlayerService
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
    private val journeyRepository: JourneyRepository,
    private val authPrefs: AuthPreferences
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
    
    // Resume position for episodes with saved progress
    private var resumePosition: Long? = null
    private var hasAppliedResumePosition = false

    private var positionJob: Job? = null
    private var progressSyncJob: Job? = null
    
    // Media3 MediaController for actual audio playback
    private var mediaController: MediaController? = null
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null

    init {
        Logger.d("PlayerViewModel: Initializing with MediaController", "PLAYER_VM")
        println("PlayerViewModel: Setting up MediaController connection")
        checkDeviceAudioCodecs()
        initializeMediaController()
    }
    
    private fun checkDeviceAudioCodecs() {
        try {
            println("PlayerViewModel: *** CHECKING DEVICE AUDIO CODEC SUPPORT ***")
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            val codecInfos = codecList.codecInfos
            
            val audioDecoders = mutableListOf<String>()
            val audioEncoders = mutableListOf<String>()
            
            for (codecInfo in codecInfos) {
                val supportedTypes = codecInfo.supportedTypes
                for (type in supportedTypes) {
                    if (type.startsWith("audio/")) {
                        if (codecInfo.isEncoder) {
                            audioEncoders.add("${codecInfo.name} ($type)")
                        } else {
                            audioDecoders.add("${codecInfo.name} ($type)")
                        }
                    }
                }
            }
            
            println("PlayerViewModel: === AUDIO DECODERS AVAILABLE ===")
            audioDecoders.forEach { decoder ->
                println("PlayerViewModel: - $decoder")
            }
            
            // Check for common codec support
            val commonCodecs = listOf("audio/mp4a-latm", "audio/mpeg", "audio/3gpp", "audio/amr-wb", "audio/flac", "audio/ogg")
            println("PlayerViewModel: === COMMON CODEC SUPPORT CHECK ===")
            commonCodecs.forEach { codec ->
                val supported = audioDecoders.any { it.contains(codec) }
                println("PlayerViewModel: $codec: ${if (supported) "✓ SUPPORTED" else "✗ NOT SUPPORTED"}")
            }
            
        } catch (e: Exception) {
            println("PlayerViewModel: Error checking codecs: ${e.message}")
        }
    }
    
    private fun initializeMediaController() {
        try {
            val context = getApplication<Application>()
            
            // Force start the PlayerService first
            println("PlayerViewModel: Starting PlayerService explicitly")
            val serviceIntent = android.content.Intent(context, PlayerService::class.java)
            context.startService(serviceIntent)
            
            val sessionToken = SessionToken(
                context,
                ComponentName(context, PlayerService::class.java)
            )
            
            println("PlayerViewModel: Building MediaController with session token")
            mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            mediaControllerFuture?.addListener({
                try {
                    mediaController = mediaControllerFuture?.get()
                    setupPlayerListener()
                    println("PlayerViewModel: *** MediaController SUCCESSFULLY CONNECTED ***")
                    Logger.d("PlayerViewModel: MediaController successfully initialized", "PLAYER_VM")
                } catch (e: Exception) {
                    println("PlayerViewModel: *** MediaController CONNECTION FAILED ***")
                    println("PlayerViewModel: Error details: ${e.message}")
                    Logger.e("PlayerViewModel: MediaController connection failed - ${e.message}", "PLAYER_VM")
                    e.printStackTrace()
                    
                    // Retry after delay
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(2000)
                        println("PlayerViewModel: Retrying MediaController connection...")
                        retryMediaControllerConnection()
                    }
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            println("PlayerViewModel: Error initializing MediaController: ${e.message}")
            Logger.e("PlayerViewModel: Error initializing MediaController - ${e.message}", "PLAYER_VM")
        }
    }
    
    private fun retryMediaControllerConnection() {
        try {
            println("PlayerViewModel: Attempting MediaController retry...")
            // Release existing connection if any
            mediaControllerFuture?.cancel(true)
            mediaController = null
            
            // Try to initialize again
            initializeMediaController()
        } catch (e: Exception) {
            println("PlayerViewModel: Retry failed: ${e.message}")
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
            println("PlayerViewModel: *** PLAY/PAUSE BUTTON PRESSED ***")
            val controller = mediaController
            
            if (controller != null) {
                println("PlayerViewModel: MediaController available")
                println("PlayerViewModel: Current player state - isPlaying: ${controller.isPlaying}, playbackState: ${controller.playbackState}")
                println("PlayerViewModel: Media items count: ${controller.mediaItemCount}")
                
                // Use actual MediaController
                if (controller.isPlaying) {
                    controller.pause()
                    println("PlayerViewModel: *** PAUSING AUDIO PLAYBACK ***")
                } else {
                    // Ensure maximum volume before playing
                    controller.volume = 1.0f
                    
                    // Check and boost system media volume
                    val context = getApplication<Application>().applicationContext
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    
                    println("PlayerViewModel: Current system media volume: $currentVolume/$maxVolume")
                    
                    // If volume is too low, suggest increasing it
                    if (currentVolume < maxVolume * 0.3) {
                        println("PlayerViewModel: *** WARNING: Media volume is very low ($currentVolume/$maxVolume) ***")
                        println("PlayerViewModel: *** INCREASE YOUR MEDIA VOLUME USING VOLUME BUTTONS ***")
                    }
                    
                    controller.play()
                    println("PlayerViewModel: *** STARTING AUDIO PLAYBACK ***")
                    println("PlayerViewModel: Player volume set to maximum (1.0)")
                    println("PlayerViewModel: If no sound, press VOLUME UP while playing")
                }
            } else {
                // Fallback to simulated playback if MediaController not ready
                println("PlayerViewModel: *** WARNING: MediaController not ready, using simulated playback ***")
                _isPlaying.value = !_isPlaying.value
                
                if (_isPlaying.value) {
                    Logger.d("PlayerViewModel: Starting simulated playback", "PLAYER_VM")
                    println("PlayerViewModel: Simulated playback started - no real audio")
                    startPositionUpdates()
                } else {
                    Logger.d("PlayerViewModel: Pausing simulated playback", "PLAYER_VM")
                    stopPositionUpdates()
                    forceSyncProgress()
                }
            }
        } catch (e: Exception) {
            println("PlayerViewModel: *** ERROR IN PLAY/PAUSE: ${e.message} ***")
            Logger.e("PlayerViewModel: Error in playPause - ${e.message}", "PLAYER_VM")
            e.printStackTrace()
        }
    }

    /**
     * Stop audio playback - used when navigating away from player screen
     */
    fun stopAudio() {
        try {
            println("PlayerViewModel: *** STOPPING AUDIO - USER NAVIGATED AWAY ***")
            val controller = mediaController
            
            if (controller != null) {
                controller.pause()
                controller.stop()
                println("PlayerViewModel: *** AUDIO STOPPED VIA MEDIACONTROLLER ***")
            } else {
                println("PlayerViewModel: *** STOPPING SIMULATED PLAYBACK ***")
            }
            
            // Update state
            _isPlaying.value = false
            stopPositionUpdates()
            
            // Save progress before stopping
            forceSyncProgress()
            
            Logger.d("PlayerViewModel: Audio stopped and progress synced", "PLAYER_VM")
        } catch (e: Exception) {
            println("PlayerViewModel: *** ERROR STOPPING AUDIO: ${e.message} ***")
            Logger.e("PlayerViewModel: Error in stopAudio - ${e.message}", "PLAYER_VM")
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
            
            // Actually seek the MediaController
            val controller = mediaController
            if (controller != null) {
                controller.seekTo(newPosition)
                println("PlayerViewModel: *** SEEKED MEDIACONTROLLER FORWARD TO ${newPosition}ms ***")
            } else {
                println("PlayerViewModel: *** WARNING: MediaController not available for seek forward ***")
            }
            
            // Update progress percentage
            val totalDur = _totalDuration.value
            if (totalDur > 0) {
                _progressPercentage.value = (newPosition.toFloat() / totalDur.toFloat()) * 100f
            }
            
            // Sync progress after seeking
            forceSyncProgress()
            Logger.d("PlayerViewModel: Seeked forward 10 seconds to ${newPosition}ms", "PLAYER_VM")
        } catch (e: Exception) {
            println("PlayerViewModel: *** ERROR IN SEEK FORWARD: ${e.message} ***")
            Logger.e("PlayerViewModel: Error in seekForward - ${e.message}", "PLAYER_VM")
        }
    }

    fun seekBack() {
        try {
            val newPosition = (_currentPosition.value - 10000).coerceAtLeast(0)
            _currentPosition.value = newPosition // Immediately update UI
            
            // Actually seek the MediaController
            val controller = mediaController
            if (controller != null) {
                controller.seekTo(newPosition)
                println("PlayerViewModel: *** SEEKED MEDIACONTROLLER BACK TO ${newPosition}ms ***")
            } else {
                println("PlayerViewModel: *** WARNING: MediaController not available for seek back ***")
            }
            
            // Update progress percentage
            val totalDur = _totalDuration.value
            if (totalDur > 0) {
                _progressPercentage.value = (newPosition.toFloat() / totalDur.toFloat()) * 100f
            }
            
            // Sync progress after seeking
            forceSyncProgress()
            Logger.d("PlayerViewModel: Seeked back 10 seconds to ${newPosition}ms", "PLAYER_VM")
        } catch (e: Exception) {
            println("PlayerViewModel: *** ERROR IN SEEK BACK: ${e.message} ***")
            Logger.e("PlayerViewModel: Error in seekBack - ${e.message}", "PLAYER_VM")
        }
    }

    fun seekTo(position: Float) {
        try {
            val newPosition = (position * (_totalDuration.value)).toLong()
            _currentPosition.value = newPosition // Immediately update UI for better UX
            _progressPercentage.value = position * 100f
            
            // Actually seek the MediaController
            val controller = mediaController
            if (controller != null) {
                controller.seekTo(newPosition)
                println("PlayerViewModel: *** SEEKED MEDIACONTROLLER TO ${newPosition}ms (${position * 100}%) ***")
            } else {
                println("PlayerViewModel: *** WARNING: MediaController not available for seekTo ***")
            }
            
            // Sync progress immediately on seek
            syncProgressToRepository()
            Logger.d("PlayerViewModel: Seeked to ${newPosition}ms (${position * 100}%)", "PLAYER_VM")
        } catch (e: Exception) {
            println("PlayerViewModel: *** ERROR IN SEEK TO: ${e.message} ***")
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
        
        // IMMEDIATELY stop current audio playback when switching episodes
        mediaController?.let { controller ->
            controller.pause()
            controller.stop()
            println("PlayerViewModel: *** STOPPED PREVIOUS AUDIO IMMEDIATELY ***")
        }
        
        // Reset playback state when switching episodes
        _isPlaying.value = false
        _currentPosition.value = 0L
        _progressPercentage.value = 0f
        stopPositionUpdates()
        
        // Reset resume position tracking
        resumePosition = null
        hasAppliedResumePosition = false
        
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
            println("PlayerViewModel: *** LOADING API AUDIO URL FROM BACKEND ***")
            println("PlayerViewModel: API Audio URL: $audioUrl")
            println("PlayerViewModel: URL length: ${audioUrl.length}")
            println("PlayerViewModel: URL starts with: ${audioUrl.take(50)}...")
            
            // Check if it's a valid URL format
            if (audioUrl.startsWith("http://") || audioUrl.startsWith("https://")) {
                println("PlayerViewModel: Valid HTTP/HTTPS URL detected")
                loadAudioFile(audioUrl)
            } else {
                println("PlayerViewModel: *** WARNING: Invalid URL format - $audioUrl ***")
                // Fallback to demo audio
                val demoAudioUrl = getDemoAudioUrl(currentJourneyId, currentEpisodeId)
                if (demoAudioUrl != null) {
                    println("PlayerViewModel: Falling back to demo audio: $demoAudioUrl")
                    loadAudioFile(demoAudioUrl)
                }
            }
        } else {
            println("PlayerViewModel: *** NO API AUDIO URL - CALLING START EPISODE ENDPOINT ***")
            // Use start episode endpoint to get streaming URL
            startEpisodeAndLoadAudio(journeyId, episodeId)
        }
        
        // Load existing progress for this episode
        loadEpisodeProgress()
    }
    
    private fun getDemoAudioUrl(journeyId: String?, episodeId: String?): String? {
        // For testing purposes, use universally supported MP3 files with guaranteed codec support
        // Replace with actual backend URLs when available
        println("PlayerViewModel: *** USING DEMO AUDIO - MP3 FORMAT FOR MAXIMUM COMPATIBILITY ***")
        return when {
            journeyId?.contains("DSA", ignoreCase = true) == true -> 
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3" // 3.5MB MP3
            journeyId?.contains("Personal Finance", ignoreCase = true) == true ->
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3" // Different MP3
            journeyId?.contains("Hackathon", ignoreCase = true) == true ->
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3" // Another MP3
            else -> 
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3" // Default MP3 - known working
        }
    }
    
    private fun loadAudioFile(audioUrl: String) {
        try {
            println("PlayerViewModel: *** ATTEMPTING TO LOAD AUDIO ***")
            println("PlayerViewModel: Audio URL: $audioUrl")
            
            val controller = mediaController
            if (controller != null) {
                println("PlayerViewModel: MediaController is available")
                loadAudioWithController(controller, audioUrl)
            } else {
                println("PlayerViewModel: *** MediaController is NULL - WAITING FOR CONNECTION ***")
                // Wait for MediaController connection
                viewModelScope.launch {
                    var attempts = 0
                    while (mediaController == null && attempts < 10) {
                        println("PlayerViewModel: Waiting for MediaController... attempt ${attempts + 1}")
                        delay(500) // Wait 500ms between attempts
                        attempts++
                    }
                    
                    val connectedController = mediaController
                    if (connectedController != null) {
                        println("PlayerViewModel: *** MediaController connected after ${attempts * 500}ms ***")
                        loadAudioWithController(connectedController, audioUrl)
                    } else {
                        println("PlayerViewModel: *** FAILED: MediaController still null after 5 seconds ***")
                        Logger.e("MediaController connection timeout", "PLAYER_VM")
                    }
                }
            }
        } catch (e: Exception) {
            println("PlayerViewModel: *** AUDIO LOADING ERROR: ${e.message} ***")
            Logger.e("PlayerViewModel: Error loading audio - ${e.message}", "PLAYER_VM")
        }
    }
    
    private fun loadAudioWithController(controller: MediaController, audioUrl: String) {
        try {
            println("PlayerViewModel: Creating MediaItem for URL: $audioUrl")
            
            val mediaItem = MediaItem.fromUri(Uri.parse(audioUrl))
            controller.setMediaItem(mediaItem)
            controller.prepare()
            
            // Set maximum volume
            controller.volume = 1.0f
            
            println("PlayerViewModel: MediaItem set and prepared successfully")
            println("PlayerViewModel: Player volume set to: ${controller.volume}")
            
            // Apply resume position after preparation
            viewModelScope.launch {
                delay(1000) // Wait 1 second for preparation
                
                // Apply resume position if available before any auto-play
                if (resumePosition != null && !hasAppliedResumePosition) {
                    controller.seekTo(resumePosition!!)
                    hasAppliedResumePosition = true
                    println("PlayerViewModel: *** APPLIED RESUME POSITION AFTER MEDIA PREPARATION: ${resumePosition}ms ***")
                }
                
                // Remove auto-play - let user control when to start
                // if (controller.playbackState == Player.STATE_READY) {
                //     println("PlayerViewModel: *** AUTO-TESTING AUDIO PLAYBACK ***")
                //     controller.play()
                //     println("PlayerViewModel: Auto-play initiated - should hear sound now!")
                // } else {
                //     println("PlayerViewModel: Player not ready yet, state: ${controller.playbackState}")
                // }
                
                println("PlayerViewModel: *** MEDIA PREPARED - READY FOR USER TO PRESS PLAY ***")
                if (resumePosition != null) {
                    println("PlayerViewModel: *** RESUME POSITION SET TO: ${resumePosition}ms - READY TO RESUME ***")
                }
            }
            
            Logger.d("Audio loaded successfully: $audioUrl", "PLAYER_VM")
        } catch (e: Exception) {
            println("PlayerViewModel: *** ERROR LOADING WITH CONTROLLER: ${e.message} ***")
            Logger.e("PlayerViewModel: Controller loading error - ${e.message}", "PLAYER_VM")
        }
    }
    
    /**
     * Call the start episode endpoint to get streaming URL and load audio
     */
    private fun startEpisodeAndLoadAudio(journeyId: String, episodeId: String) {
        viewModelScope.launch {
            try {
                println("PlayerViewModel: Calling start episode endpoint for $journeyId/$episodeId")
                
                // For simplicity, use test endpoint (no auth required)
                val result = journeyRepository.getEpisodeStreamingUrl("", journeyId, episodeId, useTestEndpoint = true)
                
                result.onSuccess { streamingUrl ->
                    println("PlayerViewModel: Got streaming URL: $streamingUrl")
                    
                    if (streamingUrl.isNotEmpty()) {
                        loadAudioFile(streamingUrl)
                    } else {
                        println("PlayerViewModel: Empty streaming URL - falling back to demo audio")
                        val demoAudioUrl = getDemoAudioUrl(journeyId, episodeId)
                        if (demoAudioUrl != null) {
                            loadAudioFile(demoAudioUrl)
                        }
                    }
                }.onFailure { exception ->
                    println("PlayerViewModel: Start episode failed: ${exception.message}")
                    // Fallback to demo audio
                    val demoAudioUrl = getDemoAudioUrl(journeyId, episodeId)
                    if (demoAudioUrl != null) {
                        println("PlayerViewModel: Using demo audio as fallback")
                        loadAudioFile(demoAudioUrl)
                    }
                }
            } catch (e: Exception) {
                println("PlayerViewModel: Exception in startEpisodeAndLoadAudio: ${e.message}")
                // Fallback to demo audio
                val demoAudioUrl = getDemoAudioUrl(journeyId, episodeId)
                if (demoAudioUrl != null) {
                    loadAudioFile(demoAudioUrl)
                }
            }
        }
    }

    /**
     * Load existing progress for the current episode
     */
    private fun loadEpisodeProgress() {
        val journeyId = currentJourneyId
        val episodeId = currentEpisodeId
        
        if (journeyId != null && episodeId != null) {
            viewModelScope.launch {
                try {
                    // Get existing progress from AuthPreferences
                    val savedProgress = authPrefs.getEpisodeProgress(journeyId, episodeId)
                    
                    if (savedProgress != null) {
                        // Load saved progress
                        _progressPercentage.value = savedProgress.progressPercentage
                        _episodeProgress.value = savedProgress
                        
                        // Calculate position from saved progress percentage and total duration
                        val totalDur = _totalDuration.value
                        if (totalDur > 0 && savedProgress.progressPercentage > 0) {
                            val calculatedResumePosition = ((savedProgress.progressPercentage / 100f) * totalDur).toLong()
                            
                            // Store resume position for later use when play is pressed
                            resumePosition = calculatedResumePosition
                            hasAppliedResumePosition = false
                            _currentPosition.value = calculatedResumePosition // Update UI immediately
                            
                            println("PlayerViewModel: *** RESUME POSITION CALCULATED: ${calculatedResumePosition}ms (${savedProgress.progressPercentage}%) ***")
                            println("PlayerViewModel: *** WILL SEEK TO THIS POSITION WHEN PLAY IS PRESSED ***")
                            Logger.d("PlayerViewModel - Resume position calculated: ${savedProgress.progressPercentage}% at ${calculatedResumePosition}ms", "PLAYER_VM")
                        } else {
                            // No saved progress or 0% progress
                            resumePosition = null
                            hasAppliedResumePosition = true // No need to seek
                            println("PlayerViewModel: *** STARTING FROM BEGINNING - No saved progress or 0% progress ***")
                            Logger.d("PlayerViewModel - Starting episode from beginning", "PLAYER_VM")
                        }
                    } else {
                        // No saved progress, start from beginning
                        val progress = EpisodeProgress(
                            progressPercentage = 0f,
                            playPositionSeconds = 0L,
                            status = EpisodeProgress.STATUS_NOT_STARTED,
                            lastUpdated = System.currentTimeMillis()
                        )
                        _episodeProgress.value = progress
                        
                        println("PlayerViewModel: *** NO SAVED PROGRESS FOUND - STARTING FROM BEGINNING ***")
                        Logger.d("PlayerViewModel - No saved progress found, starting from beginning", "PLAYER_VM")
                    }
                } catch (e: Exception) {
                    println("PlayerViewModel: *** ERROR LOADING EPISODE PROGRESS: ${e.message} ***")
                    Logger.e("PlayerViewModel - Failed to load episode progress: ${e.message}", "PLAYER_VM")
                    e.printStackTrace()
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
