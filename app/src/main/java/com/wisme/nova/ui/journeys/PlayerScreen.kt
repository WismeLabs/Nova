package com.wisme.nova.ui.journeys

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisme.nova.viewmodel.PlayerViewModel
import com.wisme.nova.R
import com.wisme.nova.domain.EpisodeDataClass
import com.wisme.nova.domain.JourneysDataClass
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Pass the ViewModel to the screen
fun PlayerScreen(
    journey: JourneysDataClass,
    episode: EpisodeDataClass,
    playerViewModel: PlayerViewModel // <-- Add this
) {
    // Collect state from the ViewModel in a lifecycle-aware manner
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by playerViewModel.currentPosition.collectAsStateWithLifecycle()
    val totalDuration by playerViewModel.totalDuration.collectAsStateWithLifecycle()

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
    Color(0xFFC1FF72)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Now Playing", style = MaterialTheme.typography.bodySmall, fontSize = 15.sp) },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back press */ }) {
                        Icon(painterResource(R.drawable.back_arrow), contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color(0XffC1FF72)
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Image(
                painter = painterResource(id = R.drawable.sample_journey),
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth().size(270.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(70.dp))

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = journey.JourneyName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Episode ${episode.episodeNumber}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = sliderPosition,
                    onValueChange = { playerViewModel.seekTo(it) }, // <-- Use ViewModel function
                    thumb = { CustomSliderThumb() },
                    track = { CustomSliderTrack(sliderFraction = sliderPosition) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = currentPosition.formatTime(), color = Color.Gray, fontSize = 12.sp)
                    Text(text = totalDuration.formatTime(), color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { playerViewModel.seekBack() }) {
                    Icon(painterResource(R.drawable.rewind10), contentDescription = "Replay 10 seconds", tint = Color.Unspecified, modifier = Modifier.size(32.dp))
                }
                IconButton(onClick = { /* Handle previous track */ }) {
                    Icon(painterResource(R.drawable.previous_episode), contentDescription = "Previous Track", tint = Color.Unspecified, modifier = Modifier.size(40.dp))
                }
                IconButton(
                    onClick = { playerViewModel.playPause() },
                    modifier = Modifier
                        .size(76.dp)
                ) {
                    Icon(
                        painter = if (isPlaying) painterResource(R.drawable.pause) else painterResource(R.drawable.play),
                        contentDescription = "Play/Pause",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(76.dp)
                    )
                }
                IconButton(onClick = { /* Handle next track */ }) {
                    Icon(painterResource(R.drawable.next_episode), contentDescription = "Next Track", tint = Color.Unspecified, modifier = Modifier.size(40.dp))
                }
                IconButton(onClick = { playerViewModel.seekForward() }) {
                    Icon(painterResource(R.drawable.skip10), contentDescription = "Forward 10 seconds", tint = Color.Unspecified, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
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

@Preview(showBackground = true)
@Composable
fun PlayerScreenPreview() {
    val sampleJourney = JourneysDataClass(
        JourneyName = "Data Structures",
        JourneyDescription = "A deep dive into fundamental data structures.",
        JourneyImg = ""
    )

    val sampleEpisode = EpisodeDataClass(
        episodeNumber = 1,
        title = "Introduction to Arrays",
        description = "Understanding the basics of arrays and their memory allocation."
    )

    // Note: The preview will require a PlayerViewModel instance to build.
    // PlayerScreen(journey = sampleJourney, episode = sampleEpisode)
}
