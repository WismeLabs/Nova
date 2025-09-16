# 🚀 Nova Android Development Setup

**Welcome to the Wisme Nova development team!** 

This guide will help you clone this base template repository and start building the actual Nova frontend application.

## 🎯 What You're Getting

This base template provides:
- ✅ **Pre-configured Android project** with modern architecture
- ✅ **Folder structure templates** with README guides  
- ✅ **Development best practices** and workflow guidelines
- ✅ **Essential dependencies** and build configuration
- ✅ **Team coding standards** and Git workflow

**What you'll build:**
- 🎵 **Podcast streaming app** with authentication
- 📱 **Modern UI** using Jetpack Compose
- 🔄 **Real-time features** with proper state management
- 🏗️ **Scalable architecture** following Clean Architecture patterns

## 📋 Prerequisites

Before you start, make sure you have:

- **Android Studio** (latest stable version) or **VS Code** with Android extensions
- **JDK 17+** (required for Android development)
- **Git** installed and configured with your Wisme credentials
- **Figma access** (ask team lead for design file permissions)
- **API documentation** access (backend team will provide endpoints)

## 🛠️ Initial Setup

### 1. Clone This Base Template

```bash
git clone https://github.com/WismeLabs/Nova.git
cd Nova
```

### 2. Switch to Development Branch

```bash
# Always branch from beta, never from main
git checkout beta
git pull origin beta

# Create your feature branch
git checkout -b feat/your-feature-name
```

### 3. Configure Local Environment

1. **Copy the template file:**
   ```bash
   cp local.properties.template local.properties
   ```

2. **Edit `local.properties` with your system paths:**
   ```properties
   # Update these paths to match your system
   sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
   ndk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk\\ndk\\25.1.8937393
   
   # Backend API (ask team lead for the actual URL)
   API_BASE_URL="https://api.wisme.com/v1/"
   ```

### 4. Get Team Credentials

Ask your team lead for:
- `google-services.json` (Firebase config)
- API base URLs and authentication keys
- Figma file access
- Any additional environment variables

Place `google-services.json` in the `app/` directory.
**Never commit this file to Git** (it's in .gitignore)

### 5. Sync and Build

1. Open project in Android Studio
2. Let Gradle sync automatically  
3. Build the project: `Build > Make Project`
4. Run on emulator to verify setup

---

## 🏗️ Understanding the Base Architecture

This template provides the foundation structure. Here's what each folder is for:

```
app/src/main/java/com/wisme/nova/
├── ui/                    # 🎨 Your screens and components go here
├── viewmodel/            # 🧠 State management for your UI
├── domain/               # 💼 Business logic and models
├── data/                 # 💾 API calls and database operations
├── di/                   # 🔧 Dependency injection setup
└── utils/                # 🛠️ Helper functions and utilities
```

**Each folder contains a README.md** explaining what should go inside and how to structure your code.

### 📊 The Data Flow You'll Build

```
User Interaction → UI Component → ViewModel → UseCase → Repository → API/Database
      ↑                                                                    ↓
      ←←←←←←←←←←←←←←← UI State Updates ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←
```

---

## 🧑‍💻 Your Development Workflow

### 1. Pick Your First Task

- Check the project board for available features
- Start with something manageable (e.g., "Create login screen UI")
- Understand requirements before coding
- Ask questions if anything is unclear

### 2. Plan Your Feature

Before coding, think about:
- **What folders do I need to create?** (e.g., `ui/auth/`, `domain/model/`)
- **What data models are needed?** (User, LoginRequest, etc.)
- **What API endpoints will I call?** (Check with backend team)
- **What screens do I need?** (Check Figma designs)

### 3. Build Following the Architecture

**Start from the inside out:**

1. **Create domain models** in `domain/model/`
   ```kotlin
   // domain/model/User.kt
   data class User(
       val id: String,
       val email: String,
       val name: String
   )
   ```

2. **Define repository interface** in `domain/repository/`
   ```kotlin
   // domain/repository/AuthRepository.kt
   interface AuthRepository {
       suspend fun login(email: String, password: String): User
   }
   ```

3. **Create use case** in `domain/usecase/`
   ```kotlin
   // domain/usecase/LoginUseCase.kt
   class LoginUseCase(private val repository: AuthRepository) {
       suspend operator fun invoke(email: String, password: String) = repository.login(email, password)
   }
   ```

4. **Implement repository** in `data/repository/`
5. **Build ViewModel** in `viewmodel/`
6. **Create UI** in `ui/`

### 4. Code Quality Standards

**Always include references:**
```kotlin
// UI for Login Screen
// Figma: https://www.figma.com/file/xxxx/Login
// Backend API: POST /api/auth/login
@Composable
fun LoginScreen() { ... }
```

**Follow naming conventions:**
- Files: `PascalCase` (e.g., `AuthViewModel.kt`)
- Functions: `camelCase` (e.g., `loginUser()`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `API_BASE_URL`)

**Keep commits focused:**
```bash
git add .
git commit -m "feat(auth): add login screen validation"
```

### 5. Testing & Pull Requests

**Test your work:**
```bash
# Run unit tests
./gradlew test

# Run on emulator
./gradlew installDebug

# Test different screen sizes
```

**Create Pull Request:**
1. **Push your branch:**
   ```bash
   git push origin feat/task-name
   ```

2. **Create Pull Request with:**
   - **Title:** What you built (e.g., "Add login screen with validation")
   - **Description:** Why + how to test + screenshots
   - **DevProofs:** Attach screenshots or screen recordings
   - **Request review** from team lead

3. **Merge only after approval**

---

## 🛠️ Common Development Tasks

### Starting a New Feature

**Example: Building Login Screen**

1. **Create feature folders:**
   ```bash
   mkdir -p app/src/main/java/com/wisme/nova/ui/auth
   mkdir -p app/src/main/java/com/wisme/nova/domain/usecase/auth
   ```

2. **Build from domain layer up:**
   - `domain/model/User.kt` - User data model
   - `domain/usecase/auth/LoginUseCase.kt` - Login business logic
   - `data/repository/AuthRepositoryImpl.kt` - API implementation
   - `viewmodel/AuthViewModel.kt` - UI state management
   - `ui/auth/LoginScreen.kt` - Compose UI

### Adding API Integration

1. **Define DTOs** in `data/remote/dto/`
2. **Create API service** in `data/remote/api/`
3. **Add to repository implementation**
4. **Update dependency injection** in `di/`

### Adding Database Operations

1. **Create Room entities** in `data/local/entity/`
2. **Define DAO interfaces** in `data/local/dao/`
3. **Update database class** in `data/local/`
4. **Add to repository**

---

## 🐛 Troubleshooting

### Common Issues

**Gradle sync fails:**
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

**Firebase errors:**
- Ensure `google-services.json` is in `app/` directory
- Check if file is corrupted
- Ask team lead for fresh copy

**API errors:**
- Verify `API_BASE_URL` in `local.properties`
- Check network connection
- Test endpoint with Postman

**Build errors:**
- Check if all dependencies are synced
- Invalidate caches: `File > Invalidate Caches and Restart`
- Ensure JDK 17+ is configured

### Getting Help

1. **Check existing code** for similar implementations
2. **Ask in team chat** with specific error messages
3. **Create detailed issue** with:
   - What you were trying to do
   - Error message/screenshots
   - Steps to reproduce

---

## 📱 Testing

### Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Specific test
./gradlew test --tests="AuthUseCaseTest"
```

### Testing Strategy

- **Unit tests** for use cases and repositories
- **UI tests** for critical user flows
- **Integration tests** for API endpoints
- **Manual testing** on different screen sizes

---

## 🚀 Ready to Code!

You're all set! Here's what to do next:

1. ✅ **Familiarize yourself** with the existing code structure
2. ✅ **Pick your first task** from the project board
3. ✅ **Create a feature branch** following the naming convention
4. ✅ **Start coding** following the architecture patterns
5. ✅ **Ask questions** when you're stuck

Remember: **Don't be afraid to ask questions!** The team is here to help you succeed.

Happy coding! 🎉