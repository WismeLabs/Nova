# Data Layer

This folder contains data access implementations, API services, and local storage.

## Suggested Structure:
```
data/
├── remote/         # API services (Retrofit interfaces)
├── local/          # Database (Room entities, DAOs)
├── repository/     # Repository implementations
└── dto/            # Data Transfer Objects for API
```

Implements the repository interfaces defined in the domain layer.