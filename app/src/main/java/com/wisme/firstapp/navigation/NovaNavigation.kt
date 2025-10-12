package com.wisme.firstapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wisme.firstapp.data.local.NavigationState
import com.wisme.firstapp.ui.home.HomeScreen
import com.wisme.firstapp.ui.login.LoginScreen
import com.wisme.firstapp.ui.onboard.OnboardingScreen
import com.wisme.firstapp.ui.profile_details.ProfileDetailsScreen
import com.wisme.firstapp.ui.signup.SignUpScreen
import com.wisme.firstapp.ui.splash.SplashScreen
import com.wisme.firstapp.ui.journeys.PlayerScreen
import com.wisme.firstapp.ui.topic.TopicRequestScreen
import com.wisme.firstapp.viewmodel.AuthViewModel
import com.wisme.firstapp.viewmodel.JourneyViewModel
import com.wisme.firstapp.viewmodel.FeedbackViewModel
import com.wisme.firstapp.ui.utils.ResponsiveTextStyles
import com.wisme.firstapp.ui.utils.ResponsiveFontSizes
import com.wisme.firstapp.ui.utils.ResponsiveSpacing
import com.wisme.firstapp.viewmodel.PlayerViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NovaNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    journeyViewModel: JourneyViewModel,
    onGoogleSignIn: () -> Unit = {}
) {
    val navigationState by authViewModel.navigationState.collectAsStateWithLifecycle()
    
    // Determine start destination based on navigation state
    val startDestination = when (navigationState) {
        NavigationState.FIRST_LAUNCH,
        NavigationState.ONBOARDING -> Routes.SPLASH
        NavigationState.LOGIN -> Routes.SPLASH
        NavigationState.PROFILE_SETUP -> Routes.SPLASH
        NavigationState.HOME -> Routes.SPLASH
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                authViewModel = authViewModel,
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Routes.PROFILE_DETAILS) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    authViewModel.completeOnboarding()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToSignUp = {
                    navController.navigate(Routes.SIGNUP)
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Routes.PROFILE_DETAILS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoogleSignIn = onGoogleSignIn,
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.SIGNUP) {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToProfileDetails = {
                    navController.navigate(Routes.PROFILE_DETAILS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoogleSignIn = onGoogleSignIn,
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.PROFILE_DETAILS) {
            ProfileDetailsScreen(
                authViewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PROFILE_DETAILS) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    // Go back to login screen safely
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable(Routes.HOME) {
            HomeScreen(
                authPrefs = authViewModel.getAuthPreferences(),
                journeyViewModel = journeyViewModel,
                onNavigateToJourneys = {
                    navController.navigate(Routes.EXPLORE_JOURNEYS)
                },
                onNavigateToPlayer = { journeyId, episodeId ->
                    navController.navigate("${Routes.PLAYER}/$journeyId/$episodeId")
                },
                onNavigateToFeedback = {
                    navController.navigate(Routes.FEEDBACK)
                },
                onNavigateToUserProfile = {
                    navController.navigate(Routes.USER_PROFILE)
                },
                onNavigateToTopicRequest = {
                    navController.navigate(Routes.TOPIC_REQUEST)
                }
            )
        }
        
        composable(Routes.EXPLORE_JOURNEYS) {
            com.wisme.firstapp.ui.journeys.ExploreJourneys(
                authPrefs = authViewModel.getAuthPreferences(),
                onJourneyClick = { journey ->
                    // Navigate to episodes screen with journey name as parameter
                    val journeyName = journey.JourneyName.replace(" ", "_").replace("/", "_")
                    navController.navigate("${Routes.EPISODES}/$journeyName")
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToFeedback = {
                    navController.navigate(Routes.FEEDBACK)
                },
                onProfileClick = {
                    navController.navigate(Routes.USER_PROFILE)
                }
            )
        }
        
        composable("${Routes.EPISODES}/{journeyName}") { backStackEntry ->
            val journeyName = backStackEntry.arguments?.getString("journeyName") ?: ""
            val decodedJourneyName = journeyName.replace("_", " ")
            com.wisme.firstapp.ui.journeys.EpisodesPage(
                journeyName = decodedJourneyName,
                onEpisodeClick = { journey, episode ->
                    println("NovaNavigation: onEpisodeClick called - Journey: ${journey.JourneyName}, Episode: ${episode.title}")
                    val journeyId = journey.JourneyName.replace(" ", "_").replace("/", "_")
                    val episodeId = "episode_${episode.episodeNumber}"
                    val playerRoute = "${Routes.PLAYER}/$journeyId/$episodeId"
                    println("NovaNavigation: Attempting to navigate to: $playerRoute")
                    try {
                        navController.navigate(playerRoute)
                        println("NovaNavigation: Successfully navigated to player screen")
                    } catch (e: Exception) {
                        println("NovaNavigation: Navigation failed: ${e.message}")
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToJourneys = {
                    navController.navigate(Routes.EXPLORE_JOURNEYS) {
                        popUpTo(Routes.EXPLORE_JOURNEYS) { inclusive = true }
                    }
                },
                onNavigateToFeedback = {
                    navController.navigate(Routes.FEEDBACK)
                }
            )
        }
        
        composable("${Routes.PLAYER}/{journeyId}/{episodeId}") { backStackEntry ->
            val journeyId = backStackEntry.arguments?.getString("journeyId") ?: ""
            val episodeId = backStackEntry.arguments?.getString("episodeId") ?: ""
            println("PlayerScreen Navigation: Simple approach - journeyId='$journeyId', episodeId='$episodeId'")
            val playerViewModel: PlayerViewModel = hiltViewModel()
            
            // Use basic journeys from /v1/journeys/ endpoint (already includes episodes with audio URLs)
            val journeys by journeyViewModel.journeys.collectAsStateWithLifecycle()
            
            // Load journeys if not already loaded
            LaunchedEffect(Unit) {
                if (journeys.isEmpty()) {
                    println("PlayerScreen Navigation: Loading basic journeys...")
                    journeyViewModel.loadJourneys()
                }
            }
            
            // Find journey from basic journeys list - match by database ID first, then display name
            val journey = journeys.find { 
                it.journeyId == journeyId ||
                it.JourneyName.equals(journeyId.replace("_", " "), ignoreCase = true)
            }
            
            if (journey == null) {
                // Show loading or error
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (journeys.isEmpty()) {
                            CircularProgressIndicator(color = Color(0xFFC1FF72))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading journeys...",
                                color = Color.White,
                                fontSize = ResponsiveFontSizes.body()
                            )
                        } else {
                            Text(
                                text = "Journey not found: $journeyId",
                                color = Color.White,
                                fontSize = ResponsiveFontSizes.body()
                            )
                        }
                    }
                }
                return@composable
            }
            
            // Parse episode number
            val episodeNumber = episodeId.removePrefix("episode_").toIntOrNull() ?: 1
            val episode = journey.episodes.find { it.episodeNumber == episodeNumber }
            
            if (episode == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Episode not found: $episodeNumber",
                        color = Color.White,
                        fontSize = ResponsiveFontSizes.body()
                    )
                }
                return@composable
            }
            
            println("PlayerScreen Navigation: Found journey='${journey.JourneyName}' episode='${episode.title}'")
            
            PlayerScreen(
                journey = journey,
                episode = episode,
                playerViewModel = playerViewModel,
                journeyViewModel = journeyViewModel,
                authPrefs = authViewModel.getAuthPreferences(),
                onBackPress = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.EXPLORE_JOURNEYS) {
                            popUpTo(Routes.HOME) { inclusive = false }
                        }
                    }
                },
                onProfileClick = {
                    navController.navigate(Routes.USER_PROFILE)
                }
            )
        }
        
        composable(Routes.FEEDBACK) {
            val feedbackViewModel: FeedbackViewModel = hiltViewModel()
            
            com.wisme.firstapp.ui.feedback.FeedbackScreen(
                authPrefs = authViewModel.getAuthPreferences(),
                feedbackViewModel = feedbackViewModel,
                onEpisodeFeedbackClick = {
                    // Set the current episode for feedback before navigating
                    val episodeId = feedbackViewModel.getAvailableEpisodeForFeedback()
                    if (episodeId != null) {
                        feedbackViewModel.loadEpisodeFeedbackQuestions()
                        feedbackViewModel.loadEpisodePreviousResponses(episodeId)
                        navController.navigate(Routes.EPISODE_FEEDBACK)
                    }
                },
                onJourneyFeedbackClick = {
                    // Set the current journey for feedback before navigating
                    val journeyId = feedbackViewModel.getAvailableJourneyForFeedback()
                    if (journeyId != null) {
                        feedbackViewModel.loadJourneyFeedbackQuestions()
                        feedbackViewModel.loadJourneyPreviousResponses(journeyId)
                        navController.navigate(Routes.JOURNEY_FEEDBACK)
                    }
                },
                onGeneralFeedbackClick = {
                    feedbackViewModel.loadGeneralFeedbackQuestions()
                    feedbackViewModel.loadGeneralPreviousResponses()
                    navController.navigate(Routes.GENERAL_FEEDBACK)
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToJourneys = {
                    navController.navigate(Routes.EXPLORE_JOURNEYS)
                }
            )
        }
        
        composable(Routes.EPISODE_FEEDBACK) {
            val feedbackViewModel: FeedbackViewModel = hiltViewModel()
            val currentEpisode by feedbackViewModel.currentFeedbackEpisode.collectAsStateWithLifecycle()
            
            com.wisme.firstapp.ui.feedback.EpisodeFeedbackScreen(
                authPrefs = authViewModel.getAuthPreferences(),
                feedbackViewModel = feedbackViewModel,
                episodeId = currentEpisode ?: "episode_1",
                episodeTitle = currentEpisode ?: "Episode",
                onBackClick = {
                    // Go back to feedback screen
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.FEEDBACK)
                    }
                },
                onSubmitFeedback = { feedbackData ->
                    // Submission is now handled in the screen via ViewModel
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.FEEDBACK)
                    }
                },
                onSkip = {
                    feedbackViewModel.skipEpisodeFeedback()
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.FEEDBACK)
                    }
                }
            )
        }
        
        composable(Routes.JOURNEY_FEEDBACK) {
            val feedbackViewModel: FeedbackViewModel = hiltViewModel()
            val currentJourney by feedbackViewModel.currentFeedbackJourney.collectAsStateWithLifecycle()
            
            com.wisme.firstapp.ui.feedback.JourneyFeedbackScreen(
                authPrefs = authViewModel.getAuthPreferences(),
                journeyId = currentJourney ?: "journey_1",
                journeyTitle = currentJourney ?: "Journey",
                onBackClick = {
                    // Go back to feedback screen
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.FEEDBACK)
                    }
                },
                onFeedbackSubmitted = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.FEEDBACK)
                    }
                },
                onSkip = {
                    feedbackViewModel.skipJourneyFeedback()
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.FEEDBACK)
                    }
                }
            )
        }
        
        composable(Routes.GENERAL_FEEDBACK) {
            val feedbackViewModel: FeedbackViewModel = hiltViewModel()
            
            com.wisme.firstapp.ui.feedback.GeneralFeedbackScreen(
                authPrefs = authViewModel.getAuthPreferences(),
                onBackClick = {
                    // Go back to feedback screen
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.FEEDBACK)
                    }
                },
                onFeedbackSubmitted = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.FEEDBACK)
                    }
                },
                onSkip = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.FEEDBACK)
                    }
                }
            )
        }
        
        composable(Routes.USER_PROFILE) {
            com.wisme.firstapp.ui.userprofile.UserProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    // Go back to home screen
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.TOPIC_REQUEST) {
            TopicRequestScreen(
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                },
                onSubmitTopic = { topic ->
                    // TODO: Implement topic submission to backend
                    // This will call the repository method when it's implemented
                }
            )
        }
    }
}

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val PROFILE_DETAILS = "profile_details"
    const val USER_PROFILE = "user_profile"
    const val HOME = "home"
    const val EXPLORE_JOURNEYS = "explore_journeys"
    const val EPISODES = "episodes"
    const val PLAYER = "player"
    const val FEEDBACK = "feedback"
    const val EPISODE_FEEDBACK = "episode_feedback"
    const val JOURNEY_FEEDBACK = "journey_feedback"
    const val GENERAL_FEEDBACK = "general_feedback"
    const val TOPIC_REQUEST = "topic_request"
}
