# TaskManagerKMPApp

A professional cross-platform Task Management application built using **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**. 

## 🚀 Overview

This application demonstrates a clean architecture approach for KMP projects, sharing UI, business logic, and data management between Android and iOS.

### Key Features
- **Dynamic Welcome Flow**: Engaging entry screen for new users.
- **Task Management**: Full CRUD-like capabilities (Add, Toggle Completion, Delete).
- **Priority System**: Visual color-coded indicators for High, Medium, and Low priorities.
- **Shared Data Layer**: Centralized repository handling state and initial data loading.
- **Robust Validation**: Domain-level validation rules for task creation.

---

## 🛠 Tech Stack
- **Kotlin Multiplatform**: Infrastructure for shared code.
- **Compose Multiplatform**: Declarative UI shared across Android and iOS.
- **ViewModel (Jetpack)**: Shared state management in `commonMain`.
- **Kotlinx Coroutines & Flow**: Reactive data streams for UI updates.
- **Kotlinx Serialization**: Shared JSON parsing.
- **Material 3**: Modern design system implementation.

---

## 📂 Architecture
The project follows a clean, layered architecture:
- **UI Layer (`ui/`)**: Purely declarative Composables (`Screens.kt`) and a shared `TaskViewModel`.
- **Domain Layer (`domain/`)**: Pure business logic and validation rules (`TaskValidator`).
- **Data Layer (`repository/`)**: Single source of truth for task data and state mutations.
- **Model Layer (`model/`)**: Shared data structures.

---

## 💻 How to Run

### Android
1. Select the `androidApp` configuration in **Android Studio**.
2. Click **Run**.

### iOS
1. Select the `iosApp` configuration.
2. Click **Run** (Requires Xcode on macOS).

---

## 🧪 Testing
The project includes a robust test suite in `commonTest` covering:
- **Validation Rules**: Ensuring data integrity.
- **Repository Logic**: Verifying state mutations.
- **ViewModel Transitions**: Testing UI state flow and navigation.

Run tests using: `./gradlew :composeApp:allTests`

---

## 👨‍💻 Developer Notes
This branch (`task/kmp-project-setup`) contains the complete refactor to address senior lead review comments, focusing on layer boundaries and removal of all scaffold boilerplate.
