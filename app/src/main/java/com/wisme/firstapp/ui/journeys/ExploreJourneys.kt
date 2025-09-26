package com.wisme.firstapp.ui.journeys

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisme.firstapp.R
import com.wisme.firstapp.domain.JourneysDataClass
//import com.wisme.firstapp.domain.EpisodeDataClass
import com.wisme.firstapp.viewmodel.JourneyViewModel

@Composable
fun ExploreJourneys(
    viewModel: JourneyViewModel = hiltViewModel(),
    onJourneyClick: (JourneysDataClass) -> Unit = {}
) {
    val journeys by viewModel.journeys.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    ExploreJourneysContent(
        journeys = journeys,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onJourneyClick = onJourneyClick
    )
}

@Composable
fun ExploreJourneysContent(
    journeys: List<JourneysDataClass>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onJourneyClick: (JourneysDataClass) -> Unit = {}
) {
    Surface(
        color = Color.Black,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 39.dp, vertical = 85.dp)
        ) {
            Text(
                text = "Choose Your Journey",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(30.dp))

            // Show loading or error state
            if (isLoading) {
                Text(
                    text = "Loading journeys...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else if (errorMessage != null) {
                Column {
                    Text(
                        text = "Error: $errorMessage",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    Text(
                        text = "Showing sample journeys instead",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                items(journeys) { journey ->
                    JourneyItem(journey = journey, onJourneyClick = onJourneyClick)
                }
            }

            Surface(
                color = Color(0xFF353634),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "On request journey generation\nfor any topic coming soon",
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    style= MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 48.dp)
                )
            }
        }
    }
}

@Composable
fun JourneyItem(
    journey: JourneysDataClass,
    onJourneyClick: (JourneysDataClass) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        modifier = Modifier.clickable { onJourneyClick(journey) }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.sample_journey),
                contentDescription = journey.JourneyName,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = journey.JourneyName,
                    color = Color.White,
                    style= MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    modifier=Modifier.padding(top=5.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = journey.JourneyDescription,
                    color = Color.White,
                    style= MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Display journey duration and episode count
                Spacer(Modifier.height(4.dp))
                val durationText = if (journey.totalDurationMinutes > 0) {
                    "${journey.totalDurationMinutes} min"
                } else {
                    "-- min" // Placeholder until audio duration is fetched from backend
                }
                val episodeText = if (journey.episodes.isNotEmpty()) {
                    "${journey.episodes.size} episodes"
                } else {
                    "-- episodes"
                }
                Text(
                    text = "$durationText • $episodeText",
                    color = Color(0xFFC1FF72), // Light green accent
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(36.dp))

            Icon(
                painter = painterResource(R.drawable.arrow),
                contentDescription = "Go",
                modifier = Modifier.size(36.dp),
                tint = Color.Unspecified
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExploreJourneysPreview() {
    // For preview, we'll create sample journeys without ViewModel dependency
    val sampleJourneys = listOf(
        JourneysDataClass(
            JourneyName = "DSA / Cracking Coding Interviews", 
            JourneyDescription = "Help students crack coding interviews, from basics to advanced topics",
            JourneyImg = "journey1",
            totalDurationMinutes = 0, // Will be fetched dynamically from audio files
            episodes = listOf() // Empty for preview
        ),
        JourneysDataClass(
            JourneyName = "Personal Finance", 
            JourneyDescription = "Teach foundational personal finance and modern investment trends",
            JourneyImg = "journey2",
            totalDurationMinutes = 0,
            episodes = listOf()
        ),
        JourneysDataClass(
            JourneyName = "How to win Hackathons", 
            JourneyDescription = "Equip students with proven strategies to consistently perform and win hackathons",
            JourneyImg = "journey3",
            totalDurationMinutes = 0,
            episodes = listOf()
        )
    )
    
    ExploreJourneysContent(
        journeys = sampleJourneys,
        isLoading = false,
        errorMessage = null
    )
}
