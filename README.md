# Nova
Wisme Frontend Service

## Architecture

The project follows a **feature-based modular MVVM structure**:

```text
app/
├── ui/                         # Presentation layer (screens, components)
│    ├── auth/                  # Login, Register, ForgotPassword
│    │    ├── AuthActivity.kt
│    │    ├── AuthViewModel.kt
│    │    └── AuthScreen.kt
│    ├── podcasts/              # Podcast listing, details
│    ├── playback/              # Player, controls
│    └── profile/               # User profile, settings
├── viewmodel/                  # Shared ViewModels and state management
├── data/                       # Data layer
│    ├── remote/                # Retrofit API services
│    ├── local/                 # Room database, DAOs
│    └── repository/            # Repositories combining remote + local
├── domain/                     # Business logic
│    ├── model/                 # Data models (Kotlin data classes)
│    └── usecase/               # Application-specific logic
├── di/                         # Dependency injection modules (Hilt)
├── utils/                      # Helpers, constants, extensions
├── App.kt                      # Application class
├── build.gradle.kts            # App-level Gradle config
└── AndroidManifest.xml
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

## 🔧 Setup Instructions

### Prerequisites
- Android Studio (recommended) or VS Code with Android extensions  
- JDK 17+  
- Firebase project (for auth)  
- `google-services.json` in `app/`  

### Installation
1. Clone the repository:
   ```bash
   git clone <https://github.com/WismeLabs/Nova.git>
   cd nova-android
   ```
2. Open in Android Studio or VS Code.  

3. Sync Gradle dependencies.  

4. Configure environment in `local.properties` or `BuildConfig`:  
```properties
API_BASE_URL=https://your-backend-url/api/v1/
FIREBASE_API_KEY=xxxx
```

5. Run on emulator or physical device.

### Deployment

- Generate release build:

```bash
./gradlew assembleRelease
```

- Configure signing in gradle.properties.

- Upload .aab to Google Play Console.

### Troubleshooting

- Gradle sync errors → Invalidate caches and restart.

- Firebase errors → Ensure google-services.json is in app/.

- API errors → Verify API_BASE_URL matches backend.

- Frontend bugs → Always handle loading and error states.



