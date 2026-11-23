# TwinSpace – Multi-Window Manager (Native Kotlin Android Project)

TwinSpace is a native Android application built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Android SDK 35**, **PackageManager**, and **Android Multi-Window APIs** (`FLAG_ACTIVITY_LAUNCH_ADJACENT`, `FLAG_ACTIVITY_NEW_TASK`, `FLAG_ACTIVITY_MULTIPLE_TASK`, and hardware-gated `ActivityOptions.setLaunchBounds`).

## Core Architecture

- **Real Installed App Discovery (`InstalledAppsRepository.kt`)**: Queries `Intent(ACTION_MAIN).addCategory(CATEGORY_LAUNCHER)` via `PackageManager`, loads real application icons into an `LruCache`, sorts A–Z, and supports instant filtering by label or package name.
- **Two-App Pairing (`TwinSpaceViewModel.kt`)**: Lets users pick a First App and Second App, swap slots, clear selection, browse Recent Apps, and prevents selecting the same app twice unless explicitly enabled in Settings.
- **Native Split-Screen Launch (`MultiWindowLauncher.kt`)**: Inspects `Activity.isInMultiWindowMode`, screen width (`smallestScreenWidthDp >= 600`), and `isLowRamDevice`. When TwinSpace is in Split Screen (or on a large screen), launches the Second App into the adjacent pane with `FLAG_ACTIVITY_LAUNCH_ADJACENT | FLAG_ACTIVITY_NEW_TASK` and the First App into the primary pane. On phones in full-screen mode, explains Android's Recents Split Screen requirement honestly rather than faking windows or abusing Accessibility Services.
- **Privacy by Design (`AndroidManifest.xml`)**: Declares **zero `INTERNET` permission** so installed app lists can never leave the device, and includes an explicit first-launch disclosure for `QUERY_ALL_PACKAGES`.

## Prerequisites

- **Android Studio Ladybug (2024.2.1)** or newer
- **JDK 17**
- **Android SDK Platform 35** (Android 15) and Build-Tools 35.0.0

## Build & Run Instructions

### 1. Open in Android Studio
1. Open Android Studio and select **File > Open...**
2. Select the `TwinSpace` folder containing `settings.gradle.kts`.
3. Wait for Gradle Sync to download dependencies (`compose-bom:2024.12.01`, `agp:8.7.3`, `kotlin:2.0.21`).

### 2. Generate the Gradle Wrapper (if building from CLI)
If you do not yet have `gradlew` in this exported folder, run once inside Android Studio Terminal (or with a local Gradle 8.9 installation):
```bash
gradle wrapper --gradle-version 8.9
```

### 3. Build & Install the Debug APK
From Android Studio:
- Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**
- Output path: `app/build/outputs/apk/debug/app-debug.apk`

From CLI:
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 4. Run Unit & Instrumented Tests
```bash
# JVM unit tests (sorting, search, recent list, duplicate guard, API 29-35 compatibility)
./gradlew testDebugUnitTest

# Instrumented Compose UI tests (requires connected device or emulator)
./gradlew connectedDebugAndroidTest
```

### 5. Build a Signed Release APK
1. Create a release keystore (keep it private, never commit to git):
```bash
keytool -genkey -v -keystore twinspace-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias twinspace
```
2. In Android Studio, choose **Build > Generate Signed Bundle / APK... > APK**.
3. Select `twinspace-release.jks`, enter your passwords, choose the `release` build variant (R8 minification + resource shrinking enabled), and click **Create**.
4. Output path: `app/build/outputs/apk/release/app-release.apk`.

## Google Play `QUERY_ALL_PACKAGES` Compliance Note
TwinSpace is a multi-window app-launcher utility whose core purpose requires listing all launchable apps on the user's device. In Google Play Console's App Content declaration:
- Classify TwinSpace under **Device Automation / Launcher Utility**.
- Emphasize that `android.permission.INTERNET` is **not** declared anywhere in the manifest, guaranteeing on-device privacy.
