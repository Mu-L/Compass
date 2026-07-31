# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Simple compass app for Android (Kotlin + Jetpack Compose), published on F-Droid and Google Play.
Package: `com.bobek.compass`.

## Build & Test Commands

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run all tests, auto-creating/booting/shutting down a Test_Phone AVD for the instrumented ones
bundle exec fastlane android test

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing env vars)
bundle exec fastlane android apk

# Deploy to Google Play (requires signing env vars + JSON key)
fastlane android deploy

# Run lint
./gradlew lint

# Screenshots via Fastlane; each lane grabs a light (1.png) and dark (2.png) shot
bundle exec fastlane android grab_screens               # creates Screenshots_* AVDs if missing, boots each in turn
bundle exec fastlane android setup_screenshot_emulators # just (re-)create the Screenshots_* AVDs, without grabbing screenshots
bundle exec fastlane android grab_screen_phone          # requires a connected/already-running device
bundle exec fastlane android grab_screen_seven_inch
bundle exec fastlane android grab_screen_ten_inch
```

Fastlane release builds require env vars: `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
Google Play deployment additionally requires `ANDROID_JSON_KEY_FILE`.

## Architecture

The app follows MVVM in a single-Activity Compose setup:

- **`CompassApplication`** — Hilt entry point
- **`MainActivity`** — Single Compose activity; hosts `AppViewModel` and `CompassViewModel`; registers sensor and
  location listeners; handles `ACCESS_LOCATION` permission workflow
- **`AppViewModel`** — Night mode preference via `StateFlow`; reads from `SettingsRepository`
- **`CompassViewModel`** — All compass UI states via `StateFlow`; loaded from `SettingsRepository` on init
- **`ICompassViewModel`** / **`ComposeCompassViewModel`** — Interface + preview implementation used by all Compose
  screens

### Key Packages

| Package     | Responsibility                                                                                                                 |
|-------------|--------------------------------------------------------------------------------------------------------------------------------|
| `data/`     | Immutable data models: `Azimuth`, `CardinalDirection`, `SensorAccuracy`, `LocationStatus`, `AppNightMode`, `AppError`          |
| `settings/` | `DataStoreSettingsRepository` — persists preferences via Jetpack DataStore; migrates from SharedPreferences; injected via Hilt |
| `ui/`       | Jetpack Compose screens: `compass/`, `settings/`, `licenses/`, `theme/`                                                        |
| `util/`     | `MathUtils` — azimuth calculation, magnetic declination, haptic feedback interval helpers                                      |

### Data Flow

Sensor events → `MainActivity` → `CompassViewModel` (StateFlow) → Compose UI

Settings changes are debounced 1 second before being written to DataStore.

### Location Handling

`MainActivity` uses `requestLocationUpdates` (not `getCurrentLocation`) to acquire a single location fix, then removes
the listener immediately after the first result. Location permission is requested once via `registerForActivityResult`.
The `repeatOnLifecycle(RESUMED)` block re-triggers location handling whenever `trueNorth` changes.

## Tech Stack

- **UI:** Jetpack Compose + Material3, Navigation Compose
- **DI:** Hilt + KSP
- **Persistence:** DataStore Preferences
- **Sensors:** Android `SensorManager` (rotation vector + magnetic field)
- **Build:** AGP 9.x, Kotlin 2.3.x, Java 11 toolchain
- **Testing:** JUnit4, Compose UI Test, kotlinx-coroutines-test, Fastlane Screengrab

## Branch Notes

`master` is the main branch used for releases and PRs.

## Translations

Translations are managed via Weblate. Do not manually edit `strings.xml` files in locale-specific resource directories —
changes come in through automated PRs from Weblate.
