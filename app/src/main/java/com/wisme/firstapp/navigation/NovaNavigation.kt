package com.wisme.firstapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.wisme.firstapp.viewmodel.AuthViewModel

@Composable
fun NovaNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
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
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.HOME) {
            HomeScreen(
                authPrefs = authViewModel.getAuthPreferences(),
                onNavigateToJourneys = {
                    navController.navigate(Routes.EXPLORE_JOURNEYS)
                }
            )
        }
        
        composable(Routes.EXPLORE_JOURNEYS) {
            com.wisme.firstapp.ui.journeys.ExploreJourneys(
                onJourneyClick = { journey ->
                    // Navigate to episodes screen with journey name as parameter
                    val journeyName = journey.JourneyName.replace(" ", "_").replace("/", "_")
                    navController.navigate("${Routes.EPISODES}/$journeyName")
                }
            )
        }
        
        composable("${Routes.EPISODES}/{journeyName}") { backStackEntry ->
            val journeyName = backStackEntry.arguments?.getString("journeyName") ?: ""
            val decodedJourneyName = journeyName.replace("_", " ")
            com.wisme.firstapp.ui.journeys.EpisodesPage(journeyName = decodedJourneyName)
        }
    }
}

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val PROFILE_DETAILS = "profile_details"
    const val HOME = "home"
    const val EXPLORE_JOURNEYS = "explore_journeys"
    const val EPISODES = "episodes"
}
