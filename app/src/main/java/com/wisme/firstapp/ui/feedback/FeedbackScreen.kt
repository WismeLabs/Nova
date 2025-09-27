package com.wisme.firstapp.ui.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisme.firstapp.theme.AppTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Surface
import com.wisme.firstapp.data.local.AuthPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    authPrefs: AuthPreferences,
    feedbackViewModel: com.wisme.firstapp.viewmodel.FeedbackViewModel,
    onEpisodeFeedbackClick: () -> Unit = {},
    onJourneyFeedbackClick: () -> Unit = {},
    onGeneralFeedbackClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToJourneys: () -> Unit = {}
) {
    // Collect states from ViewModel
    val shouldShowEpisode = feedbackViewModel.shouldShowEpisodeFeedback()
    val shouldShowJourney = feedbackViewModel.shouldShowJourneyFeedback()
    val hasSubmittedGeneral by feedbackViewModel.hasSubmittedGeneralFeedback.collectAsStateWithLifecycle()
    
    // Get user data from preferences (same as HomeScreen)
    val userName = remember { authPrefs.userDisplayName ?: authPrefs.userName ?: "Welcome" }
    
    // Refresh feedback states when screen is opened
    LaunchedEffect(Unit) {
        feedbackViewModel.refreshFeedbackStates()
    }
    com.wisme.firstapp.ui.common.Scaffold(
        onNavigateToHome = onNavigateToHome,
        onNavigateToJourneys = onNavigateToJourneys,
        onNavigateToFeedback = { }, // Already on feedback screen
        currentRoute = "feedback"
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
        // Personalized greeting and title
        Column(
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Hi $userName! 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFC1FF72)
            )
            Text(
                text = "Research & Feedback",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Feedback Cards - Always show all cards
        println("FeedbackScreen: Rendering Episode Feedback card - shouldShow=$shouldShowEpisode")
        // Episode Feedback
        FeedbackCard(
            title = "Episode Feedback",
            subtitle = "Share your thoughts on individual episodes",
            buttonLabel = if (shouldShowEpisode) "Give Episode Feedback" else "Complete an episode to answer",
            enabled = shouldShowEpisode,
            onClick = { if (shouldShowEpisode) onEpisodeFeedbackClick() }
        )
        
        println("FeedbackScreen: Rendering Journey Feedback card - shouldShow=$shouldShowJourney")
        // Journey Feedback
        FeedbackCard(
            title = "Journey Feedback", 
            subtitle = "Compare and evaluate complete learning journeys",
            buttonLabel = if (shouldShowJourney) "Give Journey Feedback" else "Complete a journey to answer",
            enabled = shouldShowJourney,
            onClick = { if (shouldShowJourney) onJourneyFeedbackClick() }
        )
        
        println("FeedbackScreen: Rendering General Feedback card (Research Studies) - always enabled")
        // General Feedback - Always enabled
        FeedbackCard(
            title = "Research Studies",
            subtitle = "Participate in our broader research study",
            buttonLabel = "Give General Feedback",
            enabled = true,
            onClick = {
                println("FeedbackScreen: General feedback card clicked")
                onGeneralFeedbackClick()
            }
        )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackCard(
    title: String,
    subtitle: String,
    buttonLabel: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    println("FeedbackCard: Rendering card - Title: '$title', ButtonLabel: '$buttonLabel', Enabled: $enabled")
    val greenGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFCFFF57), // Light green
            Color(0xFF7CFF7D)  // Darker green
        )
    )
    
    Card(
        onClick = { if (enabled) onClick() },
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Subtitle
                Text(
                    text = subtitle,
                    fontSize = 15.sp,
                    color = Color(0xFFB0B0B0),
                    lineHeight = 22.sp
                )
            }
            
            // Bottom section with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = if (enabled) greenGradient else Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2A2A2A),
                                Color(0xFF3A3A3A)
                            )
                        ),
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = buttonLabel,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (enabled) Color.Black else Color(0xFF888888)
                        )
                        
                        if (enabled) {
                            Text(
                                text = "Help us improve",
                                fontSize = 13.sp,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    if (enabled) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Navigate",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun FeedbackScreenPreview() {
    AppTheme {
        // Simple preview showing the basic structure
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF2D1B69)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Feedback Hub",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Share your experience with us",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}