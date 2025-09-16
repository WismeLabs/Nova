# Domain Layer (Business Logic)

This folder contains business logic, use cases, and domain models.

## Suggested Structure:
```
domain/
├── model/          # Data classes (User, Podcast, Episode, etc.)
├── usecase/        # Business logic (AuthUseCase, PodcastUseCase, etc.)
└── repository/     # Repository interfaces (contracts)
```

Keep this layer independent of frameworks and external dependencies.