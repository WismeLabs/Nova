package com.wisme.firstapp.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisme.firstapp.R
import com.wisme.firstapp.ui.home.HomeScreen
import com.wisme.firstapp.ui.utils.ResponsiveFontSizes
import com.wisme.firstapp.ui.utils.ResponsiveSpacing

@Composable
fun Scaffold(
    onNavigateToHome: () -> Unit = {},
    onNavigateToJourneys: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {},
    currentRoute: String = "home",
    username: String = "User",
    avatarResId: Int = R.drawable.avatar_1,
    onAvatarClick: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit = {}
){
    Scaffold(
        containerColor = Color.Black,
        topBar = { 
            TopAppBar(
                username = username,
                avatarResId = avatarResId,
                onAvatarClick = onAvatarClick
            ) 
        },
        bottomBar = { 
            BottomBar(
                onNavigateToHome = onNavigateToHome,
                onNavigateToJourneys = onNavigateToJourneys,
                onNavigateToFeedback = onNavigateToFeedback,
                currentRoute = currentRoute
            ) 
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Composable
fun TopAppBar(
    username: String = "Username", // Default username, will be dynamic
    avatarResId: Int = R.drawable.avatar_1, // Default avatar, will be dynamic
    onAvatarClick: () -> Unit = {} // Navigation callback for avatar click
) {
    val lightGreen = Color(0xFFC1FF72)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 49.dp)
            .height(66.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(avatarResId),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .clickable { onAvatarClick() },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Welcome",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = ResponsiveFontSizes.body()
                )
                Text(
                    text = "$username!",
                    color = lightGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = ResponsiveFontSizes.headingLarge()
                )
            }
        }
    }
}

@Composable
fun BottomBar(
    onNavigateToHome: () -> Unit = {},
    onNavigateToJourneys: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {},
    currentRoute: String = "home"
) {
    NavigationBar(
        containerColor = Color(0xFF27272A),
        modifier = Modifier.height(90.dp)
    ) {
        NavigationBarItem(
            label={Text("Learn",style=MaterialTheme.typography.labelSmall,color=Color.White)},
            selected=currentRoute.contains("explore") || currentRoute.contains("episodes"),
            onClick=onNavigateToJourneys,
            icon={
                Icon(
                    painter = painterResource(R.drawable.learn),
                    contentDescription = "Book",
                    tint = if (currentRoute.contains("explore") || currentRoute.contains("episodes")) 
                        MaterialTheme.colorScheme.primary else Color.White
                )
            },
            modifier=Modifier.padding(top=10.dp),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            label={Text("Home",style=MaterialTheme.typography.labelSmall,color=Color.White)},
            selected=currentRoute == "home",
            onClick=onNavigateToHome,
            icon={
                Icon(
                    painter=painterResource(R.drawable.home_icon),
                    contentDescription="Home",
                    tint=if (currentRoute == "home") MaterialTheme.colorScheme.primary else Color.White
                )
            },
            modifier=Modifier.padding(top=10.dp),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            label = {Text("Feedback",style=MaterialTheme.typography.labelSmall,color=Color.White)},
            selected = currentRoute.contains("feedback"),
            onClick = onNavigateToFeedback,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.feedback),
                    contentDescription = "Feedback",
                    tint = if (currentRoute.contains("feedback")) 
                        MaterialTheme.colorScheme.primary else Color.White
                )
            },
            modifier=Modifier.padding(top=10.dp),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
    }
}

@Preview
@Composable
fun ScaffoldPreview() {
    Scaffold { paddingValues ->
        // Preview content using paddingValues to avoid lint warning
        Box(modifier = Modifier.padding(paddingValues))
    }
}
