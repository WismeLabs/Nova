package com.wisme.nova.ui.journeys

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.wisme.nova.R
import com.wisme.nova.domain.EpisodeDataClass
import com.wisme.nova.domain.JourneysDataClass

val darkBackground = Color(0xFF121212)
val textGreen = Color(0XFFC1FF72)
val olive = Color(0XFF4a5c34)

@Composable
fun EpisodesScreenContent(journey: JourneysDataClass, episodes: List<EpisodeDataClass>) {

    LazyColumn(
        modifier = Modifier
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
                    Image(
                        painter = painterResource(id = R.drawable.sample_journey), // Replace with JourneyImg
                        contentDescription = journey.JourneyName,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = journey.JourneyName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoChip(text = "Some Knowledge")
                            InfoChip(text = "8 min episodes")
                        }
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
                TextButton(onClick = { /* Start from beginning */ }) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", tint = Color(0xFFC1FF72), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Start from beginning", color = textGreen)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        //Episodes list
        items(episodes) { episode ->
            EpisodeItem(episode = episode)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EpisodeItem(episode: EpisodeDataClass) {
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
            onClick = { /* Play episode */ },
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
        JourneyImg = ""
    )

    val sampleEpisodes = listOf(
        EpisodeDataClass(1, "The Science of First Impressions", "How we form instant judgements and what influences first impressions"),
        EpisodeDataClass(2, "Why We Procrastinate", "Understanding the psychology behind procrastination and how to overcome it"),
        EpisodeDataClass(3, "Social Media and the Psychology", "How social media affects our behaviour and decision-making processes"),
        EpisodeDataClass(4, "The Psychology of Relationships", "Understand how we form bonds and maintain meaningful relationships")
    )

    EpisodesScreenContent(journey = sampleJourney, episodes = sampleEpisodes)
}