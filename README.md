# YouPlayer (`youplayer.apk`)

[![License: MIT](https://img.shields.io/badge/License-MIT-red.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20PWA-green.svg)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)]()
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blueviolet.svg)]()

**YouPlayer** is a clean, modern, privacy-friendly, open-source video streaming player for Android and PWA. It requires **zero YouTube API keys**, relying on decentralized, privacy-focused Invidious and Piped public APIs with local Room database persistence, continuous autoplay, favorites, watch history, and offline mode.

---

## 🌟 Key Features

1. **Zero YouTube API Keys Required**
   - Automatically queries decentralized Invidious / Piped instances with intelligent failover.
   - Built-in resilient catalog so you are never left with broken feeds or empty screens.
2. **Modern YouTube Dark Mode UI**
   - Pure OLED dark aesthetic (`#0F0F0F`), YouTube Red accents (`#FF0033`), crisp typography, and fluid transitions.
   - Category chips ("All", "Trending", "Music", "Gaming", "Tech", "News", "Live").
3. **High-Performance Embedded Video Player**
   - Embedded HTML5/iframe player (`youtube-nocookie.com`) supporting custom controls.
   - Fullscreen landscape mode with 1-tap toggle.
   - Continuous playback (Autoplay next related video).
   - Playback speed control (0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x).
   - Mini-player dock bar for seamless background browsing across tabs.
4. **Local Storage (No Login Needed)**
   - Room Database persists:
     - **Saved Favorites** (Bookmarks)
     - **Watch History** (with progress & 1-tap clear history)
     - **Offline Cache** (Offline access without an internet connection)
   - 100% private: no tracking, no Google Account sign-in required.
5. **Real-Time Push Notifications**
   - System Notification Channel (`YouPlayer Alerts`) for trending videos and background updates.
   - Supports runtime permission requests on Android 13+ (Tiramisu).
6. **Secure Stripe In-App Supporter Integration**
   - In-app supporter and tipping flow with Stripe simulation (`pk_test_...`) and instant VIP badge activation.
7. **Firebase Cloud Readiness**
   - Configured with Firebase App Check and anonymous auth capability, gracefully defaulting to 100% local offline Room storage.
8. **In-App Diagnostic Logging System**
   - High-speed memory ring buffer recording HTTP calls, player events, and errors.
   - Live modal bottom sheet to view, filter, and clear diagnostic logs.
9. **Responsive Design & Window Size Classes**
   - Compact phones (Bottom Navigation, portrait/landscape video scaling).
   - Tablets & Foldables (Navigation Rail, List-Detail side-by-side view).
   - PWA responsive media queries for web browsers.

---

## 🛠️ Build & Packaging Instructions

### Method 1: Android Studio (Native APK / AAB)

1. **Prerequisites**:
   - Android Studio Hedgehog (or newer)
   - JDK 17 or JDK 21
   - Android SDK 34/36

2. **Clone and open**:
   ```bash
   git clone https://github.com/your-username/youplayer.apk.git
   cd youplayer.apk
   ```

3. **Build the Debug APK**:
   ```bash
   gradle assembleDebug
   ```
   The APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

4. **Build the Release Bundle / APK**:
   ```bash
   gradle assembleRelease
   ```

5. **Run Local JVM Unit & Robolectric Tests**:
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

### Method 2: WebIntoApp (No-Code Cloud APK Builder)

You can convert the responsive PWA located in the `/public` directory into a ready-to-install Android APK using WebIntoApp:

1. Host the `/public` directory on GitHub Pages, Netlify, or Vercel (or compress `/public` into a `.zip`).
2. Go to [WebIntoApp.com](https://www.webintoapp.com).
3. Select **"Online URL"** or **"HTML Files"**.
4. Enter App Name: `YouPlayer`.
5. Enter Package Name: `com.youplayer.app`.
6. Upload the app icon from `app/src/main/res/drawable/` or `public/assets/icon-192.png`.
7. Click **"Generate APK"** and download `youplayer.apk`.

---

### Method 3: Capacitor (Cross-Platform APK)

1. Install Capacitor CLI:
   ```bash
   npm install -g @capacitor/cli @capacitor/core @capacitor/android
   ```

2. Initialize Capacitor in the project root:
   ```bash
   npx cap init YouPlayer com.aistudio.youplayer --web-dir=public
   npx cap add android
   npx cap copy
   ```

3. Open and build in Android Studio:
   ```bash
   npx cap open android
   ```

---

## 📂 Architecture Overview

```
app/src/main/java/com/example/
├── MainActivity.kt                # Main ComponentActivity, edge-to-edge, permission handling
├── YouPlayerApplication.kt        # Application class, Room DB singleton, notification channel
├── data/
│   ├── model/
│   │   └── VideoItem.kt           # Video, Comment, Resource sealed state models
│   ├── local/
│   │   ├── Entities.kt            # Room entities: Favorites, WatchHistory, OfflineCache
│   │   ├── YouPlayerDaos.kt       # DAOs for Room with Flow and suspend functions
│   │   └── YouPlayerDatabase.kt   # Room Database class
│   ├── remote/
│   │   ├── PrivacyApiClient.kt    # Invidious & Piped multi-instance client with auto-failover
│   │   └── CuratedVideoCatalog.kt # Built-in high-definition fallback video catalog
│   └── repository/
│       └── VideoRepository.kt     # Unified data repository
├── util/
│   ├── YouPlayerLogger.kt         # In-memory diagnostic ring buffer & logger
│   ├── NetworkMonitor.kt          # Live connectivity detection via ConnectivityManager
│   ├── NotificationHelper.kt      # System notification dispatcher
│   ├── StripePaymentSimulator.kt  # Stripe checkout simulation & supporter perks
│   └── FirebaseConfigHelper.kt    # Firebase cloud storage & auth status
└── ui/
    ├── YouPlayerApp.kt            # Root adaptive scaffold (NavRail on tablet, BottomBar on phone)
    ├── theme/                     # YouTube Dark Mode Color.kt, Theme.kt, Type.kt
    ├── components/
    │   ├── VideoCard.kt           # 16:9 thumbnail, duration badge, avatar, menu
    │   ├── VideoPlayerView.kt     # Embedded HTML5/iframe player, speed, autoplay, fullscreen
    │   ├── MiniPlayer.kt          # YouTube-style floating docked player
    │   ├── VideoDetailView.kt     # Channel row, subscribe, actions, tabs (Up Next, Comments)
    │   ├── StripeCheckoutSheet.kt # Secure payment modal bottom sheet
    │   └── DebugLogSheet.kt       # Live diagnostic log viewer modal
    └── screens/
        ├── HomeScreen.kt          # Hero banner, category chips, video feed
        ├── SearchScreen.kt        # Instant search, search history, suggestions
        ├── LibraryScreen.kt       # Saved favorites, watch history, offline cache
        └── SettingsScreen.kt      # Supporter status, push alert test, diagnostic logs
```

---

## 🔒 Privacy & Open Source Compliance

- **No Google Account or Login Required**: All personal preferences, saved videos, and watch history remain 100% on the device SQLite/Room database.
- **Zero API Keys**: No Google Cloud Console or YouTube Data API v3 keys needed.
- **Open Source**: Licensed under the permissive **MIT License**.

---

## 📄 License

This project is open-source under the [MIT License](LICENSE).
