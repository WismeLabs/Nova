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
        _navigationState.value = authPrefs.getUserNavigationState()
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
                    _errorMessage.value = errorMessage
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
                _errorMessage.value = errorMessage
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
        Logger.logViewModel("AuthViewModel", "handleGoogleSignInSuccess - Starting Google sign-in process")
        Logger.logAuth("Google Sign-In Start", userId = userId)
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Check backend connectivity first
                val backendStatus = connectivityRepository.checkBackendHealth()
                if (!backendStatus.isHealthy) {
                    val errorMessage = "Cannot connect to server: ${backendStatus.message}. Please check your connection and try again."
                    _errorMessage.value = errorMessage
                    Logger.logAuth(
                        event = "Google Sign-In",
                        userId = userId,
                        success = false,
                        errorMessage = "Backend connectivity failed: ${backendStatus.message}"
                    )
                    Logger.logViewModel(
                        viewModel = "AuthViewModel",
                        operation = "handleGoogleSignInSuccess",
                        success = false,
                        errorMessage = errorMessage
                    )
                    _isLoading.value = false
                    return@launch
                }
                
                // Store authentication data locally
                authPrefs.firebaseToken = idToken
                authPrefs.userId = userId
                authPrefs.userEmail = email
                authPrefs.isLoggedIn = true
                
                // Verify token with backend API
                val verifyResult = authRepository.verifyToken(idToken)
                if (verifyResult.isSuccess) {
                    // Sync profile data with backend
                    val syncResult = authRepository.syncProfileWithBackend(idToken)
                    // Profile completion status will be updated in syncProfileWithBackend
                } else {
                    _errorMessage.value = "Backend verification failed: ${verifyResult.exceptionOrNull()?.message}"
                }
                
                checkAuthState()
                if (_errorMessage.value == null) {
                    _errorMessage.value = null // Clear any previous errors
                }
            } catch (e: Exception) {
                _errorMessage.value = "Sign-in failed: ${e.message}"
                handleSignInFailure()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Handle email/password sign-in
     */
    fun signInWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Check backend connectivity first
                val backendStatus = connectivityRepository.checkBackendHealth()
                if (!backendStatus.isHealthy) {
                    _errorMessage.value = "Cannot connect to server: ${backendStatus.message}. Please check your connection and try again."
                    _isLoading.value = false
                    return@launch
                }
                
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    val idToken = user.getIdToken(false).await().token
                    if (idToken != null) {
                        handleGoogleSignInSuccess(idToken, user.uid, user.email ?: email)
                    } else {
                        _errorMessage.value = "Failed to get authentication token"
                    }
                } else {
                    _errorMessage.value = "Sign-in failed: No user returned"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Sign-in failed: ${e.message}"
            } finally {
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
                // Check backend connectivity first
                val backendStatus = connectivityRepository.checkBackendHealth()
                if (!backendStatus.isHealthy) {
                    _errorMessage.value = "Cannot connect to server: ${backendStatus.message}. Please check your connection and try again."
                    _isLoading.value = false
                    return@launch
                }
                
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
                _errorMessage.value = "Sign-up failed: ${e.message}"
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
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Check backend connectivity first
                val backendStatus = connectivityRepository.checkBackendHealth()
                if (!backendStatus.isHealthy) {
                    _errorMessage.value = "Cannot connect to server: ${backendStatus.message}. Please check your connection and try again."
                    _isLoading.value = false
                    return@launch
                }
                
                // Store profile data locally first
                authPrefs.userName = name
                authPrefs.userDisplayName = displayName
                authPrefs.userDateOfBirth = dateOfBirth
                authPrefs.userGender = gender
                authPrefs.userProfession = profession
                authPrefs.userAvatarId = avatarId
                authPrefs.hasCompletedProfile = true
                
                // Sync with backend API
                val firebaseToken = authPrefs.firebaseToken
                if (firebaseToken != null) {
                    val result = authRepository.createUserProfile(
                        firebaseToken = firebaseToken,
                        avatarId = avatarId,
                        name = name,
                        displayName = displayName,
                        dateOfBirth = dateOfBirth,
                        gender = gender,
                        profession = profession
                    )
                    
                    if (!result.isSuccess) {
                        _errorMessage.value = "Profile sync failed: ${result.exceptionOrNull()?.message}"
                        // Revert local changes if backend sync fails
                        authPrefs.hasCompletedProfile = false
                    }
                } else {
                    _errorMessage.value = "No authentication token found"
                    authPrefs.hasCompletedProfile = false
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
