# Nova - Android Frontend Application
**Wisme Podcast Streaming App**

[![Android CI/CD](https://github.com/WismeLabs/Nova/actions/workflows/android-ci.yml/badge.svg)](https://github.com/WismeLabs/Nova/actions/workflows/android-ci.yml)
[![Code Quality](https://github.com/WismeLabs/Nova/actions/workflows/code-quality.yml/badge.svg)](https://github.com/WismeLabs/Nova/actions/workflows/code-quality.yml)

**Nova** is the official Android frontend application for Wisme, built with modern Android development practices using Kotlin and Jetpack Compose.

## 🎯 Features

Nova provides:
- 🎵 **Podcast Streaming** - Stream and discover podcasts
- 🔐 **User Authentication** - Secure login and user management
- 📱 **Modern UI** - Built with Jetpack Compose and Material 3
- 🎧 **Audio Playback** - High-quality audio streaming with playback controls
- 🏗️ **Clean Architecture** - Scalable and maintainable codebase
- 🔄 **Real-time Sync** - Keep user data synchronized across devices

## 📁 Architecture Overview

The project follows a **feature-based modular MVVM + Clean Architecture structure**:

```text
app/src/main/java/com/wisme/nova/
├── ui/                         # 🎨 Presentation layer (screens, components)
│    ├── auth/                  # Login, Register, ForgotPassword
│    ├── journeys/              # Journey listing, details  
│    ├── playback/              # Player, controls
│    └── profile/               # User profile, settings
├── viewmodel/                  # 🧠 Shared ViewModels and state management
├── data/                       # 💾 Data layer
│    ├── remote/                # Retrofit API services
│    ├── local/                 # Room database, DAOs
│    └── repository/            # Repositories combining remote + local
├── domain/                     # 💼 Business logic
│    ├── model/                 # Data models (Kotlin data classes)
│    └── usecase/               # Application-specific logic
├── di/                         # 🔧 Dependency injection modules (Hilt)
├── utils/                      # 🛠️ Helpers, constants, extensions
└── MainActivity.kt             # Main activity
```
### Data Flow (MVVM + Clean Architecture)

```text
UI (Compose/Activities/Fragments)
        │
        ▼
   ViewModel (State + Logic)
        │
        ▼
 Domain / UseCases (Business rules)
        │
        ▼
 Repository (Abstracts data sources)
        │
   ┌─────┴─────┐
   ▼           ▼
Remote (API)   Local (DB)
 Retrofit      Room/Firebase
```

## Layered Roles
- **UI (Compose/Activities/Fragments):** Renders UI, links Figma references, handles interactions.  
- **ViewModel:** Holds state, manages logic, connects to use cases.  
- **Domain/UseCases:** Business logic.  
- **Repository:** Mediates between API and DB.  
- **Data:** Implements API (Retrofit), DB (Room), Firebase.  

---

##  Technology Stack
- **Language:** Kotlin  
- **UI:** Jetpack Compose  
- **Architecture:** MVVM + Clean Architecture  
- **Dependency Injection:** Hilt  
- **Networking:** Retrofit + OkHttp  
- **Database:** Room  
- **Auth:** Firebase Auth  
- **Media:** ExoPlayer (podcast playback)  
- **Concurrency:** Coroutines + Flow  
- **Images:** Coil  
- **Code Quality:** ktlint / detekt  
- **Version Control:** Git (with Wisme Rulebook workflow)  

---

## 📋 Development Best Practices

### 🏗️ Folder Structure Guidelines

**Feature-Based Organization:**
```kotlin
// ✅ Good: Feature-based
ui/auth/LoginScreen.kt
ui/auth/RegisterScreen.kt
ui/auth/AuthViewModel.kt

// ❌ Avoid: Type-based
ui/screens/LoginScreen.kt
ui/screens/RegisterScreen.kt
ui/viewmodels/AuthViewModel.kt
```

**Naming Conventions:**
- **Files:** PascalCase (`AuthViewModel.kt`, `LoginScreen.kt`)
- **Packages:** lowercase (`com.wisme.nova.ui.auth`)
- **Classes:** PascalCase (`AuthRepository`, `UserModel`)
- **Functions:** camelCase (`loginUser()`, `validateEmail()`)
- **Constants:** UPPER_SNAKE_CASE (`API_BASE_URL`, `MAX_RETRY_COUNT`)

**Package Structure Best Practices:**
```kotlin
// ✅ Feature modules
domain/
├── model/
│   ├── User.kt
│   ├── Podcast.kt
│   └── Episode.kt
├── usecase/
│   ├── auth/
│   │   ├── LoginUseCase.kt
│   │   └── RegisterUseCase.kt
│   └── podcast/
│       ├── GetPodcastsUseCase.kt
│       └── PlayEpisodeUseCase.kt
└── repository/
    ├── AuthRepository.kt
    └── PodcastRepository.kt
```

### 🔧 Code Organization

**Dependency Flow:**
```
UI Layer → ViewModel → UseCase → Repository → Data Source
```

**Single Responsibility:**
- Each class should have one reason to change
- Separate concerns across layers
- Keep functions small and focused

**Comments & Documentation:**
```kotlin
// ✅ Always include Figma/API references
// UI for Login Screen
// Figma: https://www.figma.com/file/xxxx/Login
// Backend API: POST /auth/login
@Composable
fun LoginScreen() { ... }
```

---

## 🚀 CI/CD & Automation

### Automated Checks
Every PR automatically runs:
- ✅ **Build validation** - Ensures code compiles without errors
- ✅ **Unit tests** - Runs all existing tests
- ✅ **Lint checks** - Code style and potential issues
- ✅ **Branch protection** - Enforces branching rules

### Branch Protection Rules
- ❌ **Direct pushes to `main`** are blocked
- ✅ **Only `beta` → `main`** merges allowed
- ✅ **Feature branches** must target `beta`
- ✅ **All checks must pass** before merge

### Commit Message Format
```bash
# Use any format you prefer - no validation enforced

# Examples:
Add login validation
Fix audio playback issue
Update setup instructions
```

---

##  Git & Workflow Rules

### Branches
- **Don’t touch `main`.**  
- Branch from `beta`.  
- Naming convention: `feat/`, `fix/`, `logs/`, `docs/`, `test/`.  
  - Example: `feat/login-ui`, `fix/player-crash`.
-Domain/UseCases: business logic.
-Repository: mediates between API and DB.
-Data: implements API (Retrofit), DB (Room), Firebase.

### Commits
- One logical change per commit.  
- Format:  
  ```text
  feat(auth): add login screen
  ```

### Pull Requests
- **Title**: What you did.  
- **Description**: Why + how to test.  
- Keep PRs small, attach DevProofs (screenshots/recordings).  
- Must be reviewed before merge.  

### Merging
- Update with `beta` before merge.  
- Merge only after approval.  

### Commenting
Add Figma/API reference in comments.  

**Example:**
```kotlin
// UI for Login Screen
// Figma: https://www.figma.com/file/xxxx/Login
// Backend API: POST /auth/login
```

## � Quick Start Guide

### Prerequisites
- Android Studio (recommended) or VS Code with Android extensions  
- JDK 17+  
- Git configured on your machine

### Getting Started
1. **Clone this base template:**
   ```bash
   git clone https://github.com/WismeLabs/Nova.git
   cd Nova
   ```

2. **Switch to development branch:**
   ```bash
   git checkout beta
   git pull origin beta
   ```

3. **Create your feature branch:**
   ```bash
   git checkout -b feat/your-feature-name
   ```

4. **Configure local environment:**
   - Copy `local.properties.template` to `local.properties`
   - Update paths and environment variables
   - Get `google-services.json` from team lead (place in `app/`)

5. **Open in IDE and sync:**
   - Open project in Android Studio or VS Code
   - Let Gradle sync automatically
   - Start building your features!

### Getting Started
- Review the `SETUP.md` for development environment setup
- Check the current project structure in `app/src/main/java/com/wisme/nova/`
- Follow the architecture patterns when adding new features
- Ensure all code follows the established patterns and conventions

## 🛠️ Project Configuration

Nova includes:
- ✅ **Modern Android setup** with latest dependencies
- ✅ **Build variants** (debug/release) configured  
- ✅ **Jetpack Compose** with Material 3
- ✅ **Architecture dependencies** (Hilt, Retrofit, Room)
- ✅ **Testing frameworks** configured
- ✅ **ProGuard rules** for release optimization

### Adding Dependencies
When you need additional dependencies, add them to `app/build.gradle.kts`:

```kotlin
dependencies {
    // Example: Add Hilt for DI
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Example: Add Retrofit for networking  
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}
```

### Building & Running
```bash
# Debug build
./gradlew assembleDebug

# Release build  
./gradlew assembleRelease

# Run tests
./gradlew test
```


---

## 📄 License & Ownership

**© 2025 Wisme Labs** - Internal development template for team use.



