# Star Path Tracker - Disney Dreamlight Valley

An Android checklist application designed for Disney Dreamlight Valley players to track their Star Path duties and routine weekly duties.

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?logo=jetpackcompose&logoColor=white)

---

## Features

- **Live & Bundled Star Path Guides**: Scrapes the official IGN Dreamlight Valley Star Path wiki index to retrieve all duty lists.
- **Active Star Path Dropdown**: Easily switch between all historical and current Star Paths with the latest path selected by default.
- **Dual Tab Control**:
  - **Path Tab**: Shows the regular main Star Path duties (with real-time count badges).
  - **Weekly Tab**: Separates and displays routine/weekly duties grouped by week.
- **Show / Hide Completed Duties**: Toggle button and top bar icon to instantly hide finished duties or review them with strike-through styling.
- **Progress Tracking & Token Counter**: Live completion percentage progress bar and cumulative token tracker.
- **Instant Search & Sort**: Filter duties by name, requirement, or hint; sort by token rewards or alphabetical order.
- **Offline First & Persistent State**: Automatically caches scraped guides and persistently saves your completed checkmarks across launches and preset switches.

---

## Tech Stack

- **UI**: Jetpack Compose with Material 3 (Celestial Dreamlight theme)
- **Architecture**: MVVM with unidirectional data flow (`StateFlow`, Coroutines)
- **Scraper**: [Jsoup](https://jsoup.org/) for resilient HTML parsing of duty tables and sections
- **Serialization**: `kotlinx.serialization` for caching and state persistence
- **Storage**: `SharedPreferences` + internal JSON file caches

---

## Building and Running

1. Clone the repository:
   ```bash
   git clone https://github.com/blaynewatt/Starpath.git
   ```
2. Open in Android Studio or build with Gradle:
   ```bash
   ./gradlew assembleDebug
   ```
3. Run on a connected device:
   ```bash
   ./gradlew installDebug
   ```
