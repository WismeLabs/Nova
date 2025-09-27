package com.wisme.firstapp.ui.journeys

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisme.firstapp.R
import com.wisme.firstapp.domain.EpisodeDataClass
import com.wisme.firstapp.domain.JourneysDataClass
import com.wisme.firstapp.viewmodel.JourneyViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

val darkBackground = Color(0xFF121212)
val textGreen = Color(0XFFC1FF72)
val olive = Color(0XFF4a5c34)

@Composable
fun EpisodesPage(
    journeyName: String,
    viewModel: JourneyViewModel = hiltViewModel(),
    onEpisodeClick: (JourneysDataClass, EpisodeDataClass) -> Unit = { _, _ -> },
    onNavigateToHome: () -> Unit = {},
    onNavigateToJourneys: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {}
) {
    val journeys by viewModel.journeys.collectAsState()
    
    // Find the matching journey by name
    val currentJourney = journeys.find { journey ->
        journey.JourneyName.equals(journeyName, ignoreCase = true) ||
        journey.JourneyName.replace("/", "_").replace(" ", "_").equals(journeyName.replace(" ", "_"), ignoreCase = true)
    }
    
    Scaffold(
        containerColor = darkBackground,
        bottomBar = {
            EpisodesBottomNavigationBar(
                onNavigateToHome = onNavigateToHome,
                onNavigateToJourneys = onNavigateToJourneys,
                onNavigateToFeedback = onNavigateToFeedback
            )
        }
    ) { paddingValues ->
        if (currentJourney != null) {
            EpisodesScreenContent(
                journey = currentJourney,
                episodes = currentJourney.episodes,
                onEpisodeClick = onEpisodeClick,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // Show loading or fallback
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(darkBackground)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Loading journey...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Journey: $journeyName",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EpisodesScreenContent(
    journey: JourneysDataClass, 
    episodes: List<EpisodeDataClass>,
    onEpisodeClick: (JourneysDataClass, EpisodeDataClass) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(horizontal = 16.dp).padding(top=32.dp)
    ) {
        //Header
        item {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                //Personalised chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(olive)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.green_sparkles),
                        contentDescription = "Personalised",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Personalised", color = Color(0XFFC1FF72), fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                //Journey description
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Journey-specific image
                    val journeyImageResource = remember(journey.JourneyName) {
                        when (journey.JourneyName.lowercase()) {
                            "dsa & coding interviews", 
                            "dsa / cracking coding interviews" -> R.drawable.journey_dsa
                            "personal finance mastery", 
                            "personal finance" -> R.drawable.journey_personal_finance
                            "hackathon success guide",
                            "hackathon success" -> R.drawable.journey_hackathon
                            else -> R.drawable.journey_dsa // Default fallback
                        }
                    }
                    
                    Image(
                        painter = painterResource(id = journeyImageResource),
                        contentDescription = journey.JourneyName,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = journey.JourneyName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = journey.JourneyDescription, color = Color.White, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.clock), contentDescription = "Episodes count", tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${episodes.size} episodes", color = textGreen, fontSize = 14.sp)
                    }
                    Text(text = "~32 minutes total", color = textGreen, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        //Episodes Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Episodes", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { 
                    println("EpisodesPage: 'Start from beginning' button clicked")
                    if (episodes.isNotEmpty()) {
                        val firstEpisode = episodes.first()
                        println("EpisodesPage: Starting first episode: ${firstEpisode.title}")
                        onEpisodeClick(journey, firstEpisode)
                    } else {
                        println("EpisodesPage: No episodes available to start")
                    }
                }) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", tint = Color(0xFFC1FF72), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Start from beginning", color = textGreen)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        //Episodes list
        items(episodes) { episode ->
            EpisodeItem(
                episode = episode,
                onClick = { 
                    println("EpisodesPage: Episode clicked - Journey: ${journey.JourneyName}, Episode: ${episode.title}")
                    onEpisodeClick(journey, episode) 
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EpisodeItem(
    episode: EpisodeDataClass,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(olive),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = episode.episodeNumber.toString(),
                color = textGreen,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = episode.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = episode.description, color = Color.Gray, fontSize = 14.sp, maxLines = 2)
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(
            onClick = {
                println("EpisodesPage: Play button (IconButton) clicked for episode: ${episode.title} (Episode ${episode.episodeNumber})")
                println("EpisodesPage: About to call onClick() function")
                onClick() // Use the same onClick as the row
                println("EpisodesPage: onClick() function called successfully")
            },
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF454446))
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Episode", tint = textGreen)
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(olive)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = Color(0XFFe9ebe6), fontSize = 12.sp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun EpisodesScreenPreview() {

    val sampleJourney = JourneysDataClass(
        JourneyName = "Human Psychology",
        JourneyDescription = "Discover the fascinating world of human psychology and understand what drives our behaviour",
        JourneyImg = "",
        journeyId = "human_psychology" // Sample database ID
    )

    val sampleEpisodes = listOf(
        EpisodeDataClass(1, "The Science of First Impressions", "How we form instant judgements and what influences first impressions"),
        EpisodeDataClass(2, "Why We Procrastinate", "Understanding the psychology behind procrastination and how to overcome it"),
        EpisodeDataClass(3, "Social Media and the Psychology", "How social media affects our behaviour and decision-making processes"),
        EpisodeDataClass(4, "The Psychology of Relationships", "Understand how we form bonds and maintain meaningful relationships")
    )

    EpisodesScreenContent(journey = sampleJourney, episodes = sampleEpisodes)
}

@Composable
fun EpisodesBottomNavigationBar(
    onNavigateToHome: () -> Unit = {},
    onNavigateToJourneys: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {}
) {
    val lightGreen = Color(0xFFC1FF72)
    
    NavigationBar(
        containerColor = Color(0xFF27272A),
        modifier = Modifier.height(90.dp)
    ) {
        NavigationBarItem(
            label = { 
                Text(
                    "Learn",
                    style = MaterialTheme.typography.labelSmall,
                    color = lightGreen // Highlighting current section
                )
            },
            selected = true,
            onClick = onNavigateToJourneys,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.learn),
                    contentDescription = "Learn",
                    tint = lightGreen
                )
            },
            modifier = Modifier.padding(top = 10.dp),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            label = { 
                Text(
                    "Home",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            },
            selected = false,
            onClick = onNavigateToHome,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.home_icon),
                    contentDescription = "Home",
                    tint = Color.White
                )
            },
            modifier = Modifier.padding(top = 10.dp),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            label = { 
                Text(
                    "Feedback",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            },
            selected = false,
            onClick = onNavigateToFeedback,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.feedback),
                    contentDescription = "Feedback",
                    tint = Color.White
                )
            },
            modifier = Modifier.padding(top = 10.dp),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
    }
}
