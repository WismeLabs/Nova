package com.wisme.firstapp.ui.journeys

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisme.firstapp.R
import com.wisme.firstapp.data.local.AuthPreferences
import com.wisme.firstapp.domain.EpisodeDataClass
import com.wisme.firstapp.domain.JourneysDataClass
import com.wisme.firstapp.ui.utils.ResponsiveFontSizes
import com.wisme.firstapp.viewmodel.FeedbackViewModel
import com.wisme.firstapp.viewmodel.JourneyViewModel
import com.wisme.firstapp.viewmodel.PlayerViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    journey: JourneysDataClass,
    episode: EpisodeDataClass,
    playerViewModel: PlayerViewModel,
    journeyViewModel: JourneyViewModel,
    feedbackViewModel: FeedbackViewModel = hiltViewModel(),
    authPrefs: AuthPreferences,
    onBackPress: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    println("PlayerScreen: *** PLAYER SCREEN RENDERING ***")
    println("PlayerScreen: Journey: ${journey.JourneyName}")
    println("PlayerScreen: Episode: ${episode.title} (Episode ${episode.episodeNumber})")
    println("PlayerScreen: Episode description: ${episode.description}")
    println("PlayerScreen: Episode audio URL: ${episode.audioUrl}")
    println("PlayerScreen: Episode duration: ${episode.durationMinutes} minutes")
    println("PlayerScreen: Total episodes in journey: ${journey.episodes.size}")
    // Collect state from the ViewModel in a lifecycle-aware manner
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by playerViewModel.currentPosition.collectAsStateWithLifecycle()
    val totalDuration by playerViewModel.totalDuration.collectAsStateWithLifecycle()
    
    // Collect progress tracking state
    val progressPercentage by playerViewModel.progressPercentage.collectAsStateWithLifecycle()
    val episodeProgress by playerViewModel.episodeProgress.collectAsStateWithLifecycle()
    
    // Collect error state
    val errorMessage by playerViewModel.errorMessage.collectAsStateWithLifecycle()
    val hasAudioError by playerViewModel.hasAudioError.collectAsStateWithLifecycle()
    
    // State managed by ViewModel for consistency
    val currentEpisodeIndex by playerViewModel.currentEpisodeIndex.collectAsStateWithLifecycle()
    val episodeChangeKey by playerViewModel.episodeChangeKey.collectAsStateWithLifecycle()
    val playbackSpeed by playerViewModel.playbackSpeed.collectAsStateWithLifecycle()
    
    // Fake loading overlay for visual feedback
    val showFakeLoadingOverlay by playerViewModel.showFakeLoadingOverlay.collectAsStateWithLifecycle()
    
    // Network status for offline handling
    val isOffline = remember { derivedStateOf { !playerViewModel.isNetworkCurrentlyAvailable() } }
    
    // CONFIGURATION CHANGE SURVIVAL: Derive currentEpisode from ViewModel state
    val currentEpisode = if (journey.episodes.isNotEmpty() && currentEpisodeIndex < journey.episodes.size) {
        journey.episodes[currentEpisodeIndex]
    } else {
        episode // fallback to initial episode
    }
    
    // Show fake loading screen when PlayerScreen is first opened from EpisodesPage
    LaunchedEffect(Unit) {
        playerViewModel.showFakeLoadingForNavigation()
    }
    
    // Initialize episode index on first render
    LaunchedEffect(journey.episodes, episode.episodeNumber) {
        val initialIndex = if (journey.episodes.isNotEmpty()) {
            journey.episodes.indexOfFirst { it.episodeNumber == episode.episodeNumber }.takeIf { it >= 0 } ?: 0
        } else {
            0
        }
        playerViewModel.setCurrentEpisodeIndex(initialIndex)
    }
    
    // episodeChangeKey now managed by ViewModel for configuration change survival
    
    // Set current episode in PlayerViewModel for progress tracking and start episode
    LaunchedEffect(journey.journeyId, currentEpisode.episodeNumber, episodeChangeKey) {
        try {
            val journeyId = journey.journeyId // Use database ID instead of display name
            val episodeId = "episode_${currentEpisode.episodeNumber}"
            println("PlayerScreen: Setting up episode - $journeyId/$episodeId")
            println("PlayerScreen: Journey Display Name: ${journey.JourneyName}")
            println("PlayerScreen: Journey Database ID: $journeyId")
            println("PlayerScreen: Episode audioUrl: ${currentEpisode.audioUrl}")
            println("PlayerScreen: Episode duration: ${currentEpisode.durationMinutes} minutes")
            
            // Set up episode completion callback
            playerViewModel.setEpisodeCompletionCallback { completedEpisodeId ->
                println("PlayerScreen: Episode completed - $completedEpisodeId")
                // Trigger feedback system
                feedbackViewModel.onEpisodeCompleted(completedEpisodeId)
                
                // Check if this completes the entire journey
                val totalEpisodes = journey.episodes.size
                var completedCount = 0
                
                journey.episodes.forEach { ep ->
                    val epId = "episode_${ep.episodeNumber}"
                    if (authPrefs.isEpisodeCompleted(journey.journeyId, epId)) {
                        completedCount++
                    }
                }
                
                println("PlayerScreen: Journey completion check - $completedCount/$totalEpisodes episodes completed")
                
                // If all episodes are completed, mark journey as completed
                if (completedCount >= totalEpisodes) {
                    authPrefs.markJourneyCompleted(journey.journeyId)
                    feedbackViewModel.onJourneyCompleted(journey.journeyId, journey.JourneyName)
                    println("PlayerScreen: *** JOURNEY COMPLETED - ${journey.JourneyName} ***")
                }
                
                // Auto-advance to next episode if available
                val nextEpisodeNumber = currentEpisode.episodeNumber + 1
                val nextEpisode = journey.episodes.find { it.episodeNumber == nextEpisodeNumber }
                if (nextEpisode != null) {
                    println("PlayerScreen: Auto-advancing to episode $nextEpisodeNumber")
                    println("PlayerScreen: Current episode index before: $currentEpisodeIndex")
                    
                    // CONFIGURATION CHANGE SURVIVAL: Update episode index in ViewModel
                    val newIndex = journey.episodes.indexOfFirst { it.episodeNumber == nextEpisodeNumber }
                        .takeIf { it >= 0 } ?: currentEpisodeIndex
                    playerViewModel.setCurrentEpisodeIndex(newIndex)
                    
                    // Force recomposition by triggering episode change in ViewModel
                    playerViewModel.triggerEpisodeChange()
                    
                    println("PlayerScreen: Current episode index after: $currentEpisodeIndex")
                    println("PlayerScreen: Episode change key updated to: $episodeChangeKey")
                    
                    // Set up the new episode in PlayerViewModel
                    val newJourneyId = journey.journeyId
                    val newEpisodeId = "episode_${nextEpisode.episodeNumber}"
                    
                    playerViewModel.setCurrentEpisode(
                        journeyId = newJourneyId,
                        episodeId = newEpisodeId,
                        audioUrl = nextEpisode.audioUrl,
                        durationMinutes = nextEpisode.durationMinutes
                    )
                    
                    // Start the new episode
                    journeyViewModel.startEpisode(newJourneyId, newEpisodeId)
                    
                    // Update continue learning state immediately
                    journeyViewModel.refreshContinueLearning()
                    
                    println("PlayerScreen: *** AUTO-ADVANCE COMPLETED - New episode: ${nextEpisode.title} at index $currentEpisodeIndex ***")
                } else {
                    println("PlayerScreen: No more episodes to auto-advance to")
                }
            }
            
            playerViewModel.setCurrentEpisode(
                journeyId = journeyId, 
                episodeId = episodeId,
                audioUrl = currentEpisode.audioUrl,
                durationMinutes = currentEpisode.durationMinutes
            )
            
            // Mark episode as started for progress tracking
            journeyViewModel.startEpisode(journeyId, episodeId)
        } catch (e: Exception) {
            println("PlayerScreen: Error in LaunchedEffect - ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Stop audio when user navigates away from player screen
    DisposableEffect(Unit) {
        onDispose {
            println("PlayerScreen: *** USER NAVIGATED AWAY - STOPPING AUDIO ***")
            playerViewModel.stopAudio()
        }
    }
    
    // Use progress percentage from ViewModel (which includes completion logic)
    val completionPercentage = progressPercentage / 100f
    
    // Determine if episode is completed - check persistent completion status
    val journeyId = journey.journeyId
    val episodeId = "episode_${currentEpisode.episodeNumber}"
    val isCompleted = authPrefs.isEpisodeCompleted(journeyId, episodeId)
    
    // Sync progress to JourneyViewModel periodically and on significant changes
    LaunchedEffect(progressPercentage, currentPosition) {
        if (progressPercentage > 0) {
            val journeyId = journey.journeyId // Use database ID instead of display name
            val episodeId = "episode_${currentEpisode.episodeNumber}"
            journeyViewModel.updateEpisodeProgress(
                journeyId = journeyId,
                episodeId = episodeId,
                progressPercentage = progressPercentage,
                playPositionSeconds = currentPosition / 1000
            )
            
            // Also update continue learning if this is significant progress
            if (progressPercentage > 5f) { // Update after 5% progress
                journeyViewModel.refreshContinueLearning()
            }
        }
    }

    // Get user avatar
    val userAvatarId = remember { authPrefs.userAvatarId }
    val avatarResource = remember(userAvatarId) {
        when (userAvatarId) {
            1 -> R.drawable.avatar_1
            2 -> R.drawable.avatar_2
            3 -> R.drawable.avatar_3
            4 -> R.drawable.avatar_4
            5 -> R.drawable.avatar_5
            else -> R.drawable.avatar_1 // Default fallback
        }
    }

    PlayerScreenContent(
        journey = journey,
        episode = currentEpisode,
        authPrefs = authPrefs,
        isPlaying = isPlaying,
        currentPosition = currentPosition,
        totalDuration = totalDuration,
        completionPercentage = completionPercentage,
        isCompleted = isCompleted,
        progressPercentage = progressPercentage,
        playbackSpeed = playbackSpeed,
        currentEpisodeIndex = currentEpisodeIndex,
        avatarResource = avatarResource,
        showFakeLoadingOverlay = showFakeLoadingOverlay,
        onBackPress = onBackPress,
        onProfileClick = onProfileClick,
        onPlayPause = { 
            println("PlayerScreen: Play/Pause button clicked, current state: $isPlaying")
            playerViewModel.playPause()
            playerViewModel.forceSyncProgress()
        },
        onSeekTo = { position -> 
            playerViewModel.seekTo(position)
            playerViewModel.forceSyncProgress()
        },
        onSeekBack = { 
            playerViewModel.seekBack()
            playerViewModel.forceSyncProgress()
        },
        onSeekForward = { 
            playerViewModel.seekForward()
            playerViewModel.forceSyncProgress()
        },
        onPrevious = {
            if (currentEpisodeIndex > 0) {
                // Show fake loading overlay
                playerViewModel.showFakeLoadingForNavigation()
                
                // Save progress for current episode before switching
                playerViewModel.forceSyncProgress()
                
                val newIndex = currentEpisodeIndex - 1
                val newEpisode = journey.episodes.getOrNull(newIndex)
                if (newEpisode != null) {
                    playerViewModel.setCurrentEpisodeIndex(newIndex)
                    
                    // Set new episode in PlayerViewModel for progress tracking
                    val journeyId = journey.journeyId // Use database ID instead of display name
                    val episodeId = "episode_${newEpisode.episodeNumber}"
                    println("PlayerScreen: Switching to previous episode: ${newEpisode.title}")
                    playerViewModel.setCurrentEpisode(
                        journeyId = journeyId, 
                        episodeId = episodeId,
                        audioUrl = newEpisode.audioUrl,
                        durationMinutes = newEpisode.durationMinutes
                    )
                    
                    // Start the new episode
                    journeyViewModel.startEpisode(journeyId, episodeId)
                    
                    // Update continue learning state immediately
                    journeyViewModel.refreshContinueLearning()
                } else {
                    // Reset index if episode not found - do nothing, use ViewModel state
                }
            }
        },
        onNext = {
            if (currentEpisodeIndex < journey.episodes.size - 1) {
                // Show fake loading overlay
                playerViewModel.showFakeLoadingForNavigation()
                
                // Save progress for current episode before switching
                playerViewModel.forceSyncProgress()
                
                val newIndex = currentEpisodeIndex + 1
                val newEpisode = journey.episodes.getOrNull(newIndex)
                if (newEpisode != null) {
                    playerViewModel.setCurrentEpisodeIndex(newIndex)
                    
                    // Set new episode in PlayerViewModel for progress tracking
                    val journeyId = journey.journeyId // Use database ID instead of display name
                    val episodeId = "episode_${newEpisode.episodeNumber}"
                    println("PlayerScreen: Switching to next episode: ${newEpisode.title}")
                    playerViewModel.setCurrentEpisode(
                        journeyId = journeyId, 
                        episodeId = episodeId,
                        audioUrl = newEpisode.audioUrl,
                        durationMinutes = newEpisode.durationMinutes
                    )
                    
                    // Start the new episode
                    journeyViewModel.startEpisode(journeyId, episodeId)
                    
                    // Update continue learning state immediately
                    journeyViewModel.refreshContinueLearning()
                } else {
                    // Episode not found - do nothing, use ViewModel state
                }
            }
        },
        onSpeedChange = { speed -> 
            playerViewModel.setPlaybackSpeed(speed)
        },
        onEpisodeSelect = { index ->
            // Only allow episode switching if it's a different episode
            if (index in journey.episodes.indices && index != currentEpisodeIndex) {
                // Progress saving is now handled automatically in setCurrentEpisode
                
                // Switch to new episode
                playerViewModel.setCurrentEpisodeIndex(index)
                val newEpisode = journey.episodes.getOrNull(index)
                if (newEpisode != null) {
                    // currentEpisode is now derived from ViewModel state - no manual assignment needed
                    
                    // Set new episode in PlayerViewModel for progress tracking
                    val journeyId = journey.journeyId // Use database ID instead of display name
                    val episodeId = "episode_${newEpisode.episodeNumber}"
                    println("PlayerScreen: Switching to selected episode: ${newEpisode.title}")
                    playerViewModel.setCurrentEpisode(
                        journeyId = journeyId, 
                        episodeId = episodeId,
                        audioUrl = newEpisode.audioUrl,
                        durationMinutes = newEpisode.durationMinutes
                    )
                    
                    // Start the new episode
                    journeyViewModel.startEpisode(journeyId, episodeId)
                    
                    // Update continue learning state immediately
                    journeyViewModel.refreshContinueLearning()
                }
            }
        },
        hasAudioError = hasAudioError,
        errorMessage = errorMessage,
        onClearError = { playerViewModel.clearError() },
        isLoadingEpisode = playerViewModel.isLoadingEpisode.collectAsStateWithLifecycle().value,
        isOffline = isOffline.value,
        onRetrySync = { playerViewModel.retrySync() }
    )
}

@Composable
fun CustomSliderThumb() {
    val accentColor = Color(0xFFC1FF72)
    Box(
        modifier = Modifier
            .size(17.dp)
            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = accentColor)
            .background(accentColor, CircleShape)
    )
}

@Composable
fun CustomSliderTrack(sliderFraction: Float) {
    val trackHeight = 5.dp
    val inactiveTrackColor = Color.DarkGray
    val activeTrackColor = Color(0xFFC1FF72)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(trackHeight)
    ) {
        // Draw the inactive track first
        drawLine(
            color = inactiveTrackColor,
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = trackHeight.toPx(),
            cap = StrokeCap.Round
        )

        // Calculate the end offset for the active track
        val activeTrackEndOffset = size.width * sliderFraction

        // Draw the active track on top
        drawLine(
            color = activeTrackColor,
            start = Offset(0f, center.y),
            end = Offset(activeTrackEndOffset, center.y), // Use the calculated value
            strokeWidth = trackHeight.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreenContent(
    journey: JourneysDataClass,
    episode: EpisodeDataClass,
    authPrefs: AuthPreferences,
    isPlaying: Boolean = false,
    currentPosition: Long = 0L,
    totalDuration: Long = 0L,
    completionPercentage: Float = 0f,
    isCompleted: Boolean = false,
    progressPercentage: Float = 0f,
    playbackSpeed: Float = 1f,
    currentEpisodeIndex: Int = 0,
    avatarResource: Int = R.drawable.avatar_1,
    showFakeLoadingOverlay: Boolean = false,
    onBackPress: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onPlayPause: () -> Unit = {},
    onSeekTo: (Float) -> Unit = {},
    onSeekBack: () -> Unit = {},
    onSeekForward: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onSpeedChange: (Float) -> Unit = {},
    onEpisodeSelect: (Int) -> Unit = {},
    hasAudioError: Boolean = false,
    errorMessage: String? = null,
    onClearError: () -> Unit = {},
    isLoadingEpisode: Boolean = false,
    isOffline: Boolean = false,
    onRetrySync: () -> Unit = {}
) {
    // Calculate slider position based on player progress
    val sliderPosition = if (totalDuration > 0) {
        currentPosition.toFloat() / totalDuration
    } else {
        0f
    }

    // Function to format milliseconds into MM:SS
    fun Long.formatTime(): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    val backgroundColor = Color(0xFF121212)
    val accentColor = Color(0xFFC1FF72)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Now Playing", style = MaterialTheme.typography.bodySmall, fontSize = ResponsiveFontSizes.body()) },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(painterResource(R.drawable.back_arrow), contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Image(
                            painter = painterResource(avatarResource),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = accentColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Error message display
            if (hasAudioError && !errorMessage.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onClearError
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Network status indicator
            if (isOffline) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Offline",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "You're offline. Some features may be limited.",
                            color = Color(0xFFFF9800),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = onRetrySync,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFF9800)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFF9800)),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Retry",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            // Loading state display
            if (isLoadingEpisode) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A2A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = accentColor,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Loading episode...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Cover Image - Journey-specific image
            val journeyImageResource = remember(journey.journeyId) {
                when (journey.journeyId.lowercase()) {
                    "dsa_coding_interviews", 
                    "journey_dsa" -> R.drawable.journey_dsa
                    "personal_finance", 
                    "journey_finance" -> R.drawable.journey_personal_finance
                    "hackathon_success",
                    "journey_hackathon" -> R.drawable.journey_hackathon
                    else -> R.drawable.journey_dsa // Default fallback
                }
            }
            
            Image(
                painter = painterResource(id = journeyImageResource),
                contentDescription = "${journey.JourneyName} Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(270.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Episode Info
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = episode.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = ResponsiveFontSizes.displaySmall(),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Episode ${episode.episodeNumber} - ${if (isCompleted) "Complete" else "${progressPercentage.toInt()}% complete"}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = ResponsiveFontSizes.body()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar / Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = sliderPosition,
                    onValueChange = onSeekTo,
                    thumb = { CustomSliderThumb() },
                    track = { CustomSliderTrack(sliderFraction = sliderPosition) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = currentPosition.formatTime(), color = Color.Gray, fontSize = ResponsiveFontSizes.caption())
                    Text(text = totalDuration.formatTime(), color = Color.Gray, fontSize = ResponsiveFontSizes.caption())
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSeekBack) {
                    Icon(painterResource(R.drawable.player_rewind10), contentDescription = "Replay 10 seconds", tint = Color.Unspecified, modifier = Modifier.size(32.dp))
                }
                Image(
                    painter = painterResource(R.drawable.player_previous_episode),
                    contentDescription = "Previous Episode",
                    modifier = Modifier
                        .size(27.dp)
                        .clickable { onPrevious() }
                )
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(76.dp)
                ) {
                    Icon(
                        painter = if (isPlaying) painterResource(R.drawable.player_pause) else painterResource(R.drawable.player_play),
                        contentDescription = "Play/Pause",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Image(
                    painter = painterResource(R.drawable.player_next_episode),
                    contentDescription = "Next Episode",
                    modifier = Modifier
                        .size(27.dp)
                        .clickable { onNext() }
                )
                IconButton(onClick = onSeekForward) {
                    Icon(painterResource(R.drawable.player_skip10), contentDescription = "Forward 10 seconds", tint = Color.Unspecified, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playback Speed Selector
            PlaybackSpeedSelector(
                currentSpeed = playbackSpeed,
                onSpeedChange = onSpeedChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Episode Navigation
            Text(
                text = "Episodes",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontSize = ResponsiveFontSizes.bodyLarge(),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            EpisodeCarousel(
                episodes = journey.episodes,
                currentEpisodeIndex = currentEpisodeIndex,
                onEpisodeSelect = onEpisodeSelect,
                journeyId = journey.journeyId,
                authPrefs = authPrefs
            )
        }
    }
    
    // Fake Loading Overlay - Visual feedback only
    if (showFakeLoadingOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = accentColor,
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Loading Episode...",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = ResponsiveFontSizes.bodyLarge()
                )
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSpeedSelector(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val speeds = listOf(0.5f, 1f, 1.5f, 2f)
    val accentColor = Color(0xFFC1FF72)
    
    Box {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Speed ${currentSpeed}x",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painterResource(R.drawable.arrow),
                contentDescription = "Expand",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF2A2A2A))
        ) {
            speeds.forEach { speed ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${speed}x",
                            color = if (speed == currentSpeed) accentColor else Color.White
                        )
                    },
                    onClick = {
                        onSpeedChange(speed)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun EpisodeCarousel(
    episodes: List<EpisodeDataClass>,
    currentEpisodeIndex: Int,
    journeyId: String,
    authPrefs: AuthPreferences,
    onEpisodeSelect: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val accentColor = Color(0xFFC1FF72)
    
    // Auto-scroll to current episode
    LaunchedEffect(currentEpisodeIndex) {
        if (episodes.isNotEmpty() && currentEpisodeIndex in episodes.indices) {
            listState.animateScrollToItem(currentEpisodeIndex)
        }
    }
    
    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 14.dp)
    ) {
        itemsIndexed(episodes) { index, episode ->
            // SINGLE SOURCE OF TRUTH: Use persistent completion flag only
            val episodeId = "episode_${episode.episodeNumber}"
            val isCompleted = authPrefs.isEpisodeCompleted(journeyId, episodeId)
            
            EpisodeCard(
                episode = episode,
                isCurrentPlaying = index == currentEpisodeIndex,
                isCompleted = isCompleted,
                onClick = { onEpisodeSelect(index) }
            )
        }
    }
}

@Composable
fun EpisodeCard(
    episode: EpisodeDataClass,
    isCurrentPlaying: Boolean,
    isCompleted: Boolean = false,
    onClick: () -> Unit
) {
    val accentColor = Color(0xFFC1FF72)
    val cardBackground = when {
        isCompleted -> Brush.linearGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.5f),
                accentColor.copy(alpha = 0.2f)
            )
        )
        isCurrentPlaying -> Brush.linearGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.3f),
                accentColor.copy(alpha = 0.1f)
            )
        )
        else -> Brush.linearGradient(
            colors = listOf(
                Color(0xFF2A2A2A),
                Color(0xFF1A1A1A)
            )
        )
    }
    
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBackground)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play icon
                Icon(
                    painter = painterResource(R.drawable.player_play),
                    contentDescription = "Play Episode",
                    tint = if (isCurrentPlaying) accentColor else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Episode ${episode.episodeNumber}",
                        color = if (isCurrentPlaying) accentColor else Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrentPlaying) FontWeight.Bold else FontWeight.Normal,
                        fontSize = ResponsiveFontSizes.bodySmall()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = episode.title,
                        color = if (isCurrentPlaying) Color.White else Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = ResponsiveFontSizes.caption(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerScreenPreview() {
    val sampleEpisodes = listOf(
        EpisodeDataClass(
            episodeNumber = 1,
            title = "Introduction to Arrays",
            description = "Understanding the basics of arrays and their memory allocation."
        ),
        EpisodeDataClass(
            episodeNumber = 2,
            title = "Linked Lists Fundamentals",
            description = "Deep dive into linked list data structures."
        ),
        EpisodeDataClass(
            episodeNumber = 3,
            title = "Stack and Queue Operations",
            description = "Implementing and using stacks and queues effectively."
        )
    )
    
    val sampleJourney = JourneysDataClass(
        JourneyName = "Data Structures",
        JourneyDescription = "A comprehensive guide to data structures.",
        JourneyImg = "",
        journeyId = "data_structures", // Sample database ID
        episodes = sampleEpisodes
    )

    val sampleEpisode = sampleEpisodes[0]

    PlayerScreenContent(
        journey = sampleJourney,
        episode = sampleEpisode,
        authPrefs = AuthPreferences(LocalContext.current),
        isPlaying = false,
        currentPosition = 0L,
        totalDuration = 1200000L, // 20 minutes
        completionPercentage = 0.67f,
        playbackSpeed = 1f,
        currentEpisodeIndex = 0,
        avatarResource = R.drawable.avatar_1,
        showFakeLoadingOverlay = false,
        hasAudioError = false,
        errorMessage = null,
        onClearError = { },
        isLoadingEpisode = false,
        isOffline = false,
        onRetrySync = { }
    )
}
