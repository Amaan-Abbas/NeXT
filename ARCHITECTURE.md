# Architecture & Design System Specification: Todo + Pomodoro

This document serves as the **authoritative architectural contract and design system specification** for the completed Todo + Pomodoro application prototype.

---

## 1. Architectural Overview

The application follows **MVVM (Model-View-ViewModel)** combined with **Clean Architecture principles**, the **Repository Pattern**, **Unidirectional Data Flow (UDF)**, **Dependency Injection**, and **Reactive State Management**.

```text
+---------------------------------------------------------------------------------+
|                                PRESENTATION LAYER                               |
|  Jetpack Compose UI (Screens, TaskCard, TaskFormDialog) <--- UiState (StateFlow)|
|         │                                                            ▲          |
|         ▼ (UiEvents)                                                 │          |
|     ViewModel ───────────────────────────────────────────────────────┘          |
+---------------------------------------│-----------------------------------------+
                                        │
                                        ▼
+---------------------------------------------------------------------------------+
|                                  DOMAIN LAYER                                   |
|                Use Cases (SaveTaskUseCase, SavePomodoroSessionUseCase)          |
|            PomodoroTimerEngine (Global State Engine) & Domain Models            |
|                           Repository Interfaces                                 |
+---------------------------------------│-----------------------------------------+
                                        │
                                        ▼
+---------------------------------------------------------------------------------+
|                                   DATA LAYER                                    |
|              Repository Implementations (TaskRepositoryImpl, etc.)              |
|        Local Data Sources (TaskLocalDataSource, PomodoroLocalDataSource)        |
|               Room Database & DAOs (TaskDao, PomodoroSessionDao)                |
+---------------------------------------------------------------------------------+
```

---

## 2. Core Architectural Principles

1. **Strict Layer Boundaries**:
   $$\text{UI} \longrightarrow \text{ViewModel} \longrightarrow \text{Domain} \longrightarrow \text{Repository} \longrightarrow \text{Local Data Source} \longrightarrow \text{Room DAO}$$
   Lower-level Room DAOs and entities NEVER leak into ViewModels or UI composables.
2. **Single Responsibility & Reusability**:
   - UI dialog logic for creating and editing tasks is consolidated into a single reusable component: `TaskFormDialog`.
   - String utilities use standard Kotlin standard library methods (`isNullOrBlank()`).
3. **Room & KSP Rules**:
   - Room compiler configured strictly using KSP (`ksp(libs.androidx.room.compiler)`).
   - `@Query` suspend update/delete methods in DAOs explicitly declare `: Int` return types to prevent JVM signature mismatches during KSP code generation.
4. **Single Source of Truth**:
   - Persistent task and session state is owned by Room Database.
   - Live timer engine state is owned by `PomodoroTimerEngine` (singleton scope in `AppContainer`).
   - Screen presentation state is owned by feature ViewModels exposing immutable `StateFlow<UiState>`.
5. **Deadline-Based Timer Engine**:
   - `PomodoroTimerEngine` calculates remaining time using deadline-based target timestamps (`targetEndTimeMillis - currentTime`).
   - Timer correctness is independent of UI recomposition, configuration changes, screen locking, or app backgrounding.

---

## 3. Package Structure

```text
com.example.next/
├── MainActivity.kt               # Entry activity (unlocked screen orientation, edge-to-edge)
├── NextApplication.kt           # Application instance initializing AppContainer
├── core/
│   ├── common/                  # Result<T> wrapper, DispatcherProvider
│   ├── di/                      # AppContainer (Dependency Injection)
│   ├── navigation/              # Screen destinations, AppNavigation graph
│   └── ui/                      # Design system components
│       ├── components/          # TaskCard, TaskFormDialog, PriorityChip, AppNavBar, TopBar
│       └── theme/               # Shape.kt
├── data/
│   ├── local/                   # AppDatabase, TaskDao, PomodoroSessionDao, HomeDao
│   │   ├── dao/
│   │   └── entity/              # TaskEntity, PomodoroSessionEntity, HomeItemEntity
│   ├── mapper/                  # TaskMapper, PomodoroSessionMapper
│   └── remote/                  # HomeApiService
├── features/
│   ├── tasks/                   # Tasks feature (Task, TaskRepository, TasksScreen, TasksViewModel)
│   ├── pomodoro/                # Pomodoro feature (PomodoroSession, PomodoroTimerEngine, PomodoroScreen, ViewModel)
│   ├── settings/                # Settings feature (UserSettings, SettingsScreen, SettingsViewModel)
│   ├── home/                    # Overview/Dashboard feature
│   └── detail/                  # Topic detail feature
└── ui/theme/                    # Color.kt, Theme.kt, Type.kt
```

---

## 4. Feature Integration & Task/Pomodoro Relationship

### Task-Bound & Standalone Focus Sessions
- **Decoupled Integration**: Integration between Tasks and Pomodoro relies solely on primitive task identifiers (`taskId: String?`).
- **Navigation Protocol**:
  1. Tapping "Focus" on a `TaskCard` navigates to `Screen.Pomodoro.createRoute(taskId)`.
  2. `PomodoroScreen` displays active task details ("FOCUSED TASK") and binds session tracking to `taskId`.
  3. When a focus session finishes (`isCompleted == true`), `SavePomodoroSessionUseCase`:
     - Inserts completed `PomodoroSession` into `pomodoro_sessions` Room table.
     - Atomically increments `completedPomodoroSessions` on `TaskEntity` in `tasks` table.
     - Automatically bumps target estimated pomodoros if actual completed sessions exceed initial target.
  4. Interrupted or skipped sessions record `isCompleted = false` and `isInterrupted = true` without incrementing completed task session progress.
  5. Standalone Pomodoro sessions (`taskId == null`) run independently without task association.

---

## 5. Design System, Accessibility & Material 3 Alignment

### Color System & Theme Contrast
- Centralized `ColorScheme` in `Theme.kt` supporting Light Mode, Dark Mode, and Dynamic Color (Android 12+).
- **Theme-Aware Priority Colors**: `PriorityChip` uses distinct container/text colors in dark mode (`PriorityLowDarkContainer`, `PriorityMediumDarkContainer`, `PriorityHighDarkContainer`) to prevent low-contrast or blinding light containers.
- **Accessibility Rule Compliance**:
  - `PriorityChip` displays directional visual icons alongside text labels (`ArrowDownward` for Low, `Remove` for Medium, `ArrowUpward` for High), ensuring priority is readable without relying on color alone.
  - Interactive components adhere to minimum 48dp x 48dp touch target requirements across buttons, checkboxes, and icon actions.
- **Responsive Layout Architecture**:
  - Screen orientation is unlocked in `AndroidManifest.xml`.
  - `PomodoroScreen` features adaptive dual-column layout for landscape and tablet dimensions.
  - `TasksScreen` and `TaskDetailScreen` utilize scrollable containers preventing clipping across device sizes and scale factors.

---

## 6. Verification & Test Suite

- **Unit Testing**:
  - `TasksViewModelTest`: Task loading, tab selection (`ACTIVE` vs `COMPLETED`), priority filtering, sorting, deletion, and undo actions.
  - `TaskDetailViewModelTest`: Detail loading, task editing, completion toggle, and deletion events.
  - `PomodoroTimerEngineTest`: Timer countdown, mode transitions (`FOCUS` $\rightarrow$ `SHORT_BREAK` $\rightarrow$ `LONG_BREAK`), pause, resume, reset, and skip behaviors.
  - `SavePomodoroSessionUseCaseTest`: Verifies that focus sessions increment completed task count while interrupted/break sessions do not.
  - `TaskMapperTest` & `PomodoroSessionMapperTest`: Roundtrip domain $\leftrightarrow$ entity conversion.
- **Compose UI Testing**:
  - `TasksScreenTest`: Automated UI tests verifying element rendering, FAB interaction, task cards, and button semantics.
- **Build Commands**:
  ```powershell
  # Execute unit test suite
  ./gradlew testDebugUnitTest

  # Execute clean debug build
  ./gradlew assembleDebug

  # Execute release build
  ./gradlew assembleRelease
  ```

---

## 7. Scope & Known Prototype Limitations

- **Prototype Scope**:
  - Timer audio feedback utilizes system default notification ringtone without requiring custom audio assets.
  - Local database seeding provides initial sample data for demonstration.
- **Production Scope Considerations**:
  - Cloud synchronization or account authentication is out of scope for this offline-first prototype.
