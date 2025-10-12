package com.wisme.research.ui.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisme.research.ui.utils.ResponsiveTextStyles
import com.wisme.research.ui.utils.ResponsiveFontSizes
import com.wisme.research.ui.utils.ResponsiveSpacing
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisme.research.theme.AppTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Surface
import com.wisme.research.data.local.AuthPreferences
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisme.research.ui.profile.UserProfileViewModel
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeFeedbackScreen(
    authPrefs: AuthPreferences,
    feedbackViewModel: com.wisme.firstapp.viewmodel.FeedbackViewModel,
    episodeId: String,
    episodeTitle: String = "Episode",
    onBackClick: () -> Unit = {},
    onSubmitFeedback: (EpisodeFeedbackData) -> Unit = {},
    onSkip: () -> Unit = {}
) {
    // Collect states from ViewModel
    val feedbackState by feedbackViewModel.episodeFeedbackState.collectAsStateWithLifecycle()
    val currentEpisode by feedbackViewModel.currentFeedbackEpisode.collectAsStateWithLifecycle()
    
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
    
    // Load previous responses when screen opens
    LaunchedEffect(episodeId) {
        feedbackViewModel.loadEpisodePreviousResponses(episodeId)
    }
    
    // Initialize with previous responses if available
    var enjoymentRating by remember { mutableStateOf(feedbackState.previousResponses["episode_enjoyment"]) }
    var clarityRating by remember { mutableStateOf(feedbackState.previousResponses["episode_clarity"]) }
    
    // Update when previous responses change
    LaunchedEffect(feedbackState.previousResponses) {
        enjoymentRating = feedbackState.previousResponses["episode_enjoyment"] ?: enjoymentRating
        clarityRating = feedbackState.previousResponses["episode_clarity"] ?: clarityRating
    }
    
    val isFormValid = enjoymentRating != null && clarityRating != null
    
    // Theme colors
    val lightGreen = Color(0xFFC1FF72)
    val darkBackground = Color(0xFF1A1A1A)
    val cardBackground = Color(0xFF27272A)
    
    // Show loading overlay if submitting
    if (feedbackState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = lightGreen)
                Text(
                    text = "Submitting feedback...",
                    color = Color.White,
                    fontSize = ResponsiveFontSizes.body()
                )
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    text = "Hi $userName! 👋",
                    fontSize = ResponsiveFontSizes.bodySmall(),
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFC1FF72)
                )
                Text(
                    text = "Episode Feedback",
                    fontSize = ResponsiveFontSizes.headingLarge(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = episodeTitle,
                    fontSize = ResponsiveFontSizes.body(),
                    color = Color.Gray
                )
            }
        }
        
        // Progress indicator
        LinearProgressIndicator(
            progress = 1f, // Since there are only 2 questions, show full progress
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = lightGreen,
            trackColor = Color.Gray.copy(alpha = 0.3f)
        )
        
        // Question 1: Enjoyment
        FeedbackQuestionCard(
            question = "Did you enjoy this episode?",
            options = listOf("Yes", "Somewhat", "No"),
            selectedOption = enjoymentRating,
            onOptionSelected = { enjoymentRating = it }
        )
        
        // Question 2: Clarity
        FeedbackQuestionCard(
            question = "Was this episode clear and easy to follow?",
            options = listOf("Very clear", "Somewhat clear", "Confusing"),
            selectedOption = clarityRating,
            onOptionSelected = { clarityRating = it }
        )
        
        // Error message
        feedbackState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    fontSize = ResponsiveFontSizes.bodySmall(),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Skip button
            OutlinedButton(
                onClick = {
                    if (!feedbackState.isLoading) {
                        onSkip()
                    }
                },
                enabled = !feedbackState.isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFCCCCCC)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = Color(0xFF444444)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Skip",
                    fontSize = ResponsiveFontSizes.body(),
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Submit button
            Button(
                onClick = {
                    if (isFormValid && !feedbackState.isLoading) {
                        feedbackViewModel.clearEpisodeFeedbackError()
                        currentEpisode?.let { episodeId ->
                            feedbackViewModel.submitEpisodeFeedback(
                                episodeId,
                                EpisodeFeedbackData(
                                    enjoyment = enjoymentRating!!,
                                    clarity = clarityRating!!
                                )
                            )
                        }
                        onSubmitFeedback(
                            EpisodeFeedbackData(
                                enjoyment = enjoymentRating!!,
                                clarity = clarityRating!!
                            )
                        )
                    }
                },
                enabled = isFormValid && !feedbackState.isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightGreen,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF333333),
                    disabledContentColor = Color(0xFF666666)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Submit",
                    fontSize = ResponsiveFontSizes.body(),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FeedbackQuestionCard(
    question: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = question,
                fontSize = ResponsiveFontSizes.bodyLarge(),
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                lineHeight = 26.sp
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                options.forEach { option ->
                    FeedbackOptionItem(
                        option = option,
                        isSelected = selectedOption == option,
                        onSelect = { onOptionSelected(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackOptionItem(
    option: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val lightGreen = Color(0xFFC1FF72)
    
    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF2A2A2A) else Color(0xFF0F0F0F),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.5.dp, lightGreen)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = lightGreen,
                    unselectedColor = Color(0xFF666666)
                )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = option,
                fontSize = ResponsiveFontSizes.body(),
                fontWeight = FontWeight.Medium,
                color = if (isSelected) lightGreen else Color(0xFFCCCCCC)
            )
        }
    }
}

data class EpisodeFeedbackData(
    val enjoyment: String,
    val clarity: String
)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun EpisodeFeedbackScreenPreview() {
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
                    text = "Episode Feedback Screen",
                    color = Color.White,
                    fontSize = ResponsiveFontSizes.headingLarge(),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Arrays & Hash Tables",
                    color = Color.White,
                    fontSize = ResponsiveFontSizes.bodyLarge()
                )
            }
        }
    }
}
