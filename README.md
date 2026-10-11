# 🌱 ReLeaf — Environmental Awareness Android App

ReLeaf is an Android application designed to encourage sustainable living through air-quality monitoring, environmental action tracking, and community engagement.

## 📥 Download the Android App

**Latest APK:** [Download ReLeaf.apk](output/ReLeaf.apk)

### Installation Instructions

1. Download `ReLeaf.apk` from the `output` folder in this repository.
2. Transfer the APK to your Android phone if you downloaded it on your computer.
3. Open the APK file on your phone.
4. Allow installation from that source if Android asks for permission.
5. Install and launch ReLeaf.
6. Register an account or log in to access the app.

> **Important:** ReLeaf requires a running, reachable backend for authentication and its API-powered features. Installing the APK alone does not start the backend.

## ✨ Features

- 🌍 **AQI Monitoring:** View air-quality information for your location.
- 🌱 **Environmental Actions:** Submit Plant and Water actions with photo proof.
- ⭐ **Impact Tracking:** Track points, levels, and activity streaks.
- 📜 **Action History:** View previously submitted environmental actions.
- 🏆 **Leaderboard:** Compare points and rankings with other users.
- 👤 **User Profile:** Manage your profile and environmental progress.
- 🔐 **Authentication:** Register and log in securely.

## 🛠️ Technology Stack

### Android Frontend
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

## 🔗 Backend Repository

The Django backend is maintained separately.

**Backend source code:** [Backend Link](https://github.com/lalsingh58/Releaf-backend)

### Run the Backend Locally

1. Clone the backend repository:

   ```bash
   git clone BACKEND_REPOSITORY_URL
   ```

2. Open the backend project directory.

3. Create and activate a Python virtual environment.

4. Install the dependencies listed in the backend's `requirements.txt`.

5. Configure the required environment variables, database, and other backend settings according to the backend repository's instructions.

6. Apply migrations and start the Django server:

   ```bash
   python manage.py migrate
   python manage.py runserver
   ```

   These commands assume the backend uses the standard Django project structure and that its environment and database have been configured.

7. Configure the Android app to use the backend's reachable API base URL.

> For local testing, `127.0.0.1` on an Android phone refers to the phone itself, not your development computer. Use an appropriate development connection or a deployed backend URL.

## 🚀 Build the Android APK

To build the debug APK from source, open the Android project in Android Studio and run:

**Windows PowerShell:**

```powershell
.\gradlew.bat assembleDebug
```

The generated APK is normally located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Copy it into the `output` directory and rename it to `ReLeaf.apk` when preparing an updated download.

## 🔒 Security

- Never commit passwords, Django secret keys, database credentials, JWT signing secrets, or private API keys.
- Keep sensitive configuration in environment variables.
- Use HTTPS for a publicly deployed backend.
- A debug APK is intended primarily for testing; prepare a signed release build for public distribution.

## 📌 Project Status

ReLeaf is under active development. Features and backend availability may change as development continues.

## 👨‍💻 Developer

Developed as an environmental awareness and sustainability project.

## 📄 License

Specify a license if you intend to distribute the source code under an open-source license.
