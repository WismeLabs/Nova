# Domain Layer (Business Logic)

This folder contains the business logic, use cases, and domain models for Nova's podcast streaming functionality.

## Structure:
```
domain/
├── model/          # Data classes (User, Podcast, Episode, etc.)
├── usecase/        # Business logic (AuthUseCase, PodcastUseCase, etc.)
└── repository/     # Repository interfaces (contracts)
```

This layer is independent of frameworks and external dependencies, containing pure Kotlin business logic.