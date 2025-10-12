package com.wisme.research.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisme.research.data.api.UserProfile
import com.wisme.research.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "UserProfileViewModel"
    }
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        // Only fetch fresh data from backend, don't pre-populate with cache
        refreshProfile()
    }
    
    /**
     * Load user profile from cache
     */
    private fun loadCachedProfile() {
        val cachedProfile = userRepository.getCachedProfile()
        if (cachedProfile != null) {
            _userProfile.value = cachedProfile
            Log.d(TAG, "Loaded CACHED profile: ${cachedProfile.display_name}")
        } else {
            Log.d(TAG, "No cached profile found")
        }
    }
    
    /**
     * Refresh user profile from backend
     */
    fun refreshProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            userRepository.getMyProfile()
                .onSuccess { profile ->
                    _userProfile.value = profile
                    Log.d(TAG, "Profile refreshed with FRESH API data: ${profile.display_name}")
                }
                .onFailure { error ->
                    val errorMsg = "Failed to refresh profile: ${error.message}"
                    _errorMessage.value = errorMsg
                    Log.e(TAG, errorMsg, error)
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Get display name for UI components
     */
    fun getDisplayName(): String {
        return _userProfile.value?.display_name ?: userRepository.getDisplayName()
    }
    
    /**
     * Get user name for UI components
     */
    fun getUserName(): String {
        return _userProfile.value?.name ?: userRepository.getUserName()
    }
    
    /**
     * Get avatar ID for UI components
     */
    fun getAvatarId(): Int {
        return _userProfile.value?.avatar_id ?: userRepository.getAvatarId()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Force refresh profile after update
     */
    fun forceRefresh() {
        refreshProfile()
    }
}
