# Nova App - Development Progress

## Overview
Nova is an Android meditation/wellness app built with Kotlin, Jetpack Compose, and Firebase integration.

**Package Name**: `com.wisme.firstapp`  
**Build Status**: ✅ Successfully compiling  
**Firebase**: ✅ Integrated and configured  

---

## Authentication System

### ✅ Implemented Features
- **Google Sign-In** (Firebase Auth)
- **Email/Password Sign-In** (Firebase Auth)
- **Email/Password Sign-Up** (Firebase Auth)
- **Automatic token verification** with backend
- **Profile sync** between local storage and backend
- **Secure logout** with data cleanup

### Data Persistence
- **Local**: SharedPreferences (`AuthPreferences` class)
- **Backend**: Full API integration with Aura server
- **Offline Support**: App works without internet, syncs when online

---

## Screen Flow & Routing

### Navigation Logic (`NovaNavigation.kt`)
```
Splash Screen → Determines user state → Routes to appropriate screen
```

### User Journey States
1. **First Launch** → Onboarding → Login → Profile Setup → Home
2. **Returning User** → Skip to current progress point
3. **Logged In User** → Direct to Home

### Implemented Screens
- ✅ **SplashScreen**: App initialization, routing logic
- ✅ **OnboardingScreen**: First-time user introduction (shows once)
- ✅ **LoginScreen**: Email/Password + Google Sign-In
- ✅ **SignUpScreen**: User registration
- ✅ **ProfileDetailsScreen**: User profile completion
- ✅ **HomeScreen**: Main app interface
- ✅ **UserProfile**: User profile management
- ✅ **ExploreJourneys**: Content discovery
- ✅ **PlayerScreen**: Audio playback interface

---

## Backend Integration (Aura API)

### API Configuration
- **Base URL**: Configured in `AuraApiService.kt`
- **Authentication**: Bearer token (Firebase ID token)
- **HTTP Client**: Retrofit + OkHttp with logging

### API Endpoints Integrated
| Endpoint | Method | Purpose | Status |
|----------|--------|---------|--------|
| `/verify-token` | POST | Verify Firebase token | ✅ |
| `/create-user-profile` | POST | Create user profile | ✅ |
| `/my-profile` | GET | Get user profile | ✅ |
| `/my-profile` | PUT | Update user profile | ✅ |
| `/auth/status` | GET | Check auth status | ✅ |

### Data Models
- `VerifyTokenRequest/Response`
- `CreateUserProfileRequest/Response`
- `UserProfileResponse`
- `UpdateUserProfileRequest`
- `AuthStatusResponse`

---

## State Management

### Local Storage (AuthPreferences)
```kotlin
// Authentication
- isLoggedIn: Boolean
- firebaseToken: String
- userId: String
- userEmail: String

// User Flow
- hasCompletedOnboarding: Boolean
- hasCompletedProfile: Boolean
- isFirstLaunch: Boolean

// Profile Data
- userName, userDisplayName, userGender
- userProfession, userDateOfBirth
- userAvatarId: Int
```

### Navigation States
- `FIRST_LAUNCH`: Show onboarding
- `ONBOARDING`: Onboarding in progress
- `LOGIN`: User needs to authenticate
- `PROFILE_SETUP`: Profile completion required
- `HOME`: Fully authenticated and set up

---

## Technical Architecture

### Dependencies & Tools
- **UI**: Jetpack Compose + Material 3
- **Navigation**: Navigation Compose
- **DI**: Hilt (Dependency Injection)
- **Auth**: Firebase Authentication
- **Network**: Retrofit + OkHttp
- **Local Storage**: SharedPreferences
- **Architecture**: MVVM + Clean Architecture

### Key Classes
- `AuthViewModel`: Authentication state management
- `AuthRepository`: Backend API communication
- `AuthPreferences`: Local data persistence
- `AuraApiService`: API interface definitions
- `NovaNavigation`: App navigation logic

---

## Current Status

### ✅ Completed
- Complete authentication flow (Google + Email/Password)
- Onboarding system (shows only once)
- Local and remote data persistence
- Backend API integration
- Navigation routing logic
- Profile management system
- Firebase integration
- Package name migration (nova → firstapp)

### ✅ Recent Fixes
- Fixed ProfileDetails preview NullPointerException by separating ViewModel-dependent logic from preview content
- Created preview-friendly version of ProfileDetailsScreen for design testing
- **Added Calendar Date Picker**: Calendar icon now opens date picker dialog for Date of Birth field
- **Enhanced Form Validation**: Added validation for gender and profession dropdown selections
- **Improved Avatar Storage**: Avatar selection properly stored locally and synced with backend
- **Better Error Handling**: All form fields now show validation errors with clear messages

### 🔧 **Current Dropdown Status**
- **Gender Dropdown**: Working with Male/Female options (can be expanded to include more options)
- **Profession Dropdown**: Working with comprehensive professional categories
- **Error States**: Validation implemented but visual error indicators need enhancement
- **Functionality**: Dropdowns open/close correctly, selections are stored and synced with backend

### 📋 Next Steps
- Test authentication flows with real backend
- Add content management features
- Implement audio playback functionality
- Add error handling UI improvements
- Production deployment preparation

---

## Build Information
- **Gradle**: Successfully builds (`./gradlew assembleDebug`)
- **Warnings**: Only minor deprecation warnings (cosmetic)
- **Package**: `com.wisme.firstapp` (aligned with Firebase)
- **Target SDK**: 35
- **Min SDK**: 24