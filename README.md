# ForgeGym 🏋️‍

ForgeGym is a premium, professional training journal for serious athletes who demand data-driven insights without the clutter. Built with a "Privacy-First" philosophy, ForgeGym works 100% offline, keeping your training legacy on your device where it belongs.

##  Key Features

*   **Combat Hub**: A unified dashboard showing your active session, weekly streaks, goals, and training load.
*   **Live Workout Tracker**: Resilient session engine with pause/resume, rest timers, and real-time volume tracking.
*   **Professional Exercise Library**: 500+ exercises with instructions, pro tips, and historical performance tracking.
*   **Progressive Overload Engine**: Deterministic recommendations for weight and reps based on scientific progression strategies (Linear, Double Progression).
*   **Advanced Analytics**: Weekly/Monthly volume charts, muscle group distribution, and training heatmaps.
*   **Data Sovereignty**: Complete ownership with JSON Backup/Restore and CSV export for Excel/Sheets.
*   **Premium AMOLED Design**: Optimized for modern displays with high-contrast surfaces and smooth Material 3 animations.

##  Tech Stack

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Architecture**: Clean Architecture + MVVM + Unidirectional Data Flow (UDF)
*   **Persistence**: Room (SQL) for training data, Jetpack DataStore for preferences.
*   **Dependency Injection**: Hilt
*   **Concurrency**: Coroutines & Flow
*   **Visualization**: Custom Canvas-based charting
*   **Serialization**: Kotlinx Serialization (JSON)

##  Project Structure

```text
com.example.forgegym
├── data
│   ├── local          # Room entities, DAOs, and DataStore
│   ├── models         # Immutable domain models
│   ├── repository     # Repository implementations
│   └── sample         # Database seeding logic
├── di                 # Hilt modules
├── navigation         # Compose Navigation logic
├── ui
│   ├── components     # Reusable UI atoms
│   ├── screens        # Feature-level screens
│   └── theme          # Design system (Color, Type, Spacing)
├── util               # Shared utilities and Haptic engine
└── viewmodel          # MVI-style ViewModels
```

##  Privacy & Security

ForgeGym does not collect, track, or share your data.
*   **No Cloud Accounts**: No passwords to remember.
*   **No Tracking**: No analytics or third-party ads.
*   **No Internet Required**: Works perfectly in a basement gym or airplane mode.

##  License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
