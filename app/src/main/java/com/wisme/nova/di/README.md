# Dependency Injection

This folder contains Hilt modules for dependency injection setup.

## Suggested Structure:
```
di/
├── NetworkModule.kt      # Retrofit, OkHttp setup
├── DatabaseModule.kt     # Room database setup
├── RepositoryModule.kt   # Repository bindings
└── UseCaseModule.kt      # Use case bindings
```

Configure all dependencies and their scopes here.