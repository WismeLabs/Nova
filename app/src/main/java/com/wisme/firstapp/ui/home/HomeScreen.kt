package com.wisme.firstapp.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import com.wisme.firstapp.R
import com.wisme.firstapp.ui.common.TopAppBar
import com.wisme.firstapp.data.local.AuthPreferences
import androidx.compose.ui.platform.LocalContext


@Composable
fun HomeScreen(
    authPrefs: AuthPreferences,
    onNavigateToJourneys: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    // Get user data from preferences
    val userName = remember { authPrefs.userDisplayName ?: authPrefs.userName ?: "Welcome" }
    val userAvatarId = remember { authPrefs.userAvatarId }
    
    // Map avatar ID to drawable resource
    val avatarResource = remember(userAvatarId) {
        when (userAvatarId) {
            1 -> R.drawable.avatar_1
            2 -> R.drawable.avatar_2
            3 -> R.drawable.avatar_3
            4 -> R.drawable.avatar_4
            5 -> R.drawable.avatar_5
            else -> R.drawable.avatar_1 // Default fallback
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Dark background to match app theme
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar with dynamic user data
            TopAppBar(
                username = userName,
                avatarResId = avatarResource
            )
            
            // Scrollable content
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 180.dp) // Space for resume section (80dp) + navbar (90dp) + padding
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            
            item {
                // Category Chips Section
                CategoryChipsSection()
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            item {
                // Main Welcome Image
                MainWelcomeImageSection()
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            item {
                // Upcoming Features Section
                UpcomingFeaturesSection()
            }
            
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        // Fixed sections at bottom (Resume + Navigation)
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ResumeLearningSection()
            BottomNavigationBar(onNavigateToJourneys = onNavigateToJourneys)
        }
    }
}

@Composable
fun CategoryChipsSection() {
    // Random topics that will be fetched from backend later
    val topics = listOf(
        "Machine Learning", "Web Development", "Data Science", "Mobile Apps", 
        "AI & ChatGPT", "Blockchain", "Cloud Computing", "Cybersecurity",
        "Game Development", "UI/UX Design", "Digital Marketing", "Photography"
    )
    
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(
            text = "What users want to learn?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(topics) { index, topic ->
                Surface(
                    shape = RoundedCornerShape(25.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text(
                        text = topic,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MainWelcomeImageSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.home_welcome),
                contentDescription = "Welcome to Aura",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun UpcomingFeaturesSection() {
    val lightGreen = Color(0xFFC1FF72)
    
    val features = listOf(
        Triple(R.drawable.home_feat1, "Generate Journeys", "for any topic"),
        Triple(R.drawable.home_feat2, "Conversational A.I.", "Buddy"),
        Triple(R.drawable.home_feat3, "Enhanced", "personalization")
    )
    
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(features) { index, (imageRes, title, subtitle) ->
                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .height(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background Image
                        Image(
                            painter = painterResource(imageRes),
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Overlay with text
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = title,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = subtitle,
                                    color = lightGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResumeLearningSection(modifier: Modifier = Modifier) {
    val lightGreen = Color(0xFFC1FF72)
    val darkGreen = Color(0xFF1A241F)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Episode thumbnail/icon
            Card(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = darkGreen)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎧",
                        fontSize = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Episode info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Continue Learning",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "DSA & Coding Interviews", // This will be dynamic
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Arrays and Strings", // This will be dynamic
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            // Play button
            Card(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = lightGreen)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TrendingTopicsRow(topics: List<String>){
    Column(modifier = Modifier.fillMaxWidth().padding(start=20.dp,top=32.dp)) {
        Text(
            text = "What users want to learn",
            style =TextStyle(
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp),
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
                    color =Color.Transparent,
                    border =BorderStroke(1.dp, Color.White)
                ) {
                    Text(
                        text = topic,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color =Color.White,
                        style =TextStyle(
                            fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(onNavigateToJourneys: () -> Unit = {}) {
    val lightGreen = Color(0xFFC1FF72)
    
    NavigationBar(
        containerColor = Color(0xFF27272A),
        modifier = Modifier.height(90.dp)
    ) {
        NavigationBarItem(
            label = { 
                Text(
                    "Learn",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            },
            selected = false,
            onClick = { onNavigateToJourneys() },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.learn),
                    contentDescription = "Book",
                    tint = Color.White
                )
            },
            modifier = Modifier.padding(top = 10.dp),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            label = { 
                Text(
                    "Home",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            },
            selected = true,
            onClick = { /* TODO */ },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.home_icon),
                    contentDescription = "Home",
                    tint = lightGreen // Using app theme color for selected item
                )
            },
            modifier = Modifier.padding(top = 10.dp),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            label = { 
                Text(
                    "Feedback",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            },
            selected = false,
            onClick = { /* TODO */ },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.feedback),
                    contentDescription = "Feedback",
                    tint = Color.White
                )
            },
            modifier = Modifier.padding(top = 10.dp),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HomeScreenPreview() {
    val context = LocalContext.current
    // Create AuthPreferences for preview with default values
    val previewAuthPrefs = AuthPreferences(context)
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        HomeScreen(
            authPrefs = previewAuthPrefs,
            onNavigateToJourneys = { /* Preview navigation */ }
        )
    }
}
