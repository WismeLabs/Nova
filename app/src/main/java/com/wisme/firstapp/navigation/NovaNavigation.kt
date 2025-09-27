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
            println("PlayerScreen Navigation: Received parameters - journeyId='$journeyId', episodeId='$episodeId'")
            val playerViewModel: PlayerViewModel = hiltViewModel()
            
            // Look up the journey and episode data from JourneyViewModel
            val journeys by journeyViewModel.journeys.collectAsStateWithLifecycle()
            println("PlayerScreen Navigation: Available journeys count: ${journeys.size}")
            
            // Force data loading if journeys are empty
            LaunchedEffect(Unit) {
                if (journeys.isEmpty()) {
                    println("PlayerScreen Navigation: Journeys empty, triggering data load...")
                    journeyViewModel.loadJourneys()
                }
            }
            
            if (journeys.isEmpty()) {
                // Show loading screen while data is loading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFC1FF72))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading episode...",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
                return@composable
            }
            
            journeys.forEach { journey ->
                println("PlayerScreen Navigation: Available journey: '${journey.JourneyName}' with ${journey.episodes.size} episodes")
            }
            
            // More robust parsing and lookup
            val decodedJourneyId = try {
                journeyId.replace("_", " ")
            } catch (e: Exception) {
                println("PlayerScreen Navigation: Error decoding journeyId: $journeyId - ${e.message}")
                ""
            }
            println("PlayerScreen Navigation: Decoded journey ID: '$decodedJourneyId'")
            
            val journey = journeys.find { it.JourneyName.equals(decodedJourneyId, ignoreCase = true) }
            println("PlayerScreen Navigation: Found journey: ${journey?.JourneyName ?: "NOT FOUND"}")
            
            val episodeNumber = try {
                episodeId.removePrefix("episode_").toIntOrNull() ?: journey?.episodes?.firstOrNull()?.episodeNumber ?: 1
            } catch (e: Exception) {
                println("PlayerScreen Navigation: Error parsing episodeId: $episodeId - ${e.message}")
                1
            }
            println("PlayerScreen Navigation: Parsed episode number: $episodeNumber")
            
            val episode = journey?.episodes?.find { it.episodeNumber == episodeNumber }
            println("PlayerScreen Navigation: Found episode: ${episode?.title ?: "NOT FOUND"}")
            if (episode != null) {
                println("PlayerScreen Navigation: Episode audio URL: ${episode.audioUrl}")
            }
            
            if (journey != null && episode != null) {
                println("PlayerScreen Navigation: Rendering PlayerScreen component")
                PlayerScreen(
                    journey = journey,
                    episode = episode,
                    playerViewModel = playerViewModel,
                    journeyViewModel = journeyViewModel,
                    authPrefs = authViewModel.getAuthPreferences(),
                    onBackPress = {
                        // Go back to episodes page if possible, otherwise to journeys
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
            } else {
                // Handle error case - journey or episode not found
                println("PlayerScreen Navigation: ERROR - Journey or episode not found!")
                println("PlayerScreen Navigation: Journey found: ${journey != null}")
                println("PlayerScreen Navigation: Episode found: ${episode != null}")
                if (journey == null) {
                    println("PlayerScreen Navigation: Available journey names:")
                    journeys.forEach { j ->
                        println("PlayerScreen Navigation:   - '${j.JourneyName}'")
                    }
                }
                if (journey != null && episode == null) {
                    println("PlayerScreen Navigation: Available episodes in journey '${journey.JourneyName}':")
                    journey.episodes.forEach { ep ->
                        println("PlayerScreen Navigation:   - Episode ${ep.episodeNumber}: '${ep.title}'")
                    }
                }
                
                // Show error screen or navigate back
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "❌ Episode not found",
                            color = Color.Red,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Journey: ${journeyId} → ${decodedJourneyId}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Episode: ${episodeId} → ${episodeNumber}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }
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
