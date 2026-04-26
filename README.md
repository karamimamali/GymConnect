# GymConnect

A Spring Core-based gym CRM system for managing trainee and trainer profiles with training activity tracking.

## Tech Stack

- Java 21
- Spring Framework 6.1 (Core, no Spring Boot)
- Gradle 8.10
- JUnit 5 + Mockito (testing)
- JaCoCo (code coverage)
- SLF4J + Logback (logging)
- Jackson (JSON data loading)

## Project Structure

```
src/main/java/com/gymconnect/
├── config/          # Spring configuration
├── dao/             # Data Access Objects (in-memory storage)
├── facade/          # Facade pattern entry point
├── model/           # Domain model entities
├── service/         # Business logic layer
├── storage/         # In-memory storage beans & initialization
├── util/            # Username & password generation utilities
└── GymApplication.java
```

## How to Build & Run

```bash
./gradlew clean build      # Build + test + coverage check
./gradlew test              # Run tests only
./gradlew jacocoTestReport  # Generate coverage report (build/reports/jacoco/test/html/index.html)
./gradlew run               # Run the application (if application plugin is applied)
```

## Features

- Trainee profile management (create, update, delete, select)
- Trainer profile management (create, update, select)
- Training session management (create, select)
- Automatic username generation (FirstName.LastName with serial number for duplicates)
- Automatic random password generation (10-character alphanumeric)
- File-based initial data loading via Spring bean post-processing
- In-memory storage using ConcurrentHashMap
