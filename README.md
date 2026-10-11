# 🌱 ReLeaf — Smart Environmental Awareness App

ReLeaf is an Android application designed to encourage environmentally friendly actions through air-quality monitoring, action tracking, and community rankings.

## ✨ Features

- 🌍 **Air Quality Monitoring** — View the Air Quality Index (AQI) for your location.
- 🌱 **Environmental Actions** — Submit Plant and Water actions with photo proof.
- 📊 **Impact Tracking** — Track points, levels, and activity streaks.
- 📜 **My Actions** — Review previously submitted environmental actions.
- 🏆 **Leaderboard** — Compare environmental impact with other users.
- 👤 **User Profile** — Manage your profile and view your progress.
- 🔐 **Authentication** — Register and log in to your account.

## 📱 How to Access ReLeaf

### Option 1: Install the Android APK

1. Open the [ReLeaf GitHub repository](https://github.com/YOUR_USERNAME/ReLeaf).
2. Open the **Releases** section.
3. Download the latest `ReLeaf.apk` file from the release assets.
4. Open the downloaded APK on your Android phone.
5. If prompted, allow installation from that source in Android settings.
6. Complete the installation and open ReLeaf.
7. Register a new account or log in.

**Note:** An APK must be built and uploaded to GitHub Releases before users can install the app this way. The source code alone is not an installable Android application.

### Option 2: Build from Source

**Requirements**
- Android Studio
- Android SDK compatible with the project
- JDK compatible with the project's Gradle configuration
- Internet connection
- Access to the ReLeaf Django backend

**Steps**

1. Clone the repository:

   ```bash
   git clone https://github.com/YOUR_USERNAME/ReLeaf.git
   ```

2. Open the cloned project in Android Studio.
3. Allow Gradle to sync and download the required dependencies.
4. Configure the backend URL in `ApiClient.java`.
5. Ensure the Django backend is running and accessible.
6. Connect an Android phone with USB debugging enabled, or use an emulator.
7. Click **Run** in Android Studio.

## 🛠️ Technology Stack

### Android Application
- Java
- XML layouts
- Android Studio
- Retrofit
- OkHttp
- Gson
- Material Components

### Backend
- Python
- Django
- Django REST Framework
- JWT authentication

## 🔌 Backend Configuration

ReLeaf requires a reachable Django backend for authentication, AQI-related API requests, action submissions, statistics, action history, and leaderboard data.

For local Android development, configure the API base URL appropriately. A physical Android phone cannot normally access the development computer's `127.0.0.1` address directly; use a reachable development-host address or an appropriate ADB reverse configuration.

For production, configure the app to use your deployed HTTPS backend.

**Security:** Never commit Django secret keys, database passwords, JWT signing secrets, private API credentials, or production `.env` files to GitHub.

## 🚧 Project Status

ReLeaf is under active development. Features and API availability may change as development continues.

## 👨‍💻 Developer

Developed as an environmental awareness and sustainability project.

## 📄 License

Add your chosen open-source license here if you intend to distribute the project under one.
