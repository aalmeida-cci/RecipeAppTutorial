# Implementation Plan: Language Selection Bottom Sheet

**Branch**: `004-language-selection` | **Date**: 2026-05-28 | **Spec**: [spec.md](spec.md)

---

## Summary

Add a language selection bottom sheet to ProfileScreen that lets users switch between English and French at runtime. Language choice is persisted to DataStore and restored on cold start. All hardcoded UI strings across all screens migrate to a `LocalAppStrings` CompositionLocal, enabling instant language switching on Apply without system locale changes.

**Tech approach**:
- `AppStrings` interface + `EnStrings`/`FrStrings` objects + `CompositionLocal` for runtime locale
- Generic `DataStoreManager<T>` backed by `androidx.datastore:datastore-preferences-core:1.1.7` (KMP)
- Separate `LanguageViewModel` with full Clean Architecture layers in `features/language/`
- String migration: 25 hardcoded UI strings across 6 screens → `AppStrings` + `strings.xml`/`values-fr/strings.xml`

---

## Technical Context

**Language/Version**: Kotlin 2.3.20, Kotlin Multiplatform

**Primary Dependencies**:
- Compose Multiplatform 1.10.3, Material3 1.10.0-alpha05
- Koin 4.2.1 (Core + Compose + ViewModel)
- DataStore Preferences Core 1.1.7 — new dependency
- Jetbrains Navigation Compose 2.9.2, Lifecycle ViewModel 2.10.0

**Storage**: DataStore Preferences (`app_preferences.preferences_pb`) via `DataStoreManager`

**Testing**: `kotlin.test` (commonTest)

**Target Platform**: Android (minSdk 24) + iOS (iosArm64, iosSimulatorArm64)

**Performance Goals**: Language switch visually instant on Apply (single StateFlow emission)

**Constraints**: All new code in `commonMain`; DataStore path provided platform-specifically via Koin; no platform imports inside `commonMain`

**Scale/Scope**: 2 languages (EN, FR); 6 screens with string migration; ~20 new files; ~10 modified files

---

## Constitution Check

| Gate | Status | Notes |
|------|--------|-------|
| Clean Architecture 5 layers | PASS | `features/language/` has domain/data/ui layers |
| commonMain first | PASS | All Composables, VMs, Repos in commonMain; DataStore path injection via Koin |
| Cache-first strategy | N/A | Preference data — no remote source; DataStore is single source |
| Code reuse / no duplication | PASS | Single `DataStoreManager`; reuse `MaterialTheme.*`, `Res.*` |
| Feature package structure | PASS | `features/language/{domain,data,ui}/`; no nav package (bottom sheet, not a route) |
| No `android.*` in commonMain | PASS | DataStore path provided via Koin from platform modules |
| ViewModels via koinViewModel() | PASS | `LanguageViewModel` registered in `viewModelModule` |
| collectAsStateWithLifecycle | PASS | Required in `ProfileRoute` and `App.kt` |
| No hardcoded colors | PASS | All colors via `MaterialTheme.colorScheme.*` |
| Modifier first optional param | PASS | Applied to `LanguageBottomSheet` composable |

---

## Project Structure

### Documentation (this feature)

```
specs/004-language-selection/
├── plan.md           <- this file
├── research.md       <- Phase 0 output
├── data-model.md     <- Phase 1 output
└── tasks.md          <- Phase 2 output (/speckit-tasks)
```

### Source Code

#### New files

```
composeApp/src/commonMain/kotlin/com/adrian/recipeapp/
├── features/
│   ├── common/
│   │   ├── data/
│   │   │   └── datastore/
│   │   │       └── DataStoreManager.kt
│   │   └── localization/
│   │       ├── AppStrings.kt
│   │       ├── EnStrings.kt
│   │       ├── FrStrings.kt
│   │       └── LocalAppStrings.kt
│   └── language/
│       ├── data/
│       │   ├── datasources/
│       │   │   ├── LanguageDataSource.kt
│       │   │   └── LanguageDataSourceImpl.kt
│       │   └── repositories/
│       │       └── LanguageRepositoryImpl.kt
│       ├── domain/
│       │   ├── entities/
│       │   │   └── AppLang.kt
│       │   └── repositories/
│       │       └── LanguageRepository.kt
│       └── ui/
│           ├── LanguageUiState.kt
│           ├── LanguageViewModel.kt
│           └── LanguageBottomSheet.kt

composeApp/src/androidMain/kotlin/com/adrian/recipeapp/
└── di/
    └── DataStoreModule.android.kt

composeApp/src/iosMain/kotlin/com/adrian/recipeapp/
└── di/
    └── DataStoreModule.ios.kt

composeApp/src/commonMain/composeResources/
├── values/strings.xml              <- add 25 new string keys
└── values-fr/strings.xml           <- new file: French translations
```

#### Modified files

```
gradle/libs.versions.toml
composeApp/build.gradle.kts
composeApp/src/commonMain/.../App.kt
composeApp/src/commonMain/.../di/DataModule.kt
composeApp/src/commonMain/.../di/ViewModelModule.kt
composeApp/src/androidMain/.../MainApplication.kt (or Koin.kt)
composeApp/src/iosMain/.../KoinIos.kt
composeApp/src/commonMain/.../features/profile/ui/ProfileScreen.kt
composeApp/src/commonMain/.../features/feed/ui/FeedScreen.kt
composeApp/src/commonMain/.../features/search/ui/SearchScreen.kt
composeApp/src/commonMain/.../features/detail/ui/DetailScreen.kt
composeApp/src/commonMain/.../features/favourites/ui/FavouriteScreen.kt
composeApp/src/commonMain/.../features/common/ui/components/ErrorContent.kt
```

**Structure Decision**: KMP mobile app (Option 3). New feature code under `commonMain/features/`; platform-specific DataStore path provision in `androidMain`/`iosMain` DI modules.

---

## Implementation Sequence

### Phase A — Infrastructure

1. Add `datastore-preferences-core:1.1.7` to `libs.versions.toml` + `build.gradle.kts`
2. Create `DataStoreManager` in `features/common/data/datastore/`
3. Create `DataStoreModule.android.kt` (DataStore<Preferences> with `applicationContext.filesDir` path)
4. Create `DataStoreModule.ios.kt` (DataStore<Preferences> with `NSHomeDirectory()` path)
5. Register `DataStore<Preferences>` and `DataStoreManager` in `dataModule()` + wire platform modules into each platform's `initKoin`
6. Create `AppStrings` interface, `EnStrings` object, `FrStrings` object
7. Create `LocalAppStrings` (`compositionLocalOf { EnStrings }`) + `AppLang.strings` extension val
8. Update `values/strings.xml` (add 25 new keys)
9. Create `values-fr/strings.xml` (French translations for the same 25 keys)

### Phase B — Language Feature Layers

10. Create `AppLang` enum (`EN("en","English")`, `FR("fr","French")`, `fromCode()`, `default`)
11. Create `LanguageRepository` interface (`getSelectedLang(): Flow<Result<AppLang>>`, `saveSelectedLang(): Result<Unit>`)
12. Create `LanguageDataSource` interface + `LanguageDataSourceImpl` (wraps `DataStoreManager`, key = `stringPreferencesKey("selected_language_code")`)
13. Create `LanguageRepositoryImpl` (maps String → AppLang via `fromCode`, wraps in `Result`)
14. Register Language datasource + repository in `dataModule()`

### Phase C — Language ViewModel + UI

15. Create `LanguageUiState` (`currentLang`, `pendingLang`, `isBottomSheetVisible`, `isLoading`)
16. Create `LanguageViewModel` (init loads persisted lang; exposes `onLanguageRowTapped`, `onLanguageSelected`, `onApply`, `onDismiss`)
17. Register `LanguageViewModel` in `viewModelModule()`
18. Create `LanguageBottomSheet` composable (Material3 `ModalBottomSheet`, radio list, Apply button)
19. Update `ProfileRoute` to inject `LanguageViewModel`, pass state to `ProfileScreen`, conditionally show `LanguageBottomSheet`
20. Update `ProfileScreen` settings row tap handler for Language row

### Phase D — App-Level Locale Wiring

21. Update `App.kt`: inject `LanguageViewModel` via `koinViewModel()`, collect `currentLang`, wrap `AppNavHost` in `CompositionLocalProvider(LocalAppStrings provides langState.currentLang.strings)`

### Phase E — String Migration

22. Migrate `FeedScreen.kt` — replace 4 hardcoded strings with `LocalAppStrings.current.*`
23. Migrate `SearchScreen.kt` — replace 3 strings
24. Migrate `DetailScreen.kt` — replace 5 strings
25. Migrate `FavouriteScreen.kt` — replace 1 string
26. Migrate `ProfileScreen.kt` — replace 8 strings (title + 7 settings labels)
27. Migrate `ErrorContent.kt` — replace 1 string

---

## Key Design Contracts

### LanguageViewModel public API

```kotlin
val uiState: StateFlow<LanguageUiState>
fun onLanguageRowTapped()
fun onLanguageSelected(lang: AppLang)
fun onApply()
fun onDismiss()
```

### LanguageBottomSheet composable signature

```kotlin
@Composable
fun LanguageBottomSheet(
    modifier: Modifier = Modifier,
    uiState: LanguageUiState,
    onLanguageSelected: (AppLang) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
)
```

### App.kt integration pattern

```kotlin
@Composable
fun App() {
    val langViewModel: LanguageViewModel = koinViewModel()
    val langState by langViewModel.uiState.collectAsStateWithLifecycle()
    RecipeAppTheme {
        CompositionLocalProvider(LocalAppStrings provides langState.currentLang.strings) {
            val navController = rememberNavController()
            val appState = rememberAppState(navController)
            AppNavHost(appState = appState)
        }
    }
}
```

### ProfileRoute integration pattern

```kotlin
@Composable
fun ProfileRoute(modifier: Modifier = Modifier) {
    val profileViewModel: ProfileViewModel = koinViewModel()
    val langViewModel: LanguageViewModel = koinViewModel()
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val langState by langViewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(uiState = uiState, langState = langState,
        onLanguageRowTap = langViewModel::onLanguageRowTapped, modifier = modifier)

    if (langState.isBottomSheetVisible) {
        LanguageBottomSheet(
            uiState = langState,
            onLanguageSelected = langViewModel::onLanguageSelected,
            onApply = langViewModel::onApply,
            onDismiss = langViewModel::onDismiss
        )
    }
}
```

---

## Complexity Tracking

### Documented Deviations (constitution PATCH amendment required — see below)

**Deviation 1 — `navigation/` package omitted** (Constitution V)
Bottom sheet is shown/hidden via `LanguageUiState.isBottomSheetVisible`, not via `NavController`. There is no nav route, no `NavGraphBuilder` extension, and no route constant to define. Creating an empty `navigation/` package would be dead code, violating Principle IV (No Duplication / no half-finished implementations). Constitution PATCH amendment filed below.

**Deviation 2 — Cache-first pattern not applicable** (Constitution III)
`LanguageRepositoryImpl` wraps a single DataStore source. There is no remote API for language preference, so the LocalDataSource→RemoteDataSource→persist pattern is structurally inapplicable. DataStore IS the persistent store; there is nothing to cache from or to. Constitution PATCH amendment filed below.

**Deviation 3 — `data/models/` package omitted** (Constitution V)
No `@Serializable` DTO is needed — language preference is stored as a raw `String` key in DataStore, not as a serialized API response. A DTO + mapper with a single `String` field would be a trivial wrapper with no value. Omission documented here per constitution deviation procedure.

### Constitution PATCH Amendment — v1.0.2

> **Amendment 1.0.2 — 2026-05-28**
> Added two bounded exceptions to Principles III and V for preference-only and bottom-sheet-only features.
>
> **Principle III exception**: A repository that wraps a local-only preference store (e.g., DataStore, SharedPreferences) with no corresponding remote source is exempt from the cache-first pattern. The pattern requires a remote source to cache from; when none exists by design, the requirement is inapplicable. The repository MUST still return `Result<T>` and MUST NOT throw across the boundary.
>
> **Principle V exception**: A feature whose sole UI surface is a modal bottom sheet invoked via parent UiState (not via `NavController`) MAY omit the `navigation/` package. Conditions: (1) no nav route exists for the feature; (2) the plan's Complexity Tracking documents the omission; (3) if a nav route is added in a future iteration, a `navigation/` package MUST be introduced at that time.
>
> **Principle V exception**: A feature with no remote data source and no API DTO MAY omit `data/models/`. Conditions: (1) all persistence keys are primitives (String, Int, Boolean); (2) no `toEntity()` mapper is needed; (3) documented in Complexity Tracking.
>
> Affected principles: III, V. Templates: none. Version: 1.0.1 → 1.0.2.
