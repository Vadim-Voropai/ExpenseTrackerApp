# Open Expense Tracker

A Kotlin Multiplatform (KMP) expense tracking application for Android and iOS, featuring local database storage and Google Drive synchronization.

## 🏗 Architecture

The project follows a clean architecture approach within a Kotlin Multiplatform structure:

*   **`shared` Module**: Contains the core logic shared between Android and iOS.
    *   **`commonMain`**: Domain models, repository interfaces, and shared business logic (ViewModels using `uiState` pattern).
    *   **`data`**: Implementation of repositories, SQLDelight database setup, and remote API interactions (Ktor).
    *   **`domain`**: Business rules and repository abstractions.
    *   **`presentation`**: Shared ViewModels and UI state definitions.
    *   **`androidMain` / `iosMain`**: Platform-specific implementations (e.g., Database Driver Factory, Google Authentication).
*   **`androidApp`**: Native Android application module.
*   **`iosApp`**: Native iOS application module (SwiftUI).

## 🛠 Tech Stack

*   **UI**: Compose Multiplatform
*   **Database**: [SQLDelight](https://cashapp.github.io/sqldelight/) for persistent multiplatform storage.
*   **Dependency Injection**: [Koin](https://insert-koin.io/)
*   **Networking**: [Ktor](https://ktor.io/)
*   **Serialization**: Kotlinx Serialization
*   **Concurrency**: Kotlin Coroutines & Flow
*   **Date/Time**: Kotlinx Datetime

## ☁️ Google Drive API Configuration

To enable cloud synchronization, you must configure a project in the [Google Cloud Console](https://console.cloud.google.com/).

### 1. Create a Google Cloud Project
1.  Go to the Google Cloud Console.
2.  Create a new project named "Open Expense Tracker".

### 2. Enable Google Drive API
1.  Navigate to **APIs & Services > Library**.
2.  Search for "Google Drive API" and click **Enable**.

### 3. Configure OAuth Consent Screen
1.  Go to **APIs & Services > OAuth consent screen**.
2.  Choose **External** user type.
3.  Fill in the required app information.
4.  **Add Scopes**: Add `https://www.googleapis.com/auth/drive.file` (allows the app to see and download only the specific files that it creates or that the user opens with the app).

### 4. Create OAuth 2.0 Credentials (Android)
1.  Go to **APIs & Services > Credentials**.
2.  Click **Create Credentials > OAuth client ID**.
3.  Select **Android** as the application type.
4.  **Package Name**: `your app package name`
5.  **SHA-1 Certificate Fingerprint**: 'your SHA-1'
6.  Click **Create**.

### 5. Create OAuth 2.0 Credentials (iOS)
1.  Create another OAuth client ID, but select **iOS** as the application type.
2.  Enter your **Bundle ID** (check `iosApp` project settings).
3.  Use the generated **iOS Client ID** in your `Info.plist` (Google Sign-In configuration).

## 🚀 Getting Started

1.  Clone the repository.
2.  Open the project in Android Studio (Ladybug or newer).
3.  Sync Gradle.
4.  Run the `androidApp` or `iosApp` configuration.
