package com.wisme.nova.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisme.nova.R
import com.wisme.nova.data.JourneysDataClass

@Composable
fun ExploreJourneys() {
    val sampleJourneys = listOf(
        JourneysDataClass("Psychology & Human Behavior", "Discover the fascinating world of human psychology...", "journey1"),
        JourneysDataClass("Psychology & Human Behavior", "Discover the fascinating world of human psychology...", "journey2"),
        JourneysDataClass("Psychology & Human Behavior", "Discover the fascinating world of human psychology...", "journey3"),
        JourneysDataClass("Psychology & Human Behavior", "Discover the fascinating world of human psychology...", "journey4"),
    )

    Surface(
        color = Color.Black,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Choose Your Journey",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(sampleJourneys) { journey ->
                    JourneyItem(journey)
                }
            }

            // Bottom info box
            Surface(
                color = Color(0xFF333333),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "On request journey generation\nfor any topic coming soon",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun JourneyItem(journey: JourneysDataClass) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Journey image
            Image(
                painter = painterResource(R.drawable.sample_journey), // replace with journey.JourneyImg
                contentDescription = journey.JourneyName,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Title & description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = journey.JourneyName,
                    color = Color.White,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = journey.JourneyDescription,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Arrow icon
            Icon(
                painter = painterResource(R.drawable.arrow), // add your arrow icon
                contentDescription = "Go",
                tint = Color(0xFFB6FF69),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExploreJourneysPreview() {
    ExploreJourneys()
}
