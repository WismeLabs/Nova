package com.wisme.research.ui.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisme.research.ui.utils.ResponsiveTextStyles
import com.wisme.research.ui.utils.ResponsiveFontSizes
import com.wisme.research.ui.utils.ResponsiveSpacing
import com.wisme.research.viewmodel.FeedbackViewModel
import com.wisme.research.theme.AppTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Surface
import com.wisme.research.data.local.AuthPreferences
import com.wisme.research.ui.profile.UserProfileViewModel
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyFeedbackScreen(
    authPrefs: AuthPreferences,
    journeyId: String,
    journeyTitle: String = "Journey",
    onBackClick: () -> Unit = {},
    onFeedbackSubmitted: () -> Unit = {},
    onSkip: () -> Unit = {},
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    var moocsRating by remember { mutableStateOf<String?>(null) }
    var youtubeRating by remember { mutableStateOf<String?>(null) }
    var blogsRating by remember { mutableStateOf<String?>(null) }
    
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
    
    val coroutineScope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Load previous responses when screen opens
    LaunchedEffect(journeyId) {
        viewModel.loadPreviousJourneyResponses(journeyId)
    }
    
    // Update UI when previous responses are loaded
    val previousResponses by viewModel.previousJourneyResponses.collectAsState()
    LaunchedEffect(previousResponses) {
        previousResponses?.let { responses ->
            responses.forEach { response ->
                when (response.question_id) {
                    "journey_moocs_comparison" -> moocsRating = response.response_value
                    "journey_youtube_comparison" -> youtubeRating = response.response_value
                    "journey_blogs_comparison" -> blogsRating = response.response_value
                }
            }
        }
    }
    
    val scrollState = rememberScrollState()
    val isFormValid = moocsRating != null && youtubeRating != null && blogsRating != null
    
    // Theme colors
    val lightGreen = Color(0xFFC1FF72)
    val comparisonOptions = listOf("Much better", "Slightly better", "About the same", "Worse")
    
    Box(modifier = Modifier.fillMaxSize()) {
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
                    text = "Journey Feedback",
                    fontSize = ResponsiveFontSizes.headingLarge(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = journeyTitle,
                    fontSize = ResponsiveFontSizes.body(),
                    color = Color.Gray
                )
            }
        }
        
        // Progress indicator
        LinearProgressIndicator(
            progress = 1f,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = lightGreen,
            trackColor = Color.Gray.copy(alpha = 0.3f)
        )
        
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Introduction
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📊 Help us understand how we compare",
                        fontSize = ResponsiveFontSizes.body(),
                        fontWeight = FontWeight.SemiBold,
                        color = lightGreen
                    )
                    Text(
                        text = "Your feedback helps us improve and deliver better learning experiences.",
                        fontSize = ResponsiveFontSizes.body(),
                        color = Color(0xFFB0B0B0),
                        lineHeight = 22.sp
                    )
                }
            }
            
            // Question 1: MOOCs Comparison
            FeedbackQuestionCard(
                question = "Compared to MOOCs (like Coursera, Udemy), how would you rate this journey?",
                options = comparisonOptions,
                selectedOption = moocsRating,
                onOptionSelected = { moocsRating = it },
                icon = "🎓"
            )
            
            // Question 2: YouTube Comparison
            FeedbackQuestionCard(
                question = "Compared to YouTube, how would you rate this journey?",
                options = comparisonOptions,
                selectedOption = youtubeRating,
                onOptionSelected = { youtubeRating = it },
                icon = "📺"
            )
            
            // Question 3: Blogs/Articles Comparison
            FeedbackQuestionCard(
                question = "Compared to blogs/articles, how would you rate this journey?",
                options = comparisonOptions,
                selectedOption = blogsRating,
                onOptionSelected = { blogsRating = it },
                icon = "📖"
            )
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Skip button
            OutlinedButton(
                onClick = onSkip,
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
                    if (isFormValid && !isLoading) {
                        coroutineScope.launch {
                            val success = viewModel.submitJourneyFeedback(
                                journeyId = journeyId,
                                moocsComparison = moocsRating!!,
                                youtubeComparison = youtubeRating!!,
                                blogsComparison = blogsRating!!
                            )
                            if (success) {
                                onFeedbackSubmitted()
                            }
                        }
                    }
                },
                enabled = isFormValid && !isLoading,
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
        
        // Error message display
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
            ) {
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp),
                    fontSize = ResponsiveFontSizes.bodySmall()
                )
            }
        }
        }
        
        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF27272A)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = lightGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Submitting feedback...",
                            color = Color.White,
                            fontSize = ResponsiveFontSizes.body()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackQuestionCard(
    question: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    icon: String
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = icon,
                    fontSize = ResponsiveFontSizes.headingLarge(),
                    modifier = Modifier.padding(end = 16.dp)
                )
                Text(
                    text = question,
                    fontSize = ResponsiveFontSizes.bodyLarge(),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    lineHeight = 26.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                options.forEach { option ->
                    ComparisonOptionItem(
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
private fun ComparisonOptionItem(
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

data class JourneyFeedbackData(
    val moocsComparison: String,
    val youtubeComparison: String,
    val blogsComparison: String
)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun JourneyFeedbackScreenPreview() {
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
                    text = "Journey Feedback Screen",
                    color = Color.White,
                    fontSize = ResponsiveFontSizes.headingLarge(),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "DSA & Coding Interviews",
                    color = Color.White,
                    fontSize = ResponsiveFontSizes.bodyLarge()
                )
            }
        }
    }
}
