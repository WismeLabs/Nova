package com.wisme.research.ui.topic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisme.research.ui.utils.ResponsiveTextStyles
import com.wisme.research.ui.utils.ResponsiveSpacing
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisme.research.viewmodel.TopicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicRequestScreen(
    onNavigateBack: () -> Unit,
    onSubmitTopic: (String) -> Unit = {}
) {
    val topicViewModel: TopicViewModel = hiltViewModel()
    
    var topicText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    
    val isSubmitting by topicViewModel.isSubmitting.collectAsStateWithLifecycle()
    val submitResult by topicViewModel.submitResult.collectAsStateWithLifecycle()
    val popularTopics by topicViewModel.popularTopics.collectAsStateWithLifecycle()
    
    // Client-side validation function
    fun validateInput(input: String): String? {
        val trimmed = input.trim()
        return when {
            trimmed.isEmpty() -> "Topic cannot be empty"
            trimmed.length < 3 -> "Topic must be at least 3 characters"
            trimmed.length > 200 -> "Topic cannot exceed 200 characters"
            else -> null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Request Learning Topic",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2D1B69)
                )
            )
        },
        containerColor = Color(0xFF2D1B69)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(ResponsiveSpacing.large()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(ResponsiveSpacing.large()))
            
            // Explanatory text
            Text(
                text = "What would you like to learn?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = ResponsiveSpacing.medium())
            )
            
            Text(
                text = "Share your learning interests with us! This helps us understand what topics our community is most excited about.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = ResponsiveSpacing.small())
            )
            
            Text(
                text = "Based on popular requests, we're building an AI-powered feature that will create personalized learning journeys for any topic you want to explore.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC1FF72),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = ResponsiveSpacing.large())
            )
            
            // Topic input field
            OutlinedTextField(
                value = topicText,
                onValueChange = { 
                    topicText = it
                    validationError = validateInput(it)
                },
                label = { 
                    Text(
                        "Enter your topic of interest",
                        color = Color.White.copy(alpha = 0.7f)
                    ) 
                },
                placeholder = { 
                    Text(
                        "e.g., Advanced Python, Digital Marketing, Photography...",
                        color = Color.White.copy(alpha = 0.5f)
                    ) 
                },
                supportingText = {
                    if (validationError != null) {
                        Text(
                            text = validationError!!,
                            color = Color(0xFFF44336),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = "${topicText.length}/200 characters",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                isError = validationError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (validationError != null) ResponsiveSpacing.medium() else ResponsiveSpacing.large()),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = if (validationError != null) Color(0xFFF44336) else Color(0xFF6C5CE7),
                    unfocusedBorderColor = if (validationError != null) Color(0xFFF44336) else Color.White.copy(alpha = 0.5f),
                    errorBorderColor = Color(0xFFF44336),
                    cursorColor = Color(0xFF6C5CE7)
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )
            
            // Submit button
            Button(
                onClick = {
                    if (topicText.isNotBlank() && validationError == null) {
                        topicViewModel.submitTopicRequest(topicText.trim())
                        topicText = ""
                        validationError = null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = topicText.isNotBlank() && !isSubmitting && validationError == null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C5CE7),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Submit Request",
                        style = ResponsiveTextStyles.bodyLarge().copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(ResponsiveSpacing.large()))
            
            // Success/Error message
            submitResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.isSuccess) {
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        } else {
                            Color(0xFFF44336).copy(alpha = 0.1f)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (result.isSuccess) {
                            "✅ ${result.getOrNull()}"
                        } else {
                            "❌ ${result.exceptionOrNull()?.message ?: "Failed to submit topic request"}"
                        },
                        color = if (result.isSuccess) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFF44336)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(ResponsiveSpacing.medium())
                    )
                }
                
                LaunchedEffect(submitResult) {
                    kotlinx.coroutines.delay(4000)
                    topicViewModel.clearSubmitResult()
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Footer note
            Text(
                text = "Your requests help us prioritize which topics to develop next!",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = ResponsiveSpacing.medium())
            )
        }
    }
}
