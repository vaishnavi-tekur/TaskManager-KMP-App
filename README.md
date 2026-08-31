# TaskManagerKMPApp

A cross-platform Task Management application built using **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**. This project shares ~95% of its code (UI, Logic, and Data) between Android and iOS.

## 🚀 Overview

This application demonstrates a unified development workflow where the user interface, business logic, and data models are written once in Kotlin and deployed to both mobile platforms.

### Key Features
- **Shared UI**: Entirely built with Compose Multiplatform in the `composeApp` module.
- **Dynamic Data**: Task data is parsed from a shared JSON resource using `kotlinx-serialization`.
- **Priority Management**: Tasks support High, Medium, and Low priorities with visual indicators.
- **Task Interaction**: Add, delete, and toggle task completion states.

---

## 🛠 Tech Stack
- **Kotlin Multiplatform**: Shared logic and infrastructure.
- **Compose Multiplatform**: Shared UI for Android and iOS.
- **Kotlinx Serialization**: JSON parsing across platforms.
- **Material 3**: Modern design system implementation.

---

## 📂 Project Structure
- `composeApp/`: The core of the application.
    - `src/commonMain/`: Contains `App.kt` (UI Entry), `ui/` (Screens & ViewModel), `model/` (Data classes), and `repository/` (Logic).
    - `src/commonMain/composeResources/`: Shared assets like `tasks.json`.
    - `src/androidMain/` & `src/iosMain/`: Platform-specific entry points.
- `androidApp/`: Android-specific configuration and launcher.
- `iosApp/`: Xcode project for the iOS application.

---

## 💻 How to Run

### Android (Windows/Mac/Linux)
1. Open the project in **Android Studio**.
2. Select the `androidApp` configuration in the top toolbar.
3. Click **Run** to deploy to an emulator or physical device.

### iOS (Mac Only)
1. Ensure you have **Xcode** installed.
2. In Android Studio, select the `iosApp` configuration.
3. Click **Run** to build and launch the iOS Simulator.
4. *Alternatively:* Open the `iosApp/iosApp.xcworkspace` file in Xcode and run from there.

---

## 👨‍💻 Developer Notes for Reviewers

### Branch Strategy
This project follows a clean architecture pattern with a dedicated ViewModel and UI separation in the `commonMain` module.

### Interaction
- The app loads initial data from `tasks.json`.
- Use the **Pink FAB (+)** to navigate to the "Add New Task" screen.
- Select a priority from the dropdown and save to see it update in the shared `TaskList`.

---

## 📞 Contact
For any questions regarding the shared architecture or platform-specific implementations, please reach out to the project maintainer.
