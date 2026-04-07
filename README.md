# health-checkup-manager

An Android app for managing health checkup results with OCR scanning, trend graphs, and abnormal value alerts.

## Features

- **OCR Scanning**: Capture health checkup result sheets via camera (ML Kit, offline)
- **Manual Input**: Fallback for OCR failures or corrections
- **Dynamic Items**: Flexible schema to support varying test items across medical facilities
- **Trend Graphs**: Year-over-year comparison charts (MPAndroidChart)
- **Alerts**: Notifications for out-of-range values (data display only, no medical advice)
- **Google SSO**: Firebase Authentication

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Platform | Android (Kotlin, min API 26) |
| Camera | CameraX |
| OCR | ML Kit Text Recognition v2 |
| Local DB | Room |
| Graph | MPAndroidChart |
| Auth | Firebase Auth (Google SSO) |

## Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/health-checkup-manager.git
   cd health-checkup-manager/android
   ```

2. **Set up Firebase:**
   - Create a new Firebase project at [https://console.firebase.google.com/](https://console.firebase.google.com/).
   - Add an Android app to your Firebase project with the package name `com.example.healthcheckupmanager`.
   - Download the `google-services.json` file and place it in the `android/app/` directory.
   - Enable Google Sign-In in the Firebase console under **Authentication > Sign-in method**.

3. **Build and run the app:**
   - Open the `android` directory in Android Studio.
   - Let Gradle sync and download the required dependencies.
   - Run the app on an emulator or a physical device.

## Project Structure

```
health-checkup-manager/
├── android/        # Android application (Kotlin)
└── docs/           # Design documents and backlog
```

## Development

See [docs/](docs/) for architecture design and backlog.

## Compliance

This app displays health data only. It does not provide medical diagnosis, treatment advice, or improvement suggestions, in compliance with Japanese pharmaceutical and medical device regulations.
