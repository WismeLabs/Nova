package com.wisme.nova.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisme.nova.R

@Composable
fun HomeScreen(){
    Scaffold(
        containerColor = Color.Black,
        topBar = { TopAppBar() },
        bottomBar = { BottomBar() }
    ) {paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            Column(){
                TrendingTopicsRow(topics = listOf("Example topic", "DSA", "Exam. topics #2", "Topic #4", "Topic #5"))
                Spacer(modifier=Modifier.height(20.dp))
                Image(
                 painter = painterResource(R.drawable.wisme_home_intro),
                 contentDescription = "Wisme home intro",
                 modifier = Modifier.fillMaxWidth().height(250.dp)
             )
                UpcomingFeatures()
            }
        }
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
fun UpcomingFeatures(){
    Column(modifier = Modifier.fillMaxWidth().padding(start=12.dp,top=32.dp)) {
        Text(
            text = "Upcoming features",
            style =TextStyle(
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp),
            color = Color.White)

    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}