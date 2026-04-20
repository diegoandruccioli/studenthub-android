# StudentHub Android

> Android mobile app inspired by the StudentHub project. Track exams, credits and GPA with gamification rewards. Built with Kotlin, Clean Architecture (MVVM), Room, Coroutines and Flow. UniBO Mobile Systems 2025/26.

---

## Overview

**StudentHub** is a native Android application for managing your university career. Students can track exams, CFU (credits), GPA, and academic progress, while the app rewards completed activities (passed exams, reached milestones) with a gamification points system.

This project is the Android mobile development of the original StudentHub web project, built as the final project for the **Sistemi Mobili** course at the University of Bologna (2025/2026).

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin (100%) |
| Architecture | Clean Architecture — Multi-module |
| UI Pattern | MVVM (ViewModel + StateFlow/LiveData) |
| UI Toolkit | Views / XML + ViewBinding → Jetpack Compose (planned) |
| Async | Kotlin Coroutines + Flow |
| Local DB | Room (structured data) |
| Preferences | DataStore (Preferences) |
| Image loading | Glide |
| Min SDK | 31 (Android 12) |
| Target SDK | 36 |

---

## Architecture

The project follows **Clean Architecture** with strict module separation:

```
:app        → Entry point, Application class, manual DI (RepositoryProvider)
:ui         → Activities, Fragments, ViewModels, Adapters, Custom Views
:domain     → Use Cases, Repository interfaces, Domain Models (pure Kotlin, no Android)
:data       → Repository implementations, Room DAOs/Entities, DataStore
```

**Module dependency rules:**
```
:app  →  :ui, :domain, :data
:ui   →  :domain
:data →  :domain
:domain  →  (no internal dependencies)
```

**Data flow:**
```
View (Fragment/Activity)
  └── ViewModel  [:ui]
        └── UseCase  [:domain]
              └── Repository interface  [:domain]
                    └── RepositoryImpl  [:data]
                          └── Room DAO / DataStore
```

---

## Features

- **Exam tracking** — add, edit and view your exams with grade and CFU
- **Academic dashboard** — GPA, total CFU, progress toward degree
- **Gamification** — points and rewards for completed academic milestones
- **Local persistence** — all data stored locally with Room; preferences via DataStore
- *(Planned)* Authentication and sync with StudentHub backend (Node.js / MySQL)

---

## Project Structure

```
studenthub-android/
├── app/
│   └── src/main/java/com/unibo/android/corsolp2526/
│       └── CustomApplication.kt
├── domain/
│   └── src/main/java/com/unibo/android/domain/
│       ├── models/
│       ├── repositories/
│       └── usecases/
├── data/
│   └── src/main/java/com/unibo/android/data/
│       ├── di/
│       └── repositories/
├── ui/
│   └── src/main/java/com/unibo/android/ui/
│       ├── adapters/
│       ├── customs/
│       ├── fragments/
│       ├── HomeActivity.kt
│       └── SplashActivity.kt
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11+
- Android device or emulator with API 31+

### Build & Run

```bash
# Clone the repository
git clone https://github.com/diegoandruccioli/studenthub-android.git

# Open in Android Studio and sync Gradle, or build from CLI:
./gradlew clean build

# Install on connected device/emulator
./gradlew installDebug
```

### Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

---

## Course Requirements Compliance

This project satisfies all mandatory requirements for the UniBO Sistemi Mobili exam:

- [x] Multi-module Clean Architecture (domain / data / ui / app)
- [x] MVVM — ViewModel as state holder, zero business logic in View
- [x] ViewModel + StateFlow/LiveData — survives configuration changes
- [x] Use Cases in domain layer — single responsibility
- [x] Repository Pattern — single data access point
- [x] Kotlin Coroutines — strict Main/Background thread separation
- [x] Room — local structured data persistence
- [x] DataStore — key-value preferences
- [x] ViewBinding — no `findViewById`
- [x] RecyclerView with Adapter/ViewHolder
- [x] Runtime permissions handling

---

## License

Academic project — University of Bologna, 2025/2026.
