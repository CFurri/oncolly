# Oncolly - Mobile Health Tracking App

Oncolly is an Android application designed to help patients, particularly those undergoing cancer treatment, track their daily health activities and symptoms. It also provides a platform for doctors to monitor their patients' progress and stay informed about their condition.

## 🚀 Features

The application has two main user roles: Patient and Doctor.

### Patient Features
- **Secure Authentication**: Patients can log in securely to their accounts.
- **Activity Tracking**: Log various types of daily activities, such as:
    - Walking distance or duration
    - Medication intake
    - Symptoms and side effects
- **Activity History**: View a chronological list of all past logged activities.
- **Profile Management**: View and manage personal profile information.

### Doctor Features
- **Secure Authentication**: Doctors have a separate login to access their dashboard.
- **Patient Monitoring**: View a list of assigned patients.
- **Detailed Patient View**: Access a detailed dashboard for each patient, showing their logged activities, symptoms, and other relevant data.
- **Appointment Management**: Schedule and manage patient appointments.

## 🛠️ Tech Stack & Architecture

### Technology Stack
- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for building the native UI.
- **Architecture**: Model-View-ViewModel (MVVM)
- **Asynchronous Operations**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) for managing background threads.
- **Navigation**: [Jetpack Navigation for Compose](https://developer.android.com/jetpack/compose/navigation) to handle in-app navigation.
- **Networking**: [Retrofit](https://square.github.io/retrofit/) for RESTful API communication and [Gson](https://github.com/google/gson) for JSON serialization/deserialization.
- **QR Code Generation**: [ZXing](https://github.com/zxing/zxing) for QR code functionalities.

### Architecture
The application is built using the **MVVM (Model-View-ViewModel)** architectural pattern, promoting a separation of concerns and making the codebase more modular, testable, and maintainable.

It operates on a **Client-Server model**, where the Android app acts as the client. It communicates with a backend server (a separate Spring Boot application) via a RESTful API to handle authentication, data storage, and business logic.

## 📋 Prerequisites

To build and run this project, you will need:
- **Android Studio**: Hedgehog | 2023.1.1 or a newer version.
- **JDK**: Version 11 or higher.
- **Backend Server**: A running instance of the Oncolly backend application. The app will not function without it.

## ⚙️ Setup and Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-username/oncolly.git
    ```
2.  **Open in Android Studio**:
    - Open Android Studio.
    - Select `File > Open` and navigate to the cloned project directory.
3.  **Gradle Sync**:
    - Android Studio will automatically start syncing the project's dependencies via Gradle. Wait for it to complete.
4.  **Configure the Backend URL**:
    - You must configure the API base URL to point to your running backend server. This is set in the `SingletonApp.kt` file.
5.  **Build and Run**:
    - Run the application on an Android emulator or a physical device.

## 🔧 Configuration

The API's base URL is centralized in the project. Before running the app, ensure you update it to match the address of your local or remote backend server.

- **File**: `app/src/main/java/com/teknos/oncolly/singletons/SingletonApp.kt`
- **Note**: The application is configured with `usesCleartextTraffic="true"`, allowing it to connect to a backend via HTTP. For production environments, it is strongly recommended to use HTTPS.

## 📸 Screenshots



