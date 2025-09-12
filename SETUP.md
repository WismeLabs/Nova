# 🚀 Nova Android Development Setup

## 📋 Prerequisites

Before you start, make sure you have:

- **Android Studio** (latest stable version) or **VS Code** with Android extensions
- **JDK 17+** (required for Android development)
- **Git** installed and configured
- **Firebase project** access (ask team lead for credentials)

## 🛠️ Initial Setup

### 1. Clone the Repository

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

2. **Edit `local.properties` with your paths:**
   ```properties
   # Update these paths to match your system
   sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
   ndk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk\\ndk\\25.1.8937393
   
   # Backend API (ask team lead for the actual URL)
   API_BASE_URL="https://your-backend-url.com/api/v1/"
   ```

### 4. Add Firebase Configuration

1. Get `google-services.json` from team lead
2. Place it in the `app/` directory
3. **Never commit this file to Git** (it's in .gitignore)

### 5. Sync and Build

1. Open project in Android Studio
2. Let Gradle sync automatically
3. Build the project: `Build > Make Project`

---

## 🏗️ Project Architecture

Nova follows **Clean Architecture + MVVM** pattern:

```
app/
├── ui/                    # 🎨 Presentation Layer
│   ├── auth/             # Login, Register screens
│   ├── podcasts/         # Podcast listing, details
│   ├── playback/         # Media player controls
│   └── profile/          # User profile, settings
├── viewmodel/            # 🧠 ViewModels (state management)
├── domain/               # 💼 Business Logic
│   ├── model/            # Data models (User, Podcast, Episode)
│   └── usecase/          # Business logic (AuthUseCase, PodcastUseCase)
├── data/                 # 💾 Data Layer
│   ├── remote/           # API services (Retrofit)
│   ├── local/            # Database (Room)
│   └── repository/       # Data repositories
├── di/                   # 🔧 Dependency Injection (Hilt)
└── utils/                # 🛠️ Helpers, constants, extensions
```

### 📊 Data Flow

```
UI (Compose) → ViewModel → UseCase → Repository → API/Database
     ↑                                              ↓
     ←←←←←←←←←←← State/Data ←←←←←←←←←←←←←←←←←←←←←←←←←←←
```

---

## 🧑‍💻 Development Workflow

### 1. Pick a Task

- Check the project board for available tasks
- Assign yourself to the task
- Understand the requirements before coding

### 2. Create Feature Branch

```bash
git checkout beta
git pull origin beta
git checkout -b feat/task-name
```

### 3. Development Process

1. **Follow the architecture pattern:**
   - Start with **domain models** (data classes)
   - Create **use cases** for business logic
   - Implement **repositories** for data access
   - Build **ViewModels** for state management
   - Design **UI** with Compose

2. **Add comments with Figma/API references:**
   ```kotlin
   // UI for Login Screen
   // Figma: https://www.figma.com/file/xxxx/Login
   // Backend API: POST /auth/login
   @Composable
   fun LoginScreen() { ... }
   ```

3. **Test your changes:**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

### 4. Code Quality

- **Follow naming conventions:**
  - Files: `PascalCase` (e.g., `AuthViewModel.kt`)
  - Functions: `camelCase` (e.g., `loginUser()`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `API_BASE_URL`)

- **Keep commits small and focused:**
  ```bash
  git add .
  git commit -m "feat(auth): add login screen validation"
  ```

### 5. Pull Request

1. **Push your branch:**
   ```bash
   git push origin feat/task-name
   ```

2. **Create Pull Request:**
   - Title: What you did (e.g., "Add login screen with validation")
   - Description: Why + how to test
   - Attach screenshots/recordings (DevProofs)
   - Request review from team lead

3. **Merge only after approval**

---

## 🛠️ Common Development Tasks

### Adding a New Screen

1. **Create domain models** in `domain/model/`
2. **Add API DTOs** in `data/remote/dto/`
3. **Create use case** in `domain/usecase/`
4. **Implement repository** in `data/repository/`
5. **Build ViewModel** in `viewmodel/`
6. **Design UI** in `ui/feature/`

### Adding API Integration

1. **Define DTO classes** in `data/remote/dto/`
2. **Add API service methods** in `data/remote/`
3. **Create mappers** in `utils/Mappers.kt`
4. **Update repository implementation**
5. **Test with Postman or similar tool**

### Database Operations

1. **Create Room entities** in `data/local/entity/`
2. **Define DAO interfaces** in `data/local/dao/`
3. **Update database class** in `data/local/`
4. **Add migration if needed**

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