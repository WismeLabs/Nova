package com.wisme.firstapp.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wisme.firstapp.R
import com.wisme.firstapp.data.local.NavigationState
import com.wisme.firstapp.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

/**
 * Splash screen that handles app initialization and navigation logic
 */
@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfileSetup: () -> Unit,
    onNavigateToHome: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val navigationState by authViewModel.navigationState.collectAsStateWithLifecycle()
    var showSplash by remember { mutableStateOf(true) }
    
    // Show splash for minimum duration
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        showSplash = false
    }
    
    // Navigate based on auth state after splash
    LaunchedEffect(showSplash, navigationState) {
        if (!showSplash) {
            when (navigationState) {
                NavigationState.FIRST_LAUNCH,
                NavigationState.ONBOARDING -> onNavigateToOnboarding()
                NavigationState.LOGIN -> onNavigateToLogin()
                NavigationState.PROFILE_SETUP -> onNavigateToProfileSetup()
                NavigationState.HOME -> onNavigateToHome()
            }
        }
    }
    
    if (showSplash) {
        SplashContent()
    }
}

@Composable
private fun SplashContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo or icon
            Image(
                painter = painterResource(id = R.drawable.splash_icon),
                contentDescription = "Nova Logo",
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App name
            Text(
                text = "Wisme",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = "Learn. Grow. Explore.",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading indicator (optional)
            LoadingDots()
        }
    }
}

@Composable
private fun LoadingDots() {
    val dots = listOf("●", "●", "●")
    var currentDot by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            currentDot = (currentDot + 1) % dots.size
        }
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dots.forEachIndexed { index, dot ->
            Text(
                text = dot,
                color = if (index == currentDot) MaterialTheme.colorScheme.primary else Color.Gray,
                fontSize = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashContent()
}

private const val SPLASH_DURATION_MS = 2000L
