package com.wisme.nova.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisme.nova.R

@Composable
fun HomeScreen(modifier:Modifier=Modifier){
    Box(modifier = modifier.fillMaxSize()){
        Column{
            TrendingTopicsRow(topics = listOf("Example topic", "DSA", "Exam. topics #2", "Topic #4", "Topic #5"))
            Spacer(modifier=Modifier.height(20.dp))
            Image(
                painter = painterResource(R.drawable.wisme_home_intro),
                contentDescription = "Wisme home intro",
                modifier = Modifier.fillMaxWidth().height(250.dp))
            UpcomingFeatures()
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