# TaskManagerKMP: Complete  Developer Documentation

Welcome to **TaskManagerKMP**, a modern, cross-platform Task Management application built with **Kotlin Multiplatform (KMP)**, **Compose Multiplatform**, and a standalone **Ktor Backend Server**.

This document serves as the complete technical guide for new and existing developers. It details project setup, directory structure, feature implementation, environment configuration, cross-platform workflows (macOS & Windows), and troubleshooting.

---

## 1. Project Overview & Architecture

The application is structured into four main modules following clean KMP architecture principles for maximum code reuse:

```text
TaskManagerKMP/
├── androidApp/     # Native Android entry point & BuildConfig injection
├── shared/         # Multiplatform shared UI (Compose), State & Business Logic
│   ├── commonMain/ # Common Compose Multiplatform UI, Repository, Ktor Client
│   ├── androidMain/# Android-specific implementations (CredentialManager, SharedPreferences)
│   └── iosMain/    # iOS-specific implementations (NSUserDefaults, ViewController)
├── backend/        # Standalone Ktor REST API with SQLite database
└── iosApp/         # Native iOS Xcode wrapper project (Swift UI)
```

### Module Responsibilities

1. **`:androidApp`**: Native Android app wrapper. Initializes `SessionStorage`, `Platform` bridges, and reads `.env` variables to inject `BuildConfig` values (`GOOGLE_CLIENT_ID`, `BACKEND_URL`).
2. **`:shared`**:
   - `commonMain`: Contains all UI screens (`Screens.kt`), navigation (`App.kt`), state management (`Repository.kt`), network calls (`BackendApi.kt`), and platform abstractions (`Platform.kt`, `SessionStorage.kt`).
   - `androidMain`: Platform bindings for Android (`Platform.android.kt`, `SessionStorage.android.kt`).
   - `iosMain`: Platform bindings for iOS (`Platform.ios.kt`, `SessionStorage.ios.kt`).
3. **`:backend`**: Standalone Kotlin Ktor web server running on Netty (`port 8088`). Manages SQLite persistence (`Database.kt`) and handles authentication and task CRUD endpoints.
4. **`iosApp`**: Native iOS Xcode project loading the shared Compose Multiplatform view via `MainViewController`.

---

## 2. Codebase Map & Key Files

| Module / Path                                                    | File | Purpose |
|:-----------------------------------------------------------------| :--- | :--- |
| **`:shared:commonMain:kotlin`**                                  | [`App.kt`](file:///D:/task%20manager%20app/shared/src/commonMain/kotlin/com/example/taskmanagerkmpapp/App.kt) | Top-level Compose entry point, session auto-login check, navigation router. |
|                                                                  | [`Screens.kt`](file:///D:/task%20manager%20app/shared/src/commonMain/kotlin/com/example/taskmanagerkmpapp/Screens.kt) | All Composable UI screens: `AuthScreen`, `TaskListScreen`, `AddTaskScreen`, `ForgotPasswordScreen`, and Google Account Chooser dialog. |
|                                                                  | [`Repository.kt`](file:///D:/task%20manager%20app/shared/src/commonMain/kotlin/com/example/taskmanagerkmpapp/Repository.kt) | Central `Repo` singleton managing application state, token, and API delegation. |
|                                                                  | [`BackendApi.kt`](file:///D:/task%20manager%20app/shared/src/commonMain/kotlin/com/example/taskmanagerkmpapp/BackendApi.kt) | Ktor HTTP client making REST calls to the backend server. |
|                                                                  | [`SessionStorage.kt`](file:///D:/task%20manager%20app/shared/src/commonMain/kotlin/com/example/taskmanagerkmpapp/SessionStorage.kt) | Interface for session token and saved email accounts persistence. |
| **`:shared:androidMain`**                                        | [`Platform.android.kt`](file:///D:/task%20manager%20app/shared/src/androidMain/kotlin/com/example/taskmanagerkmpapp/Platform.android.kt) | CredentialManager integration & fallback trigger for Google Sign-In. |
|                                                                  | [`SessionStorage.android.kt`](file:///D:/task%20manager%20app/shared/src/androidMain/kotlin/com/example/taskmanagerkmpapp/SessionStorage.android.kt) | Android `SharedPreferences` implementation for session and saved emails. |
| **`:shared:iosMain`**                                            | [`SessionStorage.ios.kt`](file:///D:/task%20manager%20app/shared/src/iosMain/kotlin/com/example/taskmanagerkmpapp/SessionStorage.ios.kt) | iOS `NSUserDefaults` implementation for persistent session storage. |
| **`:backend`**                                                   | [`Application.kt`](file:///D:/task%20manager%20app/backend/src/main/kotlin/com/example/taskmanagerkmpapp/Application.kt) | Ktor server main function, Netty engine setup, routing (`/login`, `/register`, `/tasks`). |
|                                                                  | [`Database.kt`](file:///D:/task%20manager%20app/backend/src/main/kotlin/com/example/taskmanagerkmpapp/Database.kt) | SQLite JDBC wrapper with self-healing auto-recovery from corrupted `.db` files. |
| **`:androidApp:src:main:kotlin:com.example.taskmanagerkmapapp`** | [`MainActivity.kt`](file:///D:/task%20manager%20app/androidApp/src/main/kotlin/com/example/taskmanagerkmpapp/MainActivity.kt) | ComponentActivity initializing platform contexts and launching `App()`. |

---

## 3. Key Application Features

### A. Authentication & Google Sign-In
- **Domain Auto-Registration (`@bhrish.com` / `@bhrish`)**:
  Users signing in with an `@bhrish.com` email address are automatically registered and authenticated.
- **Database Verification for Other Domains**:
  For non-`@bhrish.com` emails (e.g., `@gmail.com`), the backend checks if the user is already registered in the SQLite database. If registered, sign-in succeeds; if not registered, the app displays:
  > **`User is not registered. Please register first.`**
- **In-App Account Chooser Dialog**:
  Tapping **Continue with Google** presents an in-app dialog displaying previously logged-in emails with avatar circles.
- **Account Management**:
  Users can tap the **✕** icon on any saved account item in the dialog to remove it from local storage, or select **"+ Use another account"** to enter a new email address.
- **Manual Registration & Hashing**:
  Manual signup validates required fields (Name, Username, Email, Password minimum 6 chars). Passwords are hashed using **SHA-256** prior to database storage.

### B. Task Management (CRUD)
- **Create**: Add tasks with Title, Description, Priority (`Low`, `Medium`, `High`), and an optional Due Date & Time using interactive Material3 Date/Time pickers.
- **Read & Dashboard**: View user tasks in a LazyColumn with priority color indicators. Dashboard header displays user's first name (e.g., `VAISHNAVI's Tasks`), current date, and a distinct white card showing total task count.
- **Update**: Checkbox toggles task completion status (strikethrough text style).
- **Delete**: Delete button removes tasks via the `/tasks/{id}` DELETE endpoint.



## 3. Cloning & Repository Setup (macOS & Windows)

### A. How to Clone the Repository
Open Terminal (macOS/Linux) or Command Prompt / PowerShell (Windows):

```bash
# Clone the repository
git clone https://github.com/your-org/TaskManagerKMP-App.git

# Navigate into the project folder
cd TaskManagerKMP-App
```

### B. Branch Management
To check available branches and switch to a specific branch:

```bash
# List all local and remote branches
git branch -a

# Switch to a feature or working branch
git checkout branch-name

# Pull latest updates
git pull origin branch-name
```

---

## 4. Environment Configuration (`.env`)

The project uses a root [`.env`](file:///D:/task%20manager%20app/.env) file to configure API endpoints and Google Client credentials:

```env
BACKEND_URL=http://192.168.1.31:8088
GOOGLE_CLIENT_ID=458266882707-s91o96afee97bdpvurhmo95e9ot39pu1.apps.googleusercontent.com
```

### Setting `BACKEND_URL` for Different Targets

1. **Android Emulator**:
   Use the special Android host alias:
   `BACKEND_URL=http://10.0.2.2:8088`
2. **Physical Android Device / Local Wi-Fi Network**:
   Set `BACKEND_URL` to your host computer's local Wi-Fi IP address (e.g., `http://192.168.1.31:8088`).

#### Finding Your Computer's Local IP Address:
- **macOS / Linux**:
  ```bash
  ipconfig getifaddr en0
  # or
  ifconfig | grep "inet "
  ```
- **Windows**:
  ```powershell
  ipconfig
  ```
  *(Look for IPv4 Address under your Wi-Fi or Ethernet adapter).*

> [!IMPORTANT]
> Both your physical phone and host computer MUST be connected to the **same Wi-Fi network**.

---

## 5. How to Build & Run the Application

### Step 1: Run the Backend Server (`:backend`)

The backend server MUST be running before testing authentication or task management.

#### Option A: Via Android Studio IDE
1. Open [`Application.kt`](file:///D:/task%20manager%20app/backend/src/main/kotlin/com/example/taskmanagerkmpapp/Application.kt).
2. Click the green **Play (▶)** button next to `fun main()` on line 28.
3. Confirm the console prints:
   `>>> Starting Ktor Backend Server on http://0.0.0.0:8088 ...`

#### Option B: Via Terminal / Command Line
- **macOS / Linux**:
  ```bash
  ./gradlew :backend:run
  ```
- **Windows**:
  ```powershell
  .\gradlew.bat :backend:run
  ```

---

### Step 2: Run the Android Application (`:androidApp`)

#### Option A: Via Android Studio IDE
1. Select **`androidApp`** from the run configurations dropdown at the top.
2. Choose your target device (Emulator or connected Physical Phone).
3. Click **Run (▶)**.

#### Option B: Via Terminal
- **macOS / Linux**:
  ```bash
  ./gradlew :androidApp:installDebug
  ```
- **Windows**:
  ```powershell
  .\gradlew.bat :androidApp:installDebug
  ```

---

### Step 3: Run the iOS Application (`iosApp`) (macOS Only)

1. Open Xcode on macOS.
2. Open the project file: `iosApp/iosApp.xcodeproj`.
3. Select an iOS Simulator (e.g., iPhone 15 Pro) or connected iPhone.
4. Click **Run (⌘R)**.

---

## 6. Generating & Locating the APK

To generate the Debug APK for manual installation or sharing:

### Build Command
- **macOS / Linux**:
  ```bash
  ./gradlew :androidApp:assembleDebug
  ```
- **Windows**:
  ```powershell
  .\gradlew.bat :androidApp:assembleDebug
  ```

### Output APK File Location
Once the build completes successfully, the compiled `.apk` file is located at:

```text
androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

> [!NOTE]
> **File Modified Date & Timestamps**:
> In Windows File Explorer or macOS Finder, the "Date modified" timestamp on `androidApp-debug.apk` reflects the system clock date when the APK was compiled (e.g. `23-09-2026`). Ensure your host machine / emulator clock is synchronized.

---

## 7. Troubleshooting Guide

### 1. `Network error: Connect timeout has expired`
- **Cause**: The app is trying to reach `10.0.2.2:8088` on a physical device, or your computer's local IP changed.
- **Fix**: Update `BACKEND_URL` in [`.env`](file:///D:/task%20manager%20app/.env) to your host computer's current local IP (e.g., `http://192.168.1.31:8088`) and rebuild the app.

### 2. `Network error: Failed to connect to /10.0.2.2:8088`
- **Cause**: The backend Ktor server is not running.
- **Fix**: Run `Application.kt` or `./gradlew :backend:run` first.

### 3. `Process finished with exit value 1` / `Address already in use`
- **Cause**: Port `8088` is occupied by an orphaned Java process.
- **Fix**: Stop background Java processes:
  - macOS/Linux: `pkill -f ApplicationKt`
  - Windows PowerShell: `Get-Process -Name java | Stop-Process -Force`

### 4. `SQLITE_CORRUPT: database disk image is malformed`
- **Cause**: Sudden power loss or process kill during database write.
- **Fix**: Handled automatically by self-healing logic in [`Database.kt`](file:///D:/task%20manager%20app/backend/src/main/kotlin/com/example/taskmanagerkmpapp/Database.kt). Simply restart the backend server.

---

*This document serves as the definitive reference for onboarding, developing, and deploying TaskManagerKMP across Android, iOS, and Backend environments.*
