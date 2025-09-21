package com.wisme.nova.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ExploreJourneys(){

    Column(Modifier.fillMaxSize()){
        Text(
            text="Choose your journey",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview
@Composable
fun ExploreJourneysPreview(showBackground: Boolean = true){
    ExploreJourneys()
}