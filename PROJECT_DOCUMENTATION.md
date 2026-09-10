# TaskManagerKMP: Complete Technical Documentation

This document provides an exhaustive, step-by-step technical breakdown of the TaskManager application, covering architecture, data flows, security, and UI implementation.

---

## 1. Project Architecture (Kotlin Multiplatform)

The application follows a modular architecture designed for code sharing and platform native performance.

### A. `:androidApp` (Android Entry Point)
- **Role**: Native wrapper for the shared logic.
- **Key Files**: 
    - `MainActivity.kt`: Initializes `SessionStorage`, `Platform` bridge, and `GoogleLogin` before launching the `App()` composable.
    - `build.gradle.kts`: Reads the `.env` file and generates `BuildConfig` fields used for network and API configuration.

### B. `:shared` (Shared Logic & UI)
- **`commonMain`**: 
    - `App.kt`: The navigation controller and entry point for the UI.
    - `Screens.kt`: Contains all UI screens (Auth, Tasks, Add Task, Reset).
    - `Repository.kt`: Manages the application state and business logic.
    - `BackendApi.kt`: Handles Ktor network requests to the server.
- **`androidMain`**: 
    - `Platform.android.kt`: Implements the `googleLogin` function using Android's `CredentialManager`.
    - `SessionStorage.android.kt`: Implements persistent storage using Android `SharedPreferences`.

### C. `:backend` (Server Side)
- **Role**: A standalone Kotlin server providing a RESTful API.
- **Key Files**:
    - `Application.kt`: Defines API routes (`/login`, `/register`, `/tasks`).
    - `Database.kt`: Manages an SQLite database using JDBC.

---

## 2. Detailed Technical Flows

### A. Authentication & Registration Flow
1. **Google Sign-In**:
    - The user clicks "Continue with Google".
    - `googleLogin()` (Android-specific) triggers the account picker.
    - **Verification**: The app checks if the email ends with `@bhrish.com`.
    - **Success**: The app sends the email to the backend with a `isGoogle=true` flag.
    - **Backend Action**: The server uses `getOrCreateByEmail` to automatically sign in or register the user.
2. **Manual Registration**:
    - Users provide Name, Username, Email, and Password.
    - **Validation**: `Screens.kt` checks that all fields are filled to prevent empty-email conflicts in the database.
    - **Hashing**: The backend hashes the password using **SHA-256** before storage.

### B. Task Management (CRUD)
- **Create**: `AddTaskScreen` takes title, description, and priority. The backend generates a unique ID.
- **Read**: `TaskListScreen` fetches tasks via the `Repo`. It displays a loading state and color-codes tasks by priority.
- **Update**: Checking a task sends a PUT request to `/tasks/{id}` to update the completion status.
- **Delete**: Clicking the trash icon sends a DELETE request to `/tasks/{id}`.

### C. Session Persistence
- **Login**: Upon successful login, the `UUID` token and user profile are saved in `SharedPreferences`.
- **App Startup**: `App.kt` uses a `LaunchedEffect(Unit)` to read the stored token. If valid, it pre-loads the task list from a local cache (`tasks_cache`) and skips the login screen.

---

## 3. Configuration & Networking

### A. Environment Variables (.env)
The project relies on a root `.env` file for configuration:
- `GOOGLE_CLIENT_ID`: The Web Client ID from the Google Cloud Console.
- `BACKEND_URL`: Set to your computer's local IP (e.g., `http://192.168.1.7:8080`) to allow physical Android devices to connect to your local server.

### B. Network Requirements
- **Local Network**: Both the Android device/emulator and the computer must be on the same Wi-Fi.
- **Firewall**: Port `8080` must be open on the host computer.
- **Network Profile**: The Wi-Fi network profile on Windows must be set to **Private** to allow incoming connections.

---

## 4. UI & Design System

The application uses **Compose Multiplatform** with a customized Material 3 theme.

- **Color Palette**: 
    - Primary: Deep Blue (`0xFF1A237E`).
    - Error/High Priority: Red (`Color.Red`).
    - Warning/Medium Priority: Orange (`0xFFF57C00`).
    - Success/Low Priority: Green (`Color.Green`).
- **Typography & Shapes**:
    - Buttons: Pill-shaped (`RoundedCornerShape(24.dp)`).
    - Date Display: Uses `kotlinx-datetime` to show the current date as `DD.MM.YYYY`.
- **Interaction**:
    - Password fields include an eye-icon toggle for visibility.
    - All network-heavy operations (login, saving) display a "Loading..." or "Processing..." state.

---

## 5. Security Summary
- **Domain Locking**: Only specific organizations (`@bhrish.com`) can use the one-tap login.
- **Unique Constraints**: The database enforces unique usernames and emails to prevent duplicate accounts.
- **Encrypted Storage**: Sensitive auth tokens are stored in the app's private SharedPreferences space.

---

*End of Documentation. This file serves as the definitive guide for understanding and maintaining the TaskManagerKMP project.*
