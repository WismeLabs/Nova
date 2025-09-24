package com.wisme.nova.ui.common

import ProfileScreen
import androidx.compose.foundation.Image
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
import com.wisme.nova.R
import com.wisme.nova.ui.home.HomeScreen
import com.wisme.nova.ui.home.TrendingTopicsRow
import com.wisme.nova.ui.home.UpcomingFeatures

@Composable
fun Scaffold(){
    Scaffold(
        containerColor = Color.Black,
        topBar = { TopAppBar() },
        bottomBar = { BottomBar() }
    ) {paddingValues ->
        HomeScreen(modifier=Modifier.padding(paddingValues))
    }
}

@Composable
fun TopAppBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth().padding(start=20.dp,top=49.dp)
            .height(66.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.avatar),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Welcome Lorem!",
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = "Explore Podcasts",
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 19.sp
                    )
                )
            }
        }
    }
}

@Composable
fun BottomBar() {
    NavigationBar(
        containerColor = Color(0xFF27272A),
        modifier = Modifier.height(90.dp)
    ) {
        NavigationBarItem(
            label={Text("Learn",style=MaterialTheme.typography.labelSmall,color=Color.White)},
            selected=false,
            onClick={ /* TODO */ },
            icon={
                Icon(
                    painter = painterResource(R.drawable.learn),
                    contentDescription = "Book",
                    tint = Color.White
                )
            },
            modifier=Modifier.padding(top=10.dp),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            label={Text("Home",style=MaterialTheme.typography.labelSmall,color=Color.White)},
            selected=true,
            onClick={ /* TODO */ },
            icon={
                Icon(
                    painter=painterResource(R.drawable.home_icon),
                    contentDescription="Home",
                    tint=MaterialTheme.colorScheme.primary
                )
            },
            modifier=Modifier.padding(top=10.dp),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            label = {Text("Feedback",style=MaterialTheme.typography.labelSmall,color=Color.White)},
            selected = false,
            onClick = { /* TODO */ },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.feedback),
                    contentDescription = "Feedback",
                    tint = Color.White
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
    Scaffold()
}
