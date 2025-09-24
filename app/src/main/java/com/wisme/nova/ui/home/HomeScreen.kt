package com.wisme.nova.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wisme.nova.R

data class Feature(val imageRes: Int, val description: String)

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Column {
            TrendingTopicsRow(topics = listOf("Example topic", "DSA", "Exam. topics #2", "Topic #4", "Topic #5"))
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(R.drawable.wisme_home_intro),
                contentDescription = "Wisme home intro",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
            UpcomingFeatures(
                features = listOf(
                    Feature(R.drawable.feature_1, "Feature 1: AI-powered explanations"),
                    Feature(R.drawable.feature_2, "Feature 2: Collaborative learning"),
                    Feature(R.drawable.feature_3, "Feature 3: Personalized study plans")
                )
            )
        }
    }
}

@Composable
fun TrendingTopicsRow(topics: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 32.dp)
    ) {
        Text(
            text = "What users want to learn",
            style = TextStyle(
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp
            ),
            color = Color.White,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        val infiniteList = generateSequence { topics }.flatten().take(1000).toList()
        LazyRow(
            contentPadding = PaddingValues(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(infiniteList) { index, topic ->

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text(
                        text = topic,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        style = TextStyle(
                            fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingFeatures(features: List<Feature>) {
    var selectedFeature by remember { mutableStateOf<Feature?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 32.dp)
    ) {
        Text(
            text = "Upcoming features",
            style = TextStyle(
                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp
            ),
            color = Color.White
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(features) { index, feature ->
                Image(
                    painter = painterResource(feature.imageRes),
                    contentDescription = "Upcoming feature",
                    modifier = Modifier.clickable{selectedFeature = feature}
                        .size(180.dp)
                )
            }
        }
    }

    selectedFeature?.let { feature ->
        FeaturePopup(feature = feature, onDismiss = { selectedFeature = null })
    }
}

@Composable
fun FeaturePopup(feature: Feature, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(text = feature.description)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}