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
