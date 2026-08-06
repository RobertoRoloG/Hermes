<div align="center">

# Hermes

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android SDK](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Database-Room%202.6-4285F4?style=flat-square&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini-8E75FF?style=flat-square&logo=google&logoColor=white)](https://ai.google.dev/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

</div>

---

## Overview

**Hermes** is an intelligent, privacy-focused Android application designed for deterministic daily task scheduling and AI-assisted productivity management. Combining a local Constraint Satisfaction Problem (CSP) engine with Google Gemini API integration, Hermes automates conflict resolution for flexible tasks while maintaining user schedule stability and zero battery drain from continuous background polling.

---

## Tech Stack

- **Language:** Kotlin 2.0+
- **Architecture:** MVVM (Model-View-ViewModel), Unidirectional Data Flow (UDF)
- **UI Framework:** Jetpack Compose, Material Design 3 (Cyberpunk / Modern Dark aesthetic)
- **Asynchronous Execution:** Kotlin Coroutines, StateFlow, SharedFlow
- **Local Persistence:** Room ORM (SQLite) with destructive migration support
- **AI Integration:** Google Gemini Generative Language API
- **Build System:** Gradle (KSP for Room annotation processing)
- **System Components:** `AlarmManager`, `BroadcastReceiver` (`BOOT_COMPLETED`), `FileProvider`

---

## Key Features

- **Local CSP Task Scheduler:** Deterministic gap-allocation algorithm for flexible tasks without automated schedule thrashing.
- **Manual AI Optimization (`✨`):** On-demand schedule optimization using Google Gemini API to prioritize task order intelligently.
- **Expandable Clean Timeline UI:** Minimalist task cards with optional expandable markdown notes and smooth `AnimatedVisibility` transitions.
- **Flexible Duration Management:** Quick preset chips (15m to 24h) and unrestricted custom duration input removing artificial time caps.
- **Optional End-Times & Overnight Tasks:** Native support for open-ended fixed appointments and schedules crossing midnight (past 23:59).
- **Persistent Alarm Scheduling:** `BootReceiver` restores task notifications automatically across device reboots.
- **Excel / CSV Planning Exporter:** Generates UTF-8 BOM CSV reports for daily and weekly performance, auto-saving to local Downloads and triggering native Android share intents.

---

## Prerequisites

- **JDK:** Java Development Kit 17 or higher
- **Android SDK:** Minimum API Level 26 (Android 8.0), Target API Level 34 (Android 14)
- **Build Tool:** Gradle 8.x / 9.x (managed via Gradle Wrapper)
- **API Key:** Active Google Gemini API Key from Google AI Studio

---

## Installation & Setup

1. **Clone Repository:**
   ```bash
   git clone https://github.com/RobertoRoloG/Hermes.git
   cd Hermes
   ```

2. **Configure Environment Variables:**
   Create a `local.properties` file in the project root directory and declare your Gemini API key:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   sdk.dir=C\:\\Users\\YourUser\\AppData\\Local\\Android\\Sdk
   ```

3. **Build Debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```

---

## Usage / Execution

Deploy the debug build directly to an attached Android physical device or emulator via ADB:

```bash
./gradlew installDebug
```

### Key Workflow Actions:
- **Create Task:** Tap `+` FAB on the main screen to create a fixed or flexible task.
- **Toggle Details:** Tap any task card in the timeline to expand optional description notes.
- **Manual Re-Schedule:** Tap `✨` in the top action bar to execute Gemini-assisted CSP task optimization.
- **Export Planning:** Navigate to Statistics, select Daily or Weekly, and tap `Exportar planning a Excel`.

---

## Roadmap

- [ ] Sub-task checklists (interactive item lists within expandable task cards).
- [ ] Task cloning / one-tap replication for recurring un-scheduled events.
- [ ] Subtle color-coded category badges (Personal, Work, Health).
- [ ] Multi-device database backup and restore via encrypted JSON.
