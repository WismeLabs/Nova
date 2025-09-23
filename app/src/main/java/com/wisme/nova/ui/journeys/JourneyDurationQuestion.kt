package com.wisme.nova.ui.journeys

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisme.nova.R
import com.wisme.nova.domain.JourneysDataClass

@Composable
fun JourneyDurationScreen(
    journey: JourneysDataClass,
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {

    val Gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFE4FFC2), Color(0xFFC1FF72))
    )

    Surface(
        color = Color.Black,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            //Back icon in top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter=painterResource(R.drawable.back_arrow),
                        contentDescription = "Back",
                        tint = Color.Unspecified
                    )
                }
            }
            Spacer(Modifier.height(30.dp))

            // Main content
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.sample_journey),
                        contentDescription = journey.JourneyName,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = journey.JourneyName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Personalizing your experience",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(Modifier.height(40.dp))
                Text(
                    text = "What episode length do you prefer?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFC1FF72),
                    lineHeight = 32.sp
                )

                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Choose what fits your schedule best.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(end = 100.dp)
                )

                Spacer(modifier = Modifier.height(35.dp))

                // Levels
                TimeOption(
                    icon = R.drawable.five_minutes,
                    title = "5 minutes",
                    description = "Quick, focused sessions that fit into busy schedules",
                    chipText = "Perfect for commutes",
                    chipTextColor = Color(0XFFC1FF72),
                    chipColor = Color(0XFF4a5c34)
                )

                TimeOption(
                    icon = R.drawable.seven_minutes,
                    title = "7 minutes",
                    description = "More comprehensive coverage with deeper explanations",
                    chipText = "Ideal for focus time",
                    chipTextColor = Color(0XFFFFCD6A),
                    chipColor = Color(0XFF5c5334)
                )
            }
            Spacer(Modifier.weight(1f))
            //Generate button
            Button(
                onClick = onContinueClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(49.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gradient, shape = RoundedCornerShape(30.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Generate My Episodes",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun TimeOption(icon: Int, title: String, description: String, chipText: String, chipTextColor: Color, chipColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(chipColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    painter = if(title=="5 minutes") painterResource(R.drawable.green_sparkles) else painterResource(R.drawable.brown_sparkles),
                    contentDescription = "Sparkles",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = chipText, color = chipTextColor, fontSize = 12.sp)
            }
            Text(
                text = description,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                textAlign = TextAlign.Left,
                modifier = Modifier.padding(end = 70.dp)
            )
        }
    }
}

@Preview
@Composable
fun JourneyDurationScreenPreview() {
    val sampleJourney = JourneysDataClass(
        JourneyName = "Data Structures and Algorithms",
        JourneyDescription = "",
        JourneyImg = ""
    )
    JourneyDurationScreen(journey = sampleJourney)
}
