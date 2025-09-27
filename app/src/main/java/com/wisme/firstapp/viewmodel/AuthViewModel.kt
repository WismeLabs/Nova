package com.wisme.firstapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisme.firstapp.data.local.AuthPreferences
import com.wisme.firstapp.data.local.NavigationState
import com.wisme.firstapp.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.wisme.firstapp.data.repository.AuthRepository
import com.wisme.firstapp.data.repository.ConnectivityRepository
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for managing authentication state and user flow
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authPrefs: AuthPreferences,
    private val firebaseAuth: FirebaseAuth,
    private val authRepository: AuthRepository,
    private val connectivityRepository: ConnectivityRepository
) : ViewModel() {
    
    private val _navigationState = MutableStateFlow(authPrefs.getUserNavigationState())
    val navigationState = _navigationState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    
    private val _backendHealthy = MutableStateFlow(false)
    val backendHealthy = _backendHealthy.asStateFlow()
    
    private val _passwordResetSuccess = MutableStateFlow(false)
    val passwordResetSuccess = _passwordResetSuccess.asStateFlow()
    
    private val _passwordResetError = MutableStateFlow<String?>(null)
    val passwordResetError = _passwordResetError.asStateFlow()
    
    init {
        checkAuthState()
        checkBackendHealth()
    }
    
    /**
     * Check current authentication state and update navigation
     */
    private fun checkAuthState() {
        println("AuthViewModel: checkAuthState - isLoggedIn: ${authPrefs.isLoggedIn}, hasCompletedProfile: ${authPrefs.hasCompletedProfile}")
        val newState = authPrefs.getUserNavigationState()
        println("AuthViewModel: checkAuthState - setting navigation state to: $newState")
        _navigationState.value = newState
    }
    
    /**
     * Check if backend services are healthy
     */
    private fun checkBackendHealth() {
        Logger.logViewModel("AuthViewModel", "checkBackendHealth - Starting backend health check")
        
        viewModelScope.launch {
            try {
                val services = connectivityRepository.checkAllServices()
                val allHealthy = services.values.all { it.isHealthy }
                _backendHealthy.value = allHealthy
                
                if (!allHealthy) {
                    val unhealthyServices = services.filter { !it.value.isHealthy }
                    val errorMessage = "Backend services unavailable: ${unhealthyServices.keys.joinToString(", ")}"
                    // Don't set error message for production - allow Firebase auth to work independently
                    Logger.logViewModel(
                        viewModel = "AuthViewModel",
                        operation = "checkBackendHealth",
                        success = false,
                        errorMessage = errorMessage
                    )
                } else {
                    Logger.logViewModel(
                        viewModel = "AuthViewModel",
                        operation = "checkBackendHealth",
                        success = true
                    )
                }
            } catch (e: Exception) {
                _backendHealthy.value = false
                val errorMessage = "Cannot connect to backend services: ${e.message}"
                // Don't set error message for production - allow Firebase auth to work independently
                Logger.logViewModel(
                    viewModel = "AuthViewModel",
                    operation = "checkBackendHealth",
                    success = false,
                    errorMessage = errorMessage
                )
            }
        }
    }
    
    /**
     * Retry backend health check
     */
    fun retryBackendConnection() {
        _errorMessage.value = null
        checkBackendHealth()
    }
    
    /**
     * Mark onboarding as completed
     */
    fun completeOnboarding() {
        authPrefs.hasCompletedOnboarding = true
        authPrefs.isFirstLaunch = false
        checkAuthState()
    }
    
    /**
     * Handle Google Sign-In success
     */
    fun handleGoogleSignInSuccess(
        idToken: String,
        userId: String,
        email: String
    ) {
        println("AuthViewModel: handleGoogleSignInSuccess called for userId: $userId")
        Logger.logViewModel("AuthViewModel", "handleGoogleSignInSuccess - Starting Google sign-in process")
        Logger.logAuth("Google Sign-In Start", userId = userId)
        
        viewModelScope.launch {
            _isLoading.value = true
            println("AuthViewModel: handleGoogleSignInSuccess - storing auth data locally")
            try {
                // Store authentication data locally
                authPrefs.firebaseToken = idToken
                authPrefs.userId = userId
                authPrefs.userEmail = email
                authPrefs.isLoggedIn = true
                println("AuthViewModel: Auth data stored, isLoggedIn = ${authPrefs.isLoggedIn}")
                
                // Try to verify token with backend API (non-blocking)
                try {
                    // Send login info with provider ID first
                    val loginInfoResult = authRepository.sendLoginInfo(email)
                    if (loginInfoResult.isSuccess) {
                        println("AuthViewModel: Login info sent successfully with provider ID: wisme-mvpv1")
                    } else {
                        println("AuthViewModel: Login info failed: ${loginInfoResult.exceptionOrNull()?.message}")
                    }
                    
                    val verifyResult = authRepository.verifyToken(idToken)
                    if (verifyResult.isSuccess) {
                        // Sync profile data with backend
                        val syncResult = authRepository.syncProfileWithBackend(idToken)
                        // Profile completion status will be updated in syncProfileWithBackend
                    } else {
                        // Backend verification failed, but allow sign-in to proceed
                        Logger.logAuth(
                            event = "Backend Verification Failed",
                            userId = userId,
                            success = false,
                            errorMessage = "Backend verification failed but sign-in allowed: ${verifyResult.exceptionOrNull()?.message}"
                        )
                    }
                } catch (e: Exception) {
                    // Backend communication failed, but allow sign-in to proceed
                    Logger.logAuth(
                        event = "Backend Communication Failed",
                        userId = userId,
                        success = false,
                        errorMessage = "Backend communication failed but sign-in allowed: ${e.message}"
                    )
                }
                
                println("AuthViewModel: Calling checkAuthState")
                checkAuthState()
                println("AuthViewModel: Navigation state after checkAuthState: ${_navigationState.value}")
                if (_errorMessage.value == null) {
                    _errorMessage.value = null // Clear any previous errors
                }
                println("AuthViewModel: handleGoogleSignInSuccess completed successfully")
            } catch (e: Exception) {
                println("AuthViewModel: Exception in handleGoogleSignInSuccess: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Sign-in failed: ${e.message}"
                handleSignInFailure()
            } finally {
                println("AuthViewModel: handleGoogleSignInSuccess - setting loading to false")
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Handle email/password sign-in
     */
    fun signInWithEmailPassword(email: String, password: String) {
        println("AuthViewModel: signInWithEmailPassword called with email: $email")
        viewModelScope.launch {
            _isLoading.value = true
            println("AuthViewModel: Set loading to true")
            try {
                println("AuthViewModel: Calling Firebase signInWithEmailAndPassword")
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                println("AuthViewModel: Firebase auth result - user: ${user?.uid}")
                if (user != null) {
                    val idToken = user.getIdToken(false).await().token
                    println("AuthViewModel: Got ID token: ${idToken?.take(20)}...")
                    if (idToken != null) {
                        println("AuthViewModel: Calling handleGoogleSignInSuccess")
                        handleGoogleSignInSuccess(idToken, user.uid, user.email ?: email)
                    } else {
                        println("AuthViewModel: Failed to get ID token")
                        _errorMessage.value = "Failed to get authentication token"
                    }
                } else {
                    println("AuthViewModel: No user returned from Firebase")
                    _errorMessage.value = "Sign-in failed: No user returned"
                }
            } catch (e: Exception) {
                println("AuthViewModel: Exception in signInWithEmailPassword: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = getFirebaseErrorMessage(e, isSignUp = false)
            } finally {
                println("AuthViewModel: Setting loading to false")
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Handle email/password sign-up
     */
    fun signUpWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    val idToken = user.getIdToken(false).await().token
                    if (idToken != null) {
                        handleGoogleSignInSuccess(idToken, user.uid, user.email ?: email)
                    } else {
                        _errorMessage.value = "Failed to get authentication token"
                    }
                } else {
                    _errorMessage.value = "Sign-up failed: No user returned"
                }
            } catch (e: Exception) {
                _errorMessage.value = getFirebaseErrorMessage(e, isSignUp = true)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Handle sign-in failure
     */
    private fun handleSignInFailure() {
        authPrefs.clearUserData()
        checkAuthState()
    }
    
    /**
     * Handle Google Sign-In failure
     */
    fun handleGoogleSignInFailure(errorMessage: String) {
        _isLoading.value = false
        _errorMessage.value = errorMessage
        _navigationState.value = NavigationState.LOGIN
    }
    
    /**
     * Complete user profile setup
     */
    fun completeProfile(
        name: String,
        displayName: String,
        dateOfBirth: String,
        gender: String,
        profession: String,
        avatarId: Int
    ) {
        println("AuthViewModel: completeProfile called with name=$name, displayName=$displayName")
        viewModelScope.launch {
            _isLoading.value = true
            println("AuthViewModel: completeProfile - set loading to true")
            try {
                // Check backend connectivity first
                println("AuthViewModel: completeProfile - checking backend connectivity")
                val backendStatus = connectivityRepository.checkBackendHealth()
                if (!backendStatus.isHealthy) {
                    println("AuthViewModel: completeProfile - backend not healthy: ${backendStatus.message}")
                    _errorMessage.value = "Cannot connect to server: ${backendStatus.message}. Please check your connection and try again."
                    _isLoading.value = false
                    return@launch
                }
                println("AuthViewModel: completeProfile - backend is healthy, proceeding")
                
                // Store profile data locally first
                authPrefs.userName = name
                authPrefs.userDisplayName = displayName
                authPrefs.userDateOfBirth = dateOfBirth
                authPrefs.userGender = gender
                authPrefs.userProfession = profession
                authPrefs.userAvatarId = avatarId
                authPrefs.hasCompletedProfile = true
                
                // Sync with backend API - use existing token or get fresh one
                val currentUser = firebaseAuth.currentUser
                val existingToken = authPrefs.firebaseToken
                
                if (existingToken != null) {
                    // Try to get fresh token if currentUser is available, otherwise use existing token
                    val freshToken = if (currentUser != null) {
                        try {
                            currentUser.getIdToken(true).await().token
                        } catch (e: Exception) {
                            println("AuthViewModel: Failed to get fresh token, using existing token: ${e.message}")
                            existingToken
                        }
                    } else {
                        println("AuthViewModel: No current user, using existing token")
                        existingToken
                    }
                    
                    if (freshToken != null) {
                        println("AuthViewModel: Using token for profile creation")
                        // Update stored token if we got a fresh one
                        if (freshToken != existingToken) {
                            authPrefs.firebaseToken = freshToken
                        }
                        val result = authRepository.createUserProfile(
                            firebaseToken = freshToken,
                            avatarId = avatarId,
                            name = name,
                            displayName = displayName,
                            dateOfBirth = dateOfBirth,
                            gender = gender,
                            profession = profession
                        )
                        
                        if (!result.isSuccess) {
                            _errorMessage.value = "Profile sync with server failed, but your profile has been saved locally. You can continue using the app."
                            // Keep profile completed locally even if backend sync fails - for production readiness
                            // authPrefs.hasCompletedProfile remains true
                            println("AuthViewModel: Backend sync failed but keeping profile completed locally")
                        }
                    } else {
                        println("AuthViewModel: Failed to get fresh Firebase token")
                        _errorMessage.value = "Your profile has been saved locally. Server sync will happen when connection is restored."
                        // Keep profile completed locally even if token refresh fails
                        println("AuthViewModel: Token refresh failed but keeping profile completed locally")
                    }
                } else {
                    println("AuthViewModel: No authentication token available")
                    _errorMessage.value = "Your profile has been saved locally. Please sign in again to sync with server."
                    // Keep profile completed locally but note that server sync isn't available
                    println("AuthViewModel: No token available but keeping profile completed locally")
                }
                
                checkAuthState()
                if (_errorMessage.value == null) {
                    _errorMessage.value = null // Clear any previous errors
                }
            } catch (e: Exception) {
                _errorMessage.value = "Profile setup failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Send password reset email
     */
    fun sendPasswordResetEmail(email: String) {
        Logger.logAuth("Password Reset Request", userId = null)
        Logger.logViewModel("AuthViewModel", "sendPasswordResetEmail - Starting password reset")
        
        viewModelScope.launch {
            _isLoading.value = true
            _passwordResetError.value = null
            
            try {
                // Check backend connectivity first (optional for password reset)
                val backendStatus = connectivityRepository.checkBackendHealth()
                if (!backendStatus.isHealthy) {
                    Logger.logAuth(
                        event = "Password Reset",
                        success = false,
                        errorMessage = "Backend connectivity warning: ${backendStatus.message}"
                    )
                    // Continue with password reset even if backend is down, as this is handled by Firebase
                    Logger.d("Proceeding with password reset despite backend connectivity issues", "AUTH_VM")
                }
                
                // Send password reset email via Firebase
                firebaseAuth.sendPasswordResetEmail(email).await()
                
                // Success - show success state
                _passwordResetSuccess.value = true
                
                Logger.logAuth(
                    event = "Password Reset",
                    success = true
                )
                Logger.logViewModel(
                    viewModel = "AuthViewModel",
                    operation = "sendPasswordResetEmail",
                    success = true
                )
                
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("user-not-found", ignoreCase = true) == true -> 
                        "No account found with this email address"
                    e.message?.contains("invalid-email", ignoreCase = true) == true -> 
                        "Please enter a valid email address"
                    e.message?.contains("too-many-requests", ignoreCase = true) == true -> 
                        "Too many requests. Please try again later"
                    else -> "Failed to send reset email: ${e.message}"
                }
                
                _passwordResetError.value = errorMessage
                
                Logger.logAuth(
                    event = "Password Reset",
                    success = false,
                    errorMessage = errorMessage
                )
                Logger.logViewModel(
                    viewModel = "AuthViewModel",
                    operation = "sendPasswordResetEmail",
                    success = false,
                    errorMessage = errorMessage
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sign out user
     */
    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Sign out from Firebase
                firebaseAuth.signOut()
                
                // Clear local data
                authPrefs.clearUserData()
                checkAuthState()
            } catch (e: Exception) {
                _errorMessage.value = "Sign-out failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Convert Firebase error messages to user-friendly messages
     */
    private fun getFirebaseErrorMessage(exception: Exception, isSignUp: Boolean): String {
        val message = exception.message?.lowercase() ?: ""
        
        return when {
            message.contains("email-already-in-use") || message.contains("already in use") -> {
                if (isSignUp) {
                    "This email is already registered. Please try signing in instead, or use a different email address."
                } else {
                    "This email is already registered with a different sign-in method. Try signing in with Google."
                }
            }
            message.contains("weak-password") -> {
                "Password is too weak. Please use at least 6 characters with a mix of letters and numbers."
            }
            message.contains("invalid-email") -> {
                "Please enter a valid email address."
            }
            message.contains("user-not-found") -> {
                if (isSignUp) {
                    "Sign-up failed. Please try again."
                } else {
                    "No account found with this email. Please check your email or sign up for a new account."
                }
            }
            message.contains("wrong-password") -> {
                "Incorrect password. Please check your password and try again."
            }
            message.contains("too-many-requests") -> {
                "Too many failed attempts. Please wait a few minutes before trying again."
            }
            message.contains("network") || message.contains("connection") -> {
                "Network error. Please check your internet connection and try again."
            }
            message.contains("invalid-credential") -> {
                "Invalid email or password. Please check your credentials and try again."
            }
            else -> {
                if (isSignUp) {
                    "Sign-up failed. Please check your information and try again."
                } else {
                    "Sign-in failed. Please check your credentials and try again."
                }
            }
        }
    }
    
    /**
     * Clear password reset success state
     */
    fun clearPasswordResetSuccess() {
        _passwordResetSuccess.value = false
    }
    
    /**
     * Clear password reset error state
     */
    fun clearPasswordResetError() {
        _passwordResetError.value = null
    }
    
    /**
     * Update user profile data both locally and online
     */
    fun updateUserProfile(
        name: String,
        displayName: String,
        dateOfBirth: String,
        gender: String,
        profession: String,
        avatarId: Int
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Update local preferences immediately
                authPrefs.apply {
                    userName = name
                    userDisplayName = displayName
                    userDateOfBirth = dateOfBirth
                    userGender = gender
                    userProfession = profession
                    userAvatarId = avatarId
                }
                
                // Update online if connected and authenticated
                val firebaseToken = authPrefs.firebaseToken
                if (!firebaseToken.isNullOrBlank()) {
                    try {
                        // Backend now supports all profile fields
                        val result = authRepository.updateMyProfile(
                            firebaseToken = firebaseToken,
                            name = name,
                            displayName = displayName,
                            dateOfBirth = dateOfBirth,
                            gender = gender,
                            profession = profession,
                            avatarId = avatarId
                        )
                        
                        if (result.isFailure) {
                            Logger.e("Failed to update profile online: ${result.exceptionOrNull()?.message}", "AUTH_VM")
                            // Note: Keep local changes even if online update fails
                            _errorMessage.value = "Profile updated locally. Online sync will retry when connection improves."
                        } else {
                            Logger.d("Profile updated successfully online (all fields)", "AUTH_VM")
                            _errorMessage.value = null // Clear any previous errors
                        }
                    } catch (e: Exception) {
                        Logger.e("Error updating profile online: ${e.message}", "AUTH_VM")
                        _errorMessage.value = "Profile updated locally. Backend sync will retry when connection improves."
                    }
                } else {
                    Logger.d("Profile updated locally only (offline or not authenticated)", "AUTH_VM")
                }
                
            } catch (e: Exception) {
                Logger.e("Error updating profile: ${e.message}", "AUTH_VM")
                _errorMessage.value = "Failed to update profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get current user data
     */
    fun getCurrentUserData(): UserData? {
        return if (authPrefs.isLoggedIn) {
            UserData(
                userId = authPrefs.userId,
                email = authPrefs.userEmail,
                name = authPrefs.userName,
                displayName = authPrefs.userDisplayName,
                avatarId = authPrefs.userAvatarId,
                gender = authPrefs.userGender,
                profession = authPrefs.userProfession,
                dateOfBirth = authPrefs.userDateOfBirth
            )
        } else null
    }
    
    /**
     * Expose AuthPreferences for UI components that need direct access
     */
    fun getAuthPreferences(): AuthPreferences = authPrefs
}

/**
 * Data class for user information
 */
data class UserData(
    val userId: String?,
    val email: String?,
    val name: String?,
    val displayName: String?,
    val avatarId: Int,
    val gender: String?,
    val profession: String?,
    val dateOfBirth: String?
)
