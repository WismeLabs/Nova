package com.wisme.research.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisme.research.ui.utils.ResponsiveTextStyles
import com.wisme.research.ui.utils.ResponsiveSpacing
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.wisme.research.R
import com.wisme.research.ui.common.TopAppBar
import com.wisme.research.data.local.AuthPreferences
import com.wisme.research.viewmodel.JourneyViewModel
import com.wisme.research.viewmodel.TopicViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisme.research.ui.profile.UserProfileViewModel
import androidx.compose.runtime.getValue


@Composable
fun HomeScreen(
    authPrefs: AuthPreferences,
    journeyViewModel: JourneyViewModel,
    modifier: Modifier = Modifier,
    onNavigateToJourneys: () -> Unit = {},
    onNavigateToPlayer: (String, String) -> Unit = { _, _ -> },
    onNavigateToFeedback: () -> Unit = {},
    onNavigateToUserProfile: () -> Unit = {},
    onNavigateToTopicRequest: () -> Unit = {}
) {

    // Get user profile view model for real user data
    val userProfileViewModel: UserProfileViewModel = hiltViewModel()
    val userProfile by userProfileViewModel.userProfile.collectAsState()
    
    // Get user data - use API data if available, otherwise use cached preferences
    val currentProfile = userProfile
    val userName = if (currentProfile != null) {
        currentProfile.display_name
    } else {
        authPrefs.userDisplayName ?: authPrefs.userName ?: "Welcome"
    }
    
    val userAvatarId = if (currentProfile != null) {
        currentProfile.avatar_id
    } else {
        authPrefs.userAvatarId
    }
    
    // Observe progress tracking state
    val hasStartedAnyEpisode by journeyViewModel.hasStartedAnyEpisode.collectAsState()
    val continueLearning by journeyViewModel.continueLearning.collectAsState()
    val journeys by journeyViewModel.journeys.collectAsState()
    
    // Get topic view model for popular topics
    val topicViewModel: TopicViewModel = hiltViewModel()
    val popularTopics by topicViewModel.popularTopics.collectAsStateWithLifecycle()
    
    // Map avatar ID to drawable resource
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
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Dark background to match app theme
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar with dynamic user data
            TopAppBar(
                username = userName,
                avatarResId = avatarResource,
                onAvatarClick = onNavigateToUserProfile
            )
            
            // Scrollable content
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 180.dp) // Space for resume section (80dp) + navbar (90dp) + padding
            ) {
                item {
                    Spacer(modifier = Modifier.height(ResponsiveSpacing.medium()))
                }
            
            item {
                // Category Chips Section
                CategoryChipsSection(
                    popularTopics = popularTopics,
                    onNavigateToTopicRequest = onNavigateToTopicRequest
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(ResponsiveSpacing.large()))
            }
            
            item {
                // Main Welcome Image
                MainWelcomeImageSection()
            }
            
            item {
                Spacer(modifier = Modifier.height(ResponsiveSpacing.large()))
            }
            
            item {
                // Upcoming Features Section
                UpcomingFeaturesSection()
            }
            
                item {
                    Spacer(modifier = Modifier.height(ResponsiveSpacing.large()))
                }
            }
        }
        
        // Fixed sections at bottom (Resume + Navigation)
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            // Only show continue learning if user has started any episode
            if (hasStartedAnyEpisode && continueLearning != null) {
                val (journeyId, episodeId) = continueLearning!!
                ResumeLearningSection(
                    journeyId = journeyId,
                    episodeId = episodeId,
                    journeys = journeys,
                    onPlayClick = { jId, eId -> onNavigateToPlayer(jId, eId) }
                )
            }
            BottomNavigationBar(
                onNavigateToJourneys = onNavigateToJourneys,
                onNavigateToFeedback = onNavigateToFeedback
            )
        }
    }
}

@Composable
fun CategoryChipsSection(
    popularTopics: List<com.wisme.firstapp.data.api.TopicRequestInfo>,
    onNavigateToTopicRequest: () -> Unit
) {
    // Use popular topics from backend, fallback to diverse default topics
    val topics = if (popularTopics.isNotEmpty()) {
        popularTopics.map { "${it.topic} (${it.request_count})" }
    } else {
        listOf(
            "Machine Learning", "Photography", "Stock Trading", 
            "Startups", "Cooking Basics", "Virat Kohli"
        )
    }
    
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = ResponsiveSpacing.large())) {
        Text(
            text = "What users want to learn?",
            style = ResponsiveTextStyles.titleLarge().copy(fontWeight = FontWeight.Medium),
            color = Color.White,
            modifier = Modifier.padding(bottom = ResponsiveSpacing.medium())
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(ResponsiveSpacing.medium())
        ) {
            itemsIndexed(topics) { index, topic ->
                Surface(
                    shape = RoundedCornerShape(25.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text(
                        text = topic,
                        modifier = Modifier.padding(horizontal = ResponsiveSpacing.large(), vertical = ResponsiveSpacing.small()),
                        color = Color.White,
                        style = ResponsiveTextStyles.bodyMedium().copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
        
        // Add topic request button
        Spacer(modifier = Modifier.height(ResponsiveSpacing.medium()))
        
        Button(
            onClick = onNavigateToTopicRequest,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ResponsiveSpacing.large()),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C5CE7),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "What would you like to learn?",
                style = ResponsiveTextStyles.bodyLarge().copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(vertical = ResponsiveSpacing.extraSmall())
            )
        }
    }
}

@Composable
fun MainWelcomeImageSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.home_welcome),
            contentDescription = "Welcome to Wisme",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

@Composable
fun UpcomingFeaturesSection() {
    val lightGreen = Color(0xFFC1FF72)
    
    val features = listOf(
        Triple(R.drawable.home_feat1, "Generate Journeys", "for any topic"),
        Triple(R.drawable.home_feat2, "Conversational A.I.", "Buddy"),
        Triple(R.drawable.home_feat3, "Enhanced", "personalization")
    )
    
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming Features",
                style = ResponsiveTextStyles.titleLarge().copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(ResponsiveSpacing.medium()))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(features) { index, (imageRes, title, subtitle) ->
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(160.dp)
                ) {
                    // Background Image
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                    
                    // Overlay with text
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Color.Black.copy(alpha = 0.7f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                color = Color.White,
                                style = ResponsiveTextStyles.bodyLarge().copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = subtitle,
                                color = lightGreen,
                                style = ResponsiveTextStyles.bodyMedium().copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResumeLearningSection(
    journeyId: String,
    episodeId: String,
    journeys: List<com.wisme.firstapp.domain.JourneysDataClass>,
    onPlayClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lightGreen = Color(0xFFC1FF72)
    val darkGreen = Color(0xFF1A241F)
    
    // Find the current journey and episode - use database ID first, then fallback to name matching
    val currentJourney = journeys.find { 
        it.journeyId == journeyId ||
        it.JourneyName.equals(journeyId, ignoreCase = true) ||
        it.JourneyName.contains(journeyId, ignoreCase = true)
    }
    val currentEpisode = currentJourney?.episodes?.find { episode ->
        "episode_${episode.episodeNumber}" == episodeId
    }
    
    // Fallback display if journey/episode not found
    val displayJourneyName = currentJourney?.JourneyName ?: "Unknown Journey"
    val displayEpisodeTitle = currentEpisode?.title ?: "Episode ${episodeId.removePrefix("episode_")}"
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Episode thumbnail/icon
            Card(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = darkGreen)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎧",
                        style = ResponsiveTextStyles.titleLarge()
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(ResponsiveSpacing.medium()))
            
            // Episode info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Continue Learning",
                    color = Color.White,
                    style = ResponsiveTextStyles.bodySmall().copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = displayJourneyName,
                    color = Color.White,
                    style = ResponsiveTextStyles.bodyLarge().copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = displayEpisodeTitle,
                    color = Color.Gray,
                    style = ResponsiveTextStyles.bodyMedium()
                )
            }
            
            // Play button
            Card(
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        onPlayClick(journeyId, episodeId)
                    },
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = lightGreen)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        color = Color.Black,
                        style = ResponsiveTextStyles.bodyLarge().copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
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
                    color = Color.White
                )
            },
            selected = false,
            onClick = { onNavigateToJourneys() },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.learn),
                    contentDescription = "Book",
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
                    "Home",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            },
            selected = true,
            onClick = { /* Already on Home screen */ },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.home_icon),
                    contentDescription = "Home",
                    tint = lightGreen // Using app theme color for selected item
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

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HomeScreenPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        // Note: Preview cannot use actual JourneyViewModel due to dependencies
        Text(
            text = "HomeScreen Preview - Cannot show with ViewModel dependencies",
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
    }
}
