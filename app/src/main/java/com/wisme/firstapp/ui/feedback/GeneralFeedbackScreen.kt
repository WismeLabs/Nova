package com.wisme.firstapp.ui.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisme.firstapp.viewmodel.FeedbackViewModel
import com.wisme.firstapp.theme.AppTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Surface
import com.wisme.firstapp.data.local.AuthPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralFeedbackScreen(
    authPrefs: AuthPreferences,
    onBackClick: () -> Unit = {},
    onFeedbackSubmitted: () -> Unit = {},
    onSkip: () -> Unit = {},
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    var revisitResponse by remember { mutableStateOf<String?>(null) }
    var recommendResponse by remember { mutableStateOf<String?>(null) }
    var wtpAmount by remember { mutableStateOf("") }
    
    // Get user data from preferences (same as HomeScreen)
    val userName = remember { authPrefs.userDisplayName ?: authPrefs.userName ?: "Welcome" }
    
    val coroutineScope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    // Load previous responses when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadPreviousGeneralResponses()
    }
    
    // Update UI when previous responses are loaded
    val previousResponses by viewModel.previousGeneralResponses.collectAsStateWithLifecycle()
    LaunchedEffect(previousResponses) {
        previousResponses?.let { responses ->
            responses.forEach { response ->
                when (response.question_id) {
                    "general_would_revisit" -> revisitResponse = response.response_value
                    "general_would_recommend" -> recommendResponse = response.response_value
                    "general_willingness_to_pay" -> wtpAmount = response.response_value
                }
            }
        }
    }
    
    val scrollState = rememberScrollState()
    val isFormValid = revisitResponse != null && recommendResponse != null && wtpAmount.isNotBlank()
    
    // Theme colors
    val lightGreen = Color(0xFFC1FF72)
    val yesNoMaybeOptions = listOf("🔥 Definitely Yes", "✨ Probably Yes", "🤔 Maybe", "👎 Probably No", "💸 Definitely No")
    
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFC1FF72)
                )
                Text(
                    text = "Research Studies",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Help us improve learning",
                    fontSize = 16.sp,
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
                        text = "🔬 Help shape the future of learning",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = lightGreen
                    )
                    Text(
                        text = "Your insights help us understand what learners truly value and how we can improve.",
                        fontSize = 15.sp,
                        color = Color(0xFFB0B0B0),
                        lineHeight = 22.sp
                    )
                }
            }
            
            // Question 1: Revisit Journeys
            FeedbackQuestionCard(
                question = "Would you want to revisit any journey to revise?",
                options = yesNoMaybeOptions,
                selectedOption = revisitResponse,
                onOptionSelected = { revisitResponse = it },
                icon = "🔄"
            )
            
            // Question 2: Recommendation
            FeedbackQuestionCard(
                question = "Would you recommend Wisme to a friend?",
                options = yesNoMaybeOptions,
                selectedOption = recommendResponse,
                onOptionSelected = { recommendResponse = it },
                icon = "👥"
            )
            
            // Question 3: Willingness to Pay (Open-ended)
            WTPQuestionCard(
                amount = wtpAmount,
                onAmountChanged = { wtpAmount = it }
            )
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Skip button
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color.White
                )
            ) {
                Text(
                    text = "Skip",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Submit button
            Button(
                onClick = {
                    println("GeneralFeedbackScreen: Submit button clicked - isFormValid=$isFormValid, isLoading=$isLoading")
                    println("GeneralFeedbackScreen: Form values - revisitResponse=$revisitResponse, recommendResponse=$recommendResponse, wtpAmount=$wtpAmount")
                    if (isFormValid && !isLoading) {
                        println("GeneralFeedbackScreen: Starting submission coroutine")
                        coroutineScope.launch {
                            val success = viewModel.submitGeneralFeedback(
                                wouldRevisit = revisitResponse!!,
                                wouldRecommend = recommendResponse!!,
                                willingnessToPay = wtpAmount
                            )
                            println("GeneralFeedbackScreen: Submission result - success=$success")
                            if (success) {
                                println("GeneralFeedbackScreen: Calling onFeedbackSubmitted callback")
                                onFeedbackSubmitted()
                            }
                        }
                    } else {
                        println("GeneralFeedbackScreen: Submit button conditions not met")
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightGreen,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Submit",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
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
                    fontSize = 14.sp
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
                            fontSize = 16.sp
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
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Text(
                    text = question,
                    fontSize = 18.sp,
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
                    GeneralFeedbackOptionItem(
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
private fun WTPQuestionCard(
    amount: String,
    onAmountChanged: (String) -> Unit
) {
    val lightGreen = Color(0xFFC1FF72)
    
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
                    text = "💰",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Text(
                    text = "If Wisme offered a fully personalized journey for any topic you want, how much would you pay?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    lineHeight = 26.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChanged,
                placeholder = {
                    Text(
                        text = "e.g., $10/month, $50 one-time, etc.",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = lightGreen,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = lightGreen
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 2
            )
            
            Text(
                text = "💡 This helps us understand the value you see in personalized learning experiences.",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun GeneralFeedbackOptionItem(
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) lightGreen else Color(0xFFCCCCCC)
            )
        }
    }
}

data class GeneralFeedbackData(
    val wouldRevisit: String,
    val wouldRecommend: String,
    val willingnessToPay: String
)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun GeneralFeedbackScreenPreview() {
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
                    text = "General Feedback Screen",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Research Survey",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}