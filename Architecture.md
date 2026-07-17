# ForgeGym Architecture

ForgeGym is built using **Clean Architecture** and **MVVM (Model-ViewModel-ViewModelState)** principles. The application is designed to be highly reactive, resilient, and offline-first.

## 🧱 Layered Architecture

### 1. Data Layer (`data`)
*   **Entities**: Room entities represent the SQLite schema.
*   **DAOs**: Data Access Objects handle SQL queries with indexed performance.
*   **Preferences**: Jetpack DataStore manages key-value user settings (Theme, Units, etc.).
*   **Mappers**: Isolated layer that converts database entities to clean domain models.

### 2. Domain Models (`models`)
*   Immutable data classes that define the "Combat Language" of the app.
*   Includes Workouts, Exercises, Sessions, and Analytics schemas.

### 3. Repository Layer (`repository`)
*   The single source of truth for the ViewModels.
*   Handles data orchestration between multiple DAOs.
*   Performs heavy mathematical heavy-lifting (Volume aggregation, 1RM calculations, Stress ratios).

### 4. ViewModel Layer (`viewmodel`)
*   Follows a **Unidirectional Data Flow (UDF)** pattern.
*   Exposes a single `StateFlow<UiState>` to the UI.
*   Handles user events via an `onEvent(Event)` bridge.
*   Uses `WhileSubscribed(5000)` to efficiently manage memory and resources.

### 5. UI Layer (`ui`)
*   **Screens**: Composable functions that define the layout for each feature.
*   **Components**: Highly reusable atoms (Cards, Buttons, Charts).
*   **Theme**: Centralized design system following Material 3 guidelines and AMOLED-specific coloring.

## 🔄 Reactive Flow

1.  **User Action**: The user taps "Complete Set".
2.  **ViewModel Event**: UI triggers `onEvent(OnCompleteSet)`.
3.  **Repository Update**: ViewModel calls `repository.logSet(...)`.
4.  **Database Persistence**: Repository performs an atomic Room update.
5.  **Reactive Update**: Room triggers a new emission in the `Flow`.
6.  **State Recomposition**: ViewModel combines flows into a new `UiState`, and Compose updates the screen.

## ⚡ Performance Optimization
*   **SQLite Push-Down**: Filtering and searching are performed at the database level using SQL `LIKE` and `Index` scans.
*   **Background Processing**: All data transformations happen in the `IO` dispatcher.
*   **Stable Models**: Use of immutable models ensures Compose skips unnecessary recompositions.
