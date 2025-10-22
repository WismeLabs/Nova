package com.wisme.firstapp.viewmodel

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
import com.wisme.firstapp.R
import com.wisme.firstapp.data.local.AuthPreferences
import com.wisme.firstapp.data.repository.JourneyRepository
import com.wisme.firstapp.data.repository.ConnectivityRepository
import com.wisme.firstapp.domain.EpisodeProgress
import com.wisme.firstapp.util.Logger
import com.wisme.firstapp.utils.PlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.SavedStateHandle
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application, 
    private val journeyRepository: JourneyRepository,
    private val authPrefs: AuthPreferences,
    private val savedStateHandle: SavedStateHandle,
    private val connectivityRepository: ConnectivityRepository
) : AndroidViewModel(application) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(savedStateHandle.get<Long>("current_position") ?: 0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _totalDuration = MutableStateFlow(savedStateHandle.get<Long>("total_duration") ?: 0L)
    val totalDuration = _totalDuration.asStateFlow()

    // Progress tracking state
    private val _progressPercentage = MutableStateFlow(savedStateHandle.get<Float>("progress_percentage") ?: 0f)
    val progressPercentage = _progressPercentage.asStateFlow()

    private val _episodeProgress = MutableStateFlow<EpisodeProgress?>(null)
    val episodeProgress = _episodeProgress.asStateFlow()

    // CRITICAL: Error state tracking for user feedback
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    
    private val _hasAudioError = MutableStateFlow(false)
    val hasAudioError = _hasAudioError.asStateFlow()

    // Loading state to prevent UI reset before data is loaded
    private val _isLoadingEpisode = MutableStateFlow(false)
    val isLoadingEpisode = _isLoadingEpisode.asStateFlow()

    // UI state - moved from remember for configuration change survival
    private val _playbackSpeed = MutableStateFlow(savedStateHandle.get<Float>("playback_speed") ?: 1f)
    val playbackSpeed = _playbackSpeed.asStateFlow()
    
    private val _currentEpisodeIndex = MutableStateFlow(savedStateHandle.get<Int>("current_episode_index") ?: 0)
    val currentEpisodeIndex = _currentEpisodeIndex.asStateFlow()
    
    // CONFIGURATION CHANGE SURVIVAL: Episode change trigger
    private val _episodeChangeKey = MutableStateFlow(savedStateHandle.get<Int>("episode_change_key") ?: 0)
    val episodeChangeKey = _episodeChangeKey.asStateFlow()

    // Current episode tracking - CRITICAL for process death recovery
    private var currentJourneyId: String? = savedStateHandle.get<String>("current_journey_id")
    private var currentEpisodeId: String? = savedStateHandle.get<String>("current_episode_id")
    
    // Episode completion callback
    private var onEpisodeCompletedCallback: ((String) -> Unit)? = null
    
    // Resume position for episodes with saved progress
    private var resumePosition: Long? = null
    private var hasAppliedResumePosition = false

    private var positionJob: Job? = null
    private var progressSyncJob: Job? = null
    
    // Progress batching system
    private val progressBatch = mutableMapOf<String, Double>() // episodeId -> latest progress
    private var batchSyncJob: Job? = null
    private val BATCH_INTERVAL_MS = 30000L // Batch every 30 seconds for better performance
    private val MIN_PROGRESS_CHANGE = 1.0 // Only sync if progress changed by at least 1%
    
    // Media3 MediaController for actual audio playback
    private var mediaController: MediaController? = null
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null

    init {
        Logger.d("PlayerViewModel: Initializing with MediaController", "PLAYER_VM")
        println("PlayerViewModel: Setting up MediaController connection")
        checkDeviceAudioCodecs()
        initializeMediaController()
        
        // Save critical state to survive process death
        setupStatePersistence()
        
        // Start progress batching system
        startProgressBatching()
        
        // Start periodic retry of pending syncs
        startPendingSyncRetry()
    }
    
    /**
     * Set up state persistence to survive process death
     */
    private fun setupStatePersistence() {
        viewModelScope.launch {
            _currentPosition.collect { position ->
                savedStateHandle["current_position"] = position
            }
        }
        
        viewModelScope.launch {
            _totalDuration.collect { duration ->
                savedStateHandle["total_duration"] = duration
            }
        }
        
        viewModelScope.launch {
            _progressPercentage.collect { percentage ->
                savedStateHandle["progress_percentage"] = percentage
            }
        }
        
        viewModelScope.launch {
            _playbackSpeed.collect { speed ->
                savedStateHandle["playback_speed"] = speed
            }
        }
        
        viewModelScope.launch {
            _currentEpisodeIndex.collect { index ->
                savedStateHandle["current_episode_index"] = index
            }
        }
        
        viewModelScope.launch {
            _episodeChangeKey.collect { key ->
                savedStateHandle["episode_change_key"] = key
            }
        }
    }
    
    /**
     * Check if there's a saved episode state to recover after process death
     */
    fun hasSavedEpisodeState(): Boolean {
        return currentJourneyId != null && currentEpisodeId != null
    }
    
    /**
     * Get saved episode state for recovery
     */
    fun getSavedEpisodeState(): Pair<String, String>? {
        return if (currentJourneyId != null && currentEpisodeId != null) {
            Pair(currentJourneyId!!, currentEpisodeId!!)
        } else null
    }
    
    /**
     * Set playback speed - survives configuration changes
     */
    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        
        // Apply speed to MediaController
        mediaController?.let { controller ->
            val playbackParams = android.media.PlaybackParams()
            playbackParams.speed = speed
            try {
                controller.setPlaybackParams(playbackParams)
                Logger.d("PlayerViewModel: Set playback speed to ${speed}x", "PLAYER_VM")
            } catch (e: Exception) {
                Logger.e("PlayerViewModel: Failed to set playback speed - ${e.message}", "PLAYER_VM")
                _errorMessage.value = "Failed to change playback speed. Please try again."
                _hasAudioError.value = true
            }
        }
    }
    
    /**
     * Update current episode index to prevent desync
     */
    fun setCurrentEpisodeIndex(index: Int) {
        _currentEpisodeIndex.value = index
        Logger.d("PlayerViewModel - Episode index set to $index", "PLAYER_VM")
    }
    
    /**
     * Trigger episode change recomposition (configuration change survival)
     */
    fun triggerEpisodeChange() {
        _episodeChangeKey.value += 1
        Logger.d("PlayerViewModel - Episode change triggered: ${_episodeChangeKey.value}", "PLAYER_VM")
    }
    
    /**
     * Check journey completion status independent of callback
     */
    fun checkJourneyCompletion(journeyName: String, totalEpisodes: Int): Boolean {
        var completedCount = 0
        
        for (episodeNumber in 1..totalEpisodes) {
            val episodeId = "episode_$episodeNumber"
            if (authPrefs.isEpisodeCompleted(journeyName, episodeId)) {
                completedCount++
            }
        }
        
        val isJourneyComplete = completedCount >= totalEpisodes
        Logger.d("PlayerViewModel - Journey completion check: $completedCount/$totalEpisodes episodes completed", "PLAYER_VM")
        
        if (isJourneyComplete && !authPrefs.isJourneyCompleted(journeyName)) {
            Logger.d("PlayerViewModel - Journey completed, marking as complete: $journeyName", "PLAYER_VM")
            authPrefs.markJourneyCompleted(journeyName)
        }
        
        return isJourneyComplete
    }
    
    /**
     * Clear error message and error state
     */
    fun clearError() {
        _errorMessage.value = null
        _hasAudioError.value = false
    }
    
    /**
     * Set callback to be triggered when episode is completed
     */
    fun setEpisodeCompletionCallback(callback: (String) -> Unit) {
        onEpisodeCompletedCallback = callback
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
            
            // Show user-visible error
            _errorMessage.value = "Audio system failed to initialize. Please restart the app."
            _hasAudioError.value = true
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
                    // Sync progress when pausing
                    forceSyncProgress()
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                println("PlayerViewModel: Playback state changed to $playbackState")
                when (playbackState) {
                    Player.STATE_READY -> {
                        val duration = mediaController?.duration ?: 0L
                        if (duration > 0) {
                            _totalDuration.value = duration
                            println("PlayerViewModel: Audio duration set to ${duration}ms (${duration/60000} minutes)")
                        }
                    }
                    Player.STATE_ENDED -> {
                        println("PlayerViewModel: *** EPISODE ENDED - FORCING COMPLETION ***")
                        _isPlaying.value = false
                        _progressPercentage.value = 100f
                        forceSyncProgress()
                    }
                    Player.STATE_IDLE -> {
                        println("PlayerViewModel: Player is idle")
                    }
                    Player.STATE_BUFFERING -> {
                        println("PlayerViewModel: Player is buffering")
                    }
                }
            }
            
            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                println("PlayerViewModel: Position discontinuity - old: ${oldPosition.positionMs}ms, new: ${newPosition.positionMs}ms, reason: $reason")
                // Update position immediately on discontinuity
                _currentPosition.value = newPosition.positionMs
                updateProgressFromPosition()
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
                
                // Check if player is in a valid state for playback
                val isPlayerReady = controller.playbackState == Player.STATE_READY || 
                                  controller.playbackState == Player.STATE_BUFFERING ||
                                  (controller.playbackState == Player.STATE_ENDED && controller.duration > 0)
                
                if (!isPlayerReady) {
                    println("PlayerViewModel: *** WARNING: Player not ready for playback, state: ${controller.playbackState} ***")
                    // Try to prepare the player again
                    controller.prepare()
                    return
                }
                
                // Use actual MediaController
                if (controller.isPlaying) {
                    controller.pause()
                    println("PlayerViewModel: *** PAUSING AUDIO PLAYBACK ***")
                } else {
                    // Ensure maximum volume before playing
                    controller.volume = 1.0f
                    controller.playWhenReady = true
                    
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
                    
                    // Resume position is handled after media preparation - NOT HERE
                    // This prevents double application and timing issues
                    
                    controller.play()
                    println("PlayerViewModel: *** STARTING AUDIO PLAYBACK ***")
                    println("PlayerViewModel: Player volume set to maximum (1.0)")
                    println("PlayerViewModel: If no sound, press VOLUME UP while playing")
                }
            } else {
                // MediaController not ready - maintain state consistency
                println("PlayerViewModel: *** WARNING: MediaController not ready - cannot play audio ***")
                println("PlayerViewModel: *** CRITICAL: Audio will not work until MediaController is ready ***")
                
                // DO NOT toggle state blindly - this causes state desync!
                // Instead, set explicit states based on intention
                val isCurrentlyPlaying = _isPlaying.value
                
                if (!isCurrentlyPlaying) {
                    // User wants to play but MediaController not ready
                    Logger.e("PlayerViewModel: Cannot start playback - MediaController not initialized", "PLAYER_VM")
                    // Keep playing state as false since we can't actually play
                    _isPlaying.value = false
                    
                    // CRITICAL: Show user error message that audio is not ready
                    _hasAudioError.value = true
                    _errorMessage.value = "Audio system is loading. Please wait a moment and try again."
                    println("PlayerViewModel: *** USER ERROR: Audio system not ready, please wait ***")
                } else {
                    // User wants to pause - we can always pause
                    Logger.d("PlayerViewModel: Pausing (no MediaController)", "PLAYER_VM")
                    _isPlaying.value = false
                    stopPositionUpdates()
                    forceSyncProgress()
                }
            }
        } catch (e: Exception) {
            println("PlayerViewModel: *** ERROR IN PLAY/PAUSE: ${e.message} ***")
            Logger.e("PlayerViewModel: Error in playPause - ${e.message}", "PLAYER_VM")
            e.printStackTrace()
            
            // Show user-visible error
            _errorMessage.value = "Audio playback error occurred. Please try again."
            _hasAudioError.value = true
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

    private fun updateProgressFromPosition() {
        val currentPos = _currentPosition.value
        val totalDur = _totalDuration.value
        
        if (totalDur > 0) {
            val progress = (currentPos.toFloat() / totalDur.toFloat()) * 100f
            val clampedProgress = progress.coerceIn(0f, 100f)
            _progressPercentage.value = clampedProgress
            
            println("PlayerViewModel: Progress updated - ${currentPos}ms/${totalDur}ms = ${clampedProgress}%")
        }
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionJob = viewModelScope.launch {
            while (_isPlaying.value) {
                val controller = mediaController
                if (controller != null && controller.playbackState != Player.STATE_IDLE) {
                    // Get actual position from MediaController
                    val currentPos = controller.currentPosition.coerceAtLeast(0L)
                    val totalDur = controller.duration.takeIf { it > 0 } ?: _totalDuration.value
                    
                    // Update position and duration
                    _currentPosition.value = currentPos
                    if (totalDur > 0 && totalDur != _totalDuration.value) {
                        _totalDuration.value = totalDur
                        println("PlayerViewModel: Duration updated to ${totalDur}ms")
                    }
                    
                    // Update progress
                    updateProgressFromPosition()
                    
                    // Check if we've reached the very end (within 500ms) - mark as complete
                    if (totalDur > 0 && currentPos >= totalDur - 500L) {
                        println("PlayerViewModel: *** REACHED END OF EPISODE - marking as complete ***")
                        _progressPercentage.value = 100f  // Set to 100% when truly at the end
                        _isPlaying.value = false
                        syncProgressToRepository()
                        // Don't break the loop yet - let it handle completion naturally
                    }
                    
                } else {
                    // Fallback to simulated progress if MediaController is not ready
                    val currentPos = _currentPosition.value + 1000L
                    val totalDur = _totalDuration.value
                    
                    if (totalDur > 0) {
                        _currentPosition.value = currentPos.coerceAtMost(totalDur)
                        updateProgressFromPosition()
                        
                        // Stop simulation if we've reached the end
                        if (currentPos >= totalDur) {
                            _isPlaying.value = false
                            _progressPercentage.value = 100f
                            syncProgressToRepository()
                            break
                        }
                    }
                }
                
                delay(1500) // Update every 1.5 seconds for better battery life
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
            
            // Show user-visible error
            _errorMessage.value = "Audio seeking failed. Please try again."
            _hasAudioError.value = true
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
            
            // Show user-visible error
            _errorMessage.value = "Audio seeking failed. Please try again."
            _hasAudioError.value = true
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
            
            // Show user-visible error
            _errorMessage.value = "Audio seeking failed. Please try again."
            _hasAudioError.value = true
        }
    }

    /**
     * Set the current episode being played and load audio
     */
    fun setCurrentEpisode(journeyId: String, episodeId: String, audioUrl: String? = null, durationMinutes: Int = 0) {
        // Set loading state to prevent UI flicker
        _isLoadingEpisode.value = true
        
        currentJourneyId = journeyId
        currentEpisodeId = episodeId
        
        // Save episode IDs to survive process death
        savedStateHandle["current_journey_id"] = journeyId
        savedStateHandle["current_episode_id"] = episodeId
        
        Logger.d("PlayerViewModel - Setting current episode: $journeyId/$episodeId", "PLAYER_VM")
        println("PlayerViewModel: Setting episode with audioUrl: $audioUrl, duration: $durationMinutes minutes")
        
        // CRITICAL: Save previous episode's progress before switching
        if (currentJourneyId != null && currentEpisodeId != null && 
            (currentJourneyId != journeyId || currentEpisodeId != episodeId)) {
            println("PlayerViewModel: *** SAVING PREVIOUS EPISODE PROGRESS BEFORE SWITCH ***")
            forceSyncProgress() // Save current progress immediately
        }
        
        // IMMEDIATELY stop current audio playback when switching episodes
        mediaController?.let { controller ->
            controller.pause()
            controller.stop()
            println("PlayerViewModel: *** STOPPED PREVIOUS AUDIO IMMEDIATELY ***")
        }
        
        // Reset playback state when switching episodes
        _isPlaying.value = false
        stopPositionUpdates()
        
        // Reset resume position tracking
        resumePosition = null
        hasAppliedResumePosition = false
        
        // DON'T reset position/progress immediately - let loadEpisodeProgress() handle it
        // This prevents the UI from flashing 0% before loading saved progress
        
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
            
            // Set maximum volume and ensure no interruptions
            controller.volume = 1.0f
            controller.playWhenReady = false // Don't auto-play
            
            println("PlayerViewModel: MediaItem set and prepared successfully")
            println("PlayerViewModel: Player volume set to: ${controller.volume}")
            
            // Apply resume position after preparation with proper timing
            viewModelScope.launch {
                delay(2000) // Wait 2 seconds for stable preparation
                
                // Ensure the player is still valid and ready
                if (controller.playbackState == Player.STATE_READY || controller.playbackState == Player.STATE_BUFFERING) {
                    // CONSOLIDATED: Apply resume position calculated during prepareEpisode
                    applyResumePositionIfNeeded(controller)
                    
                    // Clear loading state - media is ready
                    _isLoadingEpisode.value = false
                    
                    println("PlayerViewModel: *** MEDIA PREPARED AND READY FOR PLAYBACK ***")
                    if (resumePosition != null) {
                        println("PlayerViewModel: *** WILL RESUME FROM: ${resumePosition}ms ***")
                    } else {
                        println("PlayerViewModel: *** WILL START FROM BEGINNING ***")
                    }
                } else {
                    println("PlayerViewModel: *** WARNING: Player not ready after preparation, state: ${controller.playbackState} ***")
                }
            }
            
            Logger.d("Audio loaded successfully: $audioUrl", "PLAYER_VM")
        } catch (e: Exception) {
            println("PlayerViewModel: *** ERROR LOADING WITH CONTROLLER: ${e.message} ***")
            Logger.e("PlayerViewModel: Controller loading error - ${e.message}", "PLAYER_VM")
            e.printStackTrace()
            
            // Clear loading state even on error
            _isLoadingEpisode.value = false
            _hasAudioError.value = true
            _errorMessage.value = "Failed to load audio: ${e.message}"
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
                        // Load saved progress and position
                        _progressPercentage.value = savedProgress.progressPercentage
                        _currentPosition.value = savedProgress.playPositionSeconds * 1000L  // Convert to milliseconds
                        _episodeProgress.value = savedProgress
                        
                        println("PlayerViewModel: *** LOADED SAVED PROGRESS - ${savedProgress.progressPercentage}% at position ${savedProgress.playPositionSeconds}s ***")
                        
                        // Only calculate resume position if we have meaningful progress (> 5% and < 95%)
                        if (savedProgress.progressPercentage > 5f && savedProgress.progressPercentage < 95f) {
                            // Use the saved position in seconds if available and reasonable
                            val savedPositionMs = savedProgress.playPositionSeconds * 1000L
                            
                            // CONSOLIDATED: Calculate resume position but don't apply yet
                            calculateResumePosition(savedProgress, savedPositionMs)
                        } else {
                            // Episode is just started or almost complete - start from beginning
                            _currentPosition.value = 0L  // Reset position for start/end episodes
                            resumePosition = null
                            hasAppliedResumePosition = true
                            println("PlayerViewModel: *** STARTING FROM BEGINNING - Progress: ${savedProgress.progressPercentage}% ***")
                            Logger.d("PlayerViewModel - Starting from beginning due to progress level", "PLAYER_VM")
                        }
                    } else {
                        // No saved progress, start from beginning
                        _currentPosition.value = 0L
                        _progressPercentage.value = 0f
                        
                        val progress = EpisodeProgress(
                            progressPercentage = 0f,
                            playPositionSeconds = 0L,
                            status = EpisodeProgress.STATUS_NOT_STARTED,
                            lastUpdated = System.currentTimeMillis()
                        )
                        _episodeProgress.value = progress
                        resumePosition = null
                        hasAppliedResumePosition = true
                        
                        println("PlayerViewModel: *** NO SAVED PROGRESS FOUND - STARTING FROM BEGINNING ***")
                        Logger.d("PlayerViewModel - No saved progress found, starting from beginning", "PLAYER_VM")
                    }
                    
                    // Clear loading state once progress is loaded
                    _isLoadingEpisode.value = false
                } catch (e: Exception) {
                    println("PlayerViewModel: *** ERROR LOADING EPISODE PROGRESS: ${e.message} ***")
                    Logger.e("PlayerViewModel - Failed to load episode progress: ${e.message}", "PLAYER_VM")
                    e.printStackTrace()
                    
                    // Safe fallback
                    _currentPosition.value = 0L
                    _progressPercentage.value = 0f
                    resumePosition = null
                    hasAppliedResumePosition = true
                    
                    // Clear loading state even on error
                    _isLoadingEpisode.value = false
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
                delay(30000) // Sync every 30 seconds while playing for better performance
                syncProgressToRepository(useBatching = true) // Use batching for regular progress updates
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
     * Send episode completion event to backend for persistent tracking
     * @return true if analytics were sent successfully, false otherwise
     */
    private suspend fun sendCompletionAnalytics(episodeId: String): Boolean {
        val journeyId = currentJourneyId
        
        if (journeyId != null && journeyRepository != null) {
            return try {
                val token = authPrefs.firebaseToken
                if (!token.isNullOrEmpty()) {
                    val currentPos = _currentPosition.value
                    val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(java.util.Date())
                    
                    val result = journeyRepository.recordListenEvent(
                        token = token,
                        episodeId = episodeId,
                        eventType = "complete",
                        timestamp = timestamp,
                        playPositionSeconds = (currentPos / 1000).toInt(),
                        sessionId = null
                    )
                    
                    if (result.isSuccess) {
                        println("PlayerViewModel: *** COMPLETION ANALYTICS SENT TO BACKEND - $episodeId ***")
                        Logger.d("PlayerViewModel - Completion analytics sent for $episodeId", "PLAYER_VM")
                        true
                    } else {
                        Logger.e("PlayerViewModel - Analytics failed: ${result.exceptionOrNull()?.message}", "PLAYER_VM")
                        false
                    }
                } else {
                    println("PlayerViewModel: *** NO TOKEN AVAILABLE FOR COMPLETION ANALYTICS ***")
                    false
                }
            } catch (e: Exception) {
                Logger.e("PlayerViewModel - Failed to send completion analytics: ${e.message}", "PLAYER_VM")
                println("PlayerViewModel: *** FAILED TO SEND COMPLETION ANALYTICS: ${e.message} ***")
                false
            }
        }
        return false
    }
    
    /**
     * Check if network is available
     */
    private suspend fun isNetworkAvailable(): Boolean {
        return try {
            val backendStatus = connectivityRepository.checkBackendHealth()
            backendStatus.isHealthy
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Queue pending completion for retry when analytics succeed
     */
    private fun queuePendingCompletion(journeyId: String, episodeId: String, progress: Float) {
        val completionData = "$journeyId|$episodeId|$progress|${System.currentTimeMillis()}"
        // Add to pending sync queue for retry
        authPrefs.addPendingProgressSync(journeyId, episodeId, "completion|$completionData")
        Logger.d("PlayerViewModel - Episode completion queued for retry", "PLAYER_VM")
    }

    /**
     * Sync current progress to repository (with optional batching)
     */
    private fun syncProgressToRepository(useBatching: Boolean = false) {
        val journeyId = currentJourneyId
        val episodeId = currentEpisodeId
        
        if (journeyId != null && episodeId != null && journeyRepository != null) {
            viewModelScope.launch {
                try {
                    val currentPos = _currentPosition.value
                    val progress = _progressPercentage.value
                    
                    Logger.d("PlayerViewModel - Syncing progress: ${progress}% at ${currentPos/1000}s", "PLAYER_VM")
                    
                    // If using batching and not completed, add to batch instead of immediate sync
                    // Consider completed if progress >= 95% OR if we've reached 100%
                    val isCompleted = progress >= EpisodeProgress.COMPLETION_THRESHOLD || progress >= 100f
                    if (useBatching && !isCompleted) {
                        addToProgressBatch(episodeId, progress.toDouble())
                        return@launch
                    }
                    
                    // Update local episode progress in AuthPreferences
                    val updatedProgress = EpisodeProgress(
                        progressPercentage = progress,
                        playPositionSeconds = (currentPos / 1000),
                        status = if (isCompleted) {
                            EpisodeProgress.STATUS_COMPLETED
                        } else {
                            EpisodeProgress.STATUS_IN_PROGRESS
                        },
                        lastUpdated = System.currentTimeMillis()
                    )
                    
                    // Check for progress conflicts with backend before saving
                    val resolvedProgress = resolveProgressConflict(journeyId, episodeId, updatedProgress)
                    
                    // Save resolved progress to AuthPreferences
                    authPrefs.setEpisodeProgress(journeyId, episodeId, resolvedProgress)
                    
                    _episodeProgress.value = resolvedProgress
                    
                    // SINGLE SOURCE OF TRUTH: Use persistent completion flag as the authority
                    val wasAlreadyMarkedComplete = authPrefs.isEpisodeCompleted(journeyId, episodeId)
                    
                    // Trigger completion if episode reached threshold AND not already processed
                    if (isCompleted && !wasAlreadyMarkedComplete && episodeId != null) {
                        Logger.d("PlayerViewModel - Episode completed: $episodeId at ${progress}%", "PLAYER_VM")
                        
                        // CRITICAL: Send completion analytics FIRST to ensure backend consistency
                        val analyticsSuccess = sendCompletionAnalytics(episodeId)
                        val networkAvailable = isNetworkAvailable()
                        
                        // Only mark as completed locally if analytics succeeded OR we're offline (will retry)
                        if (analyticsSuccess || !networkAvailable) {
                            authPrefs.markEpisodeCompleted(journeyId, episodeId)
                            
                            // Trigger the completion callback
                            onEpisodeCompletedCallback?.invoke(episodeId)
                            
                            // Check journey completion independent of callback
                            val totalEpisodes = currentState.selectedJourney?.episodes?.size ?: 0
                            if (totalEpisodes > 0) {
                                checkJourneyCompletion(journeyId, totalEpisodes)
                            }
                            
                            println("PlayerViewModel: *** EPISODE COMPLETED AND MARKED PERMANENTLY - $journeyId/$episodeId ***")
                        } else {
                            // Analytics failed and we're online - queue for retry
                            Logger.w("PlayerViewModel - Analytics failed, episode completion queued for retry", "PLAYER_VM")
                            queuePendingCompletion(journeyId, episodeId, progress)
                        }
                    } else if (isCompleted && wasAlreadyMarkedComplete) {
                        // Episode already completed - just ensure progress is updated
                        println("PlayerViewModel: Episode $episodeId already marked complete, skipping completion callback")
                    }
                    
                } catch (e: Exception) {
                    Logger.e("PlayerViewModel - Failed to sync progress: ${e.message}", "PLAYER_VM")
                    
                    // Add to offline queue for retry when network returns
                    val currentPos = _currentPosition.value
                    val progress = _progressPercentage.value
                    val progressData = "${progress}|${currentPos}|${System.currentTimeMillis()}"
                    authPrefs.addPendingProgressSync(journeyId, episodeId, progressData)
                    
                    Logger.d("PlayerViewModel - Progress sync queued for retry", "PLAYER_VM")
                }
            }
        }
    }

    /**
     * Get current episode progress for external access
     */
    fun getCurrentEpisodeProgress(): Triple<String?, String?, Float>? {
        return if (currentJourneyId != null && currentEpisodeId != null) {
            // Safe access without force unwrapping to prevent crashes
            Triple(currentJourneyId, currentEpisodeId, _progressPercentage.value)
        } else null
    }

    /**
     * Force sync progress (called when pausing, seeking, etc.)
     */
    fun forceSyncProgress() {
        syncProgressToRepository()
    }
    
    /**
     * Resolve progress conflicts between local and backend data
     */
    private suspend fun resolveProgressConflict(
        journeyId: String, 
        episodeId: String, 
        localProgress: EpisodeProgress
    ): EpisodeProgress {
        return try {
            // Get existing local progress
            val existingLocal = authPrefs.getEpisodeProgress(journeyId, episodeId)
            
            // If no existing progress, use new progress
            if (existingLocal == null) {
                Logger.d("PlayerViewModel - No existing progress, using new progress", "PLAYER_VM")
                return localProgress
            }
            
            // CONFLICT RESOLUTION RULES:
            // 1. Always prefer higher progress percentage (user made more progress)
            // 2. If percentages equal, prefer more recent timestamp
            // 3. Always preserve completion status once achieved
            
            val shouldUseLocal = when {
                // Rule 1: Always preserve completion
                existingLocal.status == EpisodeProgress.STATUS_COMPLETED -> {
                    Logger.d("PlayerViewModel - Existing episode completed, preserving completion", "PLAYER_VM")
                    false // Keep existing completed status
                }
                localProgress.status == EpisodeProgress.STATUS_COMPLETED -> {
                    Logger.d("PlayerViewModel - New progress shows completion, using new", "PLAYER_VM")
                    true // Use new completion
                }
                // Rule 2: Higher progress wins
                localProgress.progressPercentage > existingLocal.progressPercentage -> {
                    Logger.d("PlayerViewModel - New progress higher (${localProgress.progressPercentage}% vs ${existingLocal.progressPercentage}%), using new", "PLAYER_VM")
                    true
                }
                localProgress.progressPercentage < existingLocal.progressPercentage -> {
                    Logger.d("PlayerViewModel - Existing progress higher (${existingLocal.progressPercentage}% vs ${localProgress.progressPercentage}%), keeping existing", "PLAYER_VM")
                    false
                }
                // Rule 3: Same progress, prefer more recent
                else -> {
                    val useLocal = localProgress.lastUpdated > existingLocal.lastUpdated
                    Logger.d("PlayerViewModel - Same progress, using ${if (useLocal) "new" else "existing"} based on timestamp", "PLAYER_VM")
                    useLocal
                }
            }
            
            if (shouldUseLocal) localProgress else existingLocal
            
        } catch (e: Exception) {
            Logger.e("PlayerViewModel - Error resolving progress conflict: ${e.message}", "PLAYER_VM")
            localProgress // Fallback to local progress on error
        }
    }
    
    /**
     * Check network connectivity and process pending operations
     */
    fun checkNetworkAndProcessPending() {
        viewModelScope.launch {
            try {
                // Simple network check by trying to reach backend
                val backendStatus = connectivityRepository.checkBackendHealth()
                if (backendStatus.isHealthy) {
                    Logger.d("PlayerViewModel - Network available, processing pending syncs", "PLAYER_VM")
                    processPendingSyncs()
                } else {
                    Logger.d("PlayerViewModel - Network unavailable, syncs remain queued", "PLAYER_VM")
                }
            } catch (e: Exception) {
                Logger.e("PlayerViewModel - Network check failed: ${e.message}", "PLAYER_VM")
            }
        }
    }
    
    /**
     * Process pending progress syncs when network returns
     */
    fun processPendingSyncs() {
        viewModelScope.launch {
            val pendingSyncs = authPrefs.getPendingProgressSyncs()
            for (syncItem in pendingSyncs) {
                try {
                    val parts = syncItem.split("|")
                    if (parts.size >= 4) {
                        val journeyId = parts[0]
                        val episodeId = parts[1]
                        val progressData = parts[2]
                        val progressParts = progressData.split("|")
                        
                        if (progressParts.size >= 2) {
                            val progress = progressParts[0].toFloat()
                            val position = progressParts[1].toLong()
                            
                            // Try to sync with backend
                            val result = journeyRepository.updateEpisodeProgress(
                                journeyId = journeyId,
                                episodeId = episodeId,
                                progressPercentage = progress,
                                playPositionSeconds = (position / 1000)
                            )
                            
                            if (result.isSuccess) {
                                // Successfully synced, remove from queue
                                authPrefs.removePendingProgressSync(journeyId, episodeId)
                                Logger.d("PlayerViewModel - Pending sync successful: $journeyId/$episodeId", "PLAYER_VM")
                            } else {
                                Logger.d("PlayerViewModel - Pending sync still failing: $journeyId/$episodeId", "PLAYER_VM")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Logger.e("PlayerViewModel - Error processing pending sync: ${e.message}", "PLAYER_VM")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        
        // Stop all background operations first
        stopPositionUpdates()
        stopProgressSync()
        
        // Stop playback and release MediaController properly
        try {
            mediaController?.let { controller ->
                controller.pause()
                controller.stop()
                controller.clearMediaItems()
            }
        } catch (e: Exception) {
            Logger.e("PlayerViewModel: Error stopping playback: ${e.message}", "PLAYER_VM")
        }
        
        // Final progress sync before clearing
        syncProgressToRepository()
        
        // Release MediaController resources
        try {
            mediaControllerFuture?.let { future ->
                MediaController.releaseFuture(future)
            }
            mediaController = null
            mediaControllerFuture = null
            println("PlayerViewModel: MediaController released properly")
        } catch (e: Exception) {
            println("PlayerViewModel: Error releasing MediaController: ${e.message}")
            Logger.e("PlayerViewModel: MediaController cleanup error: ${e.message}", "PLAYER_VM")
        }
        
        Logger.d("PlayerViewModel: Cleared and synced final progress", "PLAYER_VM")
    }
    
    /**
     * Start the progress batching system to reduce network spam
     */
    private fun startProgressBatching() {
        batchSyncJob?.cancel()
        batchSyncJob = viewModelScope.launch {
            while (true) {
                delay(BATCH_INTERVAL_MS)
                processPendingProgressBatch()
            }
        }
    }
    
    /**
     * Add progress to batch for later sync
     */
    private fun addToProgressBatch(episodeId: String, progress: Double) {
        val currentBatchProgress = progressBatch[episodeId] ?: 0.0
        
        // Only update if progress changed significantly
        if (kotlin.math.abs(progress - currentBatchProgress) >= MIN_PROGRESS_CHANGE) {
            progressBatch[episodeId] = progress
            Logger.d("PlayerViewModel - Added to progress batch: $episodeId = ${progress}%", "PLAYER_VM")
        }
    }
    
    /**
     * Process and sync batched progress updates
     */
    private suspend fun processPendingProgressBatch() {
        if (progressBatch.isEmpty()) return
        
        val batchToProcess = progressBatch.toMap()
        progressBatch.clear()
        
        Logger.d("PlayerViewModel - Processing progress batch: ${batchToProcess.size} items", "PLAYER_VM")
        
        batchToProcess.forEach { (episodeId, progress) ->
            try {
                // Use existing sync logic but in batch
                if (isNetworkAvailable()) {
                    val success = journeyRepository.syncProgress(episodeId, progress.toFloat())
                    if (!success) {
                        // Re-add to batch for retry
                        progressBatch[episodeId] = progress
                        Logger.w("PlayerViewModel - Batch sync failed, re-queued: $episodeId", "PLAYER_VM")
                    }
                } else {
                    // No network, re-add to batch for retry
                    progressBatch[episodeId] = progress
                }
            } catch (e: Exception) {
                Logger.e("PlayerViewModel - Batch sync error for $episodeId: ${e.message}", "PLAYER_VM")
                // Re-add to batch for retry
                progressBatch[episodeId] = progress
            }
        }
    }
    
    /**
     * Force immediate sync of all batched progress
     */
    fun flushProgressBatch() {
        viewModelScope.launch {
            processPendingProgressBatch()
        }
    }
    
    /**
     * CONSOLIDATED: Calculate resume position from saved progress (called during prepareEpisode)
     */
    private fun calculateResumePosition(savedProgress: EpisodeProgress, savedPositionMs: Long) {
        // Prefer saved position over calculated percentage
        if (savedPositionMs > 5000L) { // At least 5 seconds into the episode
            resumePosition = savedPositionMs
            hasAppliedResumePosition = false
            _currentPosition.value = savedPositionMs
            
            println("PlayerViewModel: *** RESUME FROM SAVED POSITION: ${savedPositionMs}ms (${savedProgress.progressPercentage}%) ***")
            Logger.d("PlayerViewModel - Resume from saved position: ${savedPositionMs}ms", "PLAYER_VM")
        } else {
            // Fallback to percentage calculation
            val totalDur = _totalDuration.value
            if (totalDur > 0) {
                val calculatedResumePosition = ((savedProgress.progressPercentage / 100f) * totalDur).toLong()
                
                if (calculatedResumePosition > 5000L) {
                    resumePosition = calculatedResumePosition
                    hasAppliedResumePosition = false
                    _currentPosition.value = calculatedResumePosition
                    
                    println("PlayerViewModel: *** RESUME FROM CALCULATED POSITION: ${calculatedResumePosition}ms (${savedProgress.progressPercentage}%) ***")
                    Logger.d("PlayerViewModel - Resume from calculated position: ${calculatedResumePosition}ms", "PLAYER_VM")
                } else {
                    resumePosition = null
                    hasAppliedResumePosition = true
                    println("PlayerViewModel: *** CALCULATED POSITION TOO EARLY - STARTING FROM BEGINNING ***")
                }
            } else {
                // Duration not available yet, wait for it
                resumePosition = null
                hasAppliedResumePosition = false
                println("PlayerViewModel: *** DURATION NOT AVAILABLE - WILL CALCULATE RESUME POSITION LATER ***")
            }
        }
    }
    
    /**
     * CONSOLIDATED: Apply resume position once media is ready (called from media preparation callback)
     */
    private suspend fun applyResumePositionIfNeeded(controller: MediaController) {
        if (resumePosition != null && !hasAppliedResumePosition) {
            resumePosition?.let { position ->
                println("PlayerViewModel: *** SEEKING TO RESUME POSITION: ${position}ms ***")
                controller.seekTo(position)
                hasAppliedResumePosition = true
                
                // Wait for seek to complete
                delay(500)
                println("PlayerViewModel: *** RESUME POSITION APPLIED SUCCESSFULLY ***")
            }
        }
    }
    
    /**
     * Start periodic retry of pending syncs for offline queue processing
     */
    private fun startPendingSyncRetry() {
        viewModelScope.launch {
            while (true) {
                delay(60000) // Check every minute for pending syncs
                checkNetworkAndProcessPending()
            }
        }
    }
}
