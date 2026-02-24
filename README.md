# SwipeCounter 📈

SwipeCounter is a sophisticated Android application designed to track and analyze user swipe gestures. Built with modern Android development practices, it provides real-time statistics, beautiful visualizations, and a premium experience.

## ✨ Features

- **Gesture Tracking**: Accurately counts and categorizes swipes (up, down, left, right).
- **Beautiful Analytics**: Interactive charts powered by [Vico](https://github.com/patrykandpatrick/vico) showing your swipe trends.
- **Accessibility Service**: Runs efficiently in the background using Android Accessibility Services.
- **Premium Features**: Integrated with **RevenueCat** for seamless subscription management.
- **Ad Integration**: Optimized **AdMob** implementation with secure configuration.
- **Modern UI**: Built entirely with **Jetpack Compose** following Material 3 guidelines.

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Persistence**: Room Database & DataStore
- **Dependency Injection**: KSP / Modern Android patterns
- **Analytics/Subscriptions**: RevenueCat
- **Monetization**: Google AdMob
- **Architecture**: MVVM (Model-View-ViewModel)

## 🚀 Getting Started

### Prerequisites

- Android Studio Koala or newer
- JDK 17+
- Android SDK 35 (Compile SDK)

### Security Configuration (IMPORTANT)

This project uses a secure configuration method to keep API keys safe. To run the project, you **must** create a `local.properties` file in the root directory and add the following keys:

```properties
REVENUECAT_API_KEY=your_revenuecat_key_here
ADMOB_APP_ID=your_admob_app_id_here
ADMOB_BANNER_ID=your_admob_banner_id_here
```

The app will read these keys during build time and inject them via `BuildConfig`.

## 📸 Screenshots

*(Add your app screenshots here to show off the UI!)*

## 📄 License

Copyright © 2026 Emre Algür. All rights reserved.
