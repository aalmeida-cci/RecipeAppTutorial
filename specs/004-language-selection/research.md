# Research: Language Selection Bottom Sheet

**Branch**: `004-language-selection` | **Date**: 2026-05-28

---

## Decision 1: Runtime Locale Switching Strategy

**Decision**: `AppStrings` interface + `CompositionLocal` pattern for runtime locale switching.

**Rationale**: CMP's `Res.string.*` with `values/strings.xml` + `values-fr/strings.xml` follows the **system locale**, not an in-app selected locale. Since the requirement is to switch language immediately on Apply (independent of system locale), we need a `CompositionLocal<AppStrings>` wrapping the composition tree. `AppStrings` is an interface with two implementations: `EnStrings` and `FrStrings`. The `App.kt` root composable wraps everything with `CompositionLocalProvider(LocalAppStrings provides lang.strings)`. Screens consume `LocalAppStrings.current`.

`values/strings.xml` and `values-fr/strings.xml` are still created as the canonical string source of truth and for system-locale fallback.

**Alternatives considered**:
- `Res.string.*` directly — rejected: only follows system locale, no runtime switching
- Platform-specific `AppLocaleManager` (article approach) — rejected: requires Activity restart on Android (API 33+ `LocaleManager` or `AppCompatDelegate`), complex for KMP; composition-local approach is simpler, pure commonMain

---

## Decision 2: DataStore for Persistence

**Decision**: `androidx.datastore:datastore-preferences-core:1.1.7` (KMP-compatible).

**Rationale**: User explicitly requested DataStore. DataStore 1.1.0+ ships KMP support for Android, iOS, JVM, JS via `PreferenceDataStoreFactory.createWithPath()`. A generic `DataStoreManager` class wraps save/get operations with typed `Preferences.Key<T>` — no hardcoded keys inside the manager.

**Dependency setup**:
- `libs.versions.toml`: add `datastore = "1.1.7"` + `datastore-preferences = { module = "androidx.datastore:datastore-preferences-core", ... }`
- `build.gradle.kts` `commonMain`: add `implementation(libs.datastore.preferences)`
- No platform-specific DataStore deps needed (core module is sufficient for KMP)

**DataStore instance creation**: `PreferenceDataStoreFactory.createWithPath { path }` in `commonMain`. Platform-specific path provided via Koin:
- Android: Koin `androidModule` supplies `filesDir.path + "/app_prefs.preferences_pb"` (has `applicationContext` already via `initKoin`)
- iOS: `KoinIos.kt` supplies `NSHomeDirectory() + "/app_prefs.preferences_pb"`

**Alternatives considered**:
- `multiplatform-settings` (already in project) — rejected per user requirement; DataStore preferred
- `expect/actual` for DataStore creation — rejected: `createWithPath` is already in `commonMain` without expect/actual; only the path string is platform-specific, injected via Koin

---

## Decision 3: Language Feature Architecture

**Decision**: Full Clean Architecture layers in `features/language/` — NOT using the UI-Only exception.

**Rationale**: This feature has real persistence (DataStore), a domain entity (`AppLang`), and a repository boundary. Constitution Principle I mandates five layers when data sources exist. Repository returns `Result<T>`.

**Package layout**:
```
features/language/
├── domain/
│   ├── entities/AppLang.kt          ← sealed class or enum
│   └── repositories/LanguageRepository.kt
├── data/
│   ├── datasources/LanguageDataSource.kt + LanguageDataSourceImpl.kt
│   └── repositories/LanguageRepositoryImpl.kt
└── ui/
    ├── LanguageUiState.kt
    ├── LanguageViewModel.kt
    └── LanguageBottomSheet.kt
```

No `navigation/` package — the bottom sheet is shown/hidden via `ProfileScreen` UiState, not via NavController.

---

## Decision 4: DataStoreManager Placement

**Decision**: `features/common/data/datastore/` package.

**Rationale**: `DataStoreManager` is a shared infrastructure utility (not feature-specific). Constitution Principle IV enforces reuse — a single `DataStoreManager` serves all future preference needs.

```kotlin
class DataStoreManager(private val dataStore: DataStore<Preferences>) {
    suspend fun <T> save(key: Preferences.Key<T>, value: T)
    fun <T> get(key: Preferences.Key<T>, defaultValue: T): Flow<T>
}
```

---

## Decision 5: Localization Package Placement

**Decision**: `features/common/localization/` package.

**Rationale**: Localization infrastructure (`AppStrings`, `LocalAppStrings`) is shared across all feature screens — belongs in `common`, not a single feature package.

---

## Decision 6: LanguageBottomSheet Integration with ProfileScreen

**Decision**: `ProfileScreen` holds bottom sheet visibility state via its existing `ProfileUiState`. `LanguageBottomSheet` is a standalone `@Composable` accepting `LanguageUiState`, selection callbacks, and `onApply` callback. `ProfileRoute` injects both `ProfileViewModel` (via `koinViewModel()`) and `LanguageViewModel` (via `koinViewModel()`).

**Rationale**: Separate ViewModels (confirmed in clarification Q2). `ProfileScreen` composes both. The bottom sheet doesn't need its own nav graph — it's a modal surface within Profile.

---

## String Migration Map

All hardcoded UI strings mapped to `AppStrings` keys:

| Screen | Hardcoded String | AppStrings Key |
|--------|-----------------|----------------|
| FeedScreen | "Hi there!" | `hiThere` |
| FeedScreen | "Got a tasty dish in mind?" | `gotTastyDish` |
| FeedScreen | "Top Recommendations" | `topRecommendations` |
| FeedScreen | "Recipes Of the Week" | `recipesOfTheWeek` |
| SearchScreen | "Search Recipes" | `searchRecipes` |
| SearchScreen | "Search Recipe Items..." | `searchHint` |
| SearchScreen | "No results" | `noResults` |
| DetailScreen | "Description" | `description` |
| DetailScreen | "Ingredients" | `ingredients` |
| DetailScreen | "Instructions" | `instructions` |
| DetailScreen | "Watch Video" | `watchVideo` |
| DetailScreen | "Go Back" | `goBack` |
| ProfileScreen | "Profile" | `profileTitle` |
| ProfileScreen | "QR Code Scan" | `qrCodeScan` |
| ProfileScreen | "QR Code Generation" | `qrCodeGeneration` |
| ProfileScreen | "Connect to Bluetooth Device" | `connectBluetooth` |
| ProfileScreen | "Notification" | `notification` |
| ProfileScreen | "Language" | `language` |
| ProfileScreen | "Logout" | `logout` |
| FavouriteScreen | "Favourites" | `favourites` |
| ErrorContent | "Error in loading Items" | `errorLoadingItems` |
| LanguageBottomSheet | "Select Language" | `selectLanguage` |
| LanguageBottomSheet | "English" | `english` |
| LanguageBottomSheet | "French" | `french` |
| LanguageBottomSheet | "Apply" | `apply` |

**Note**: Strings already using `Res.string.*` (tab labels: home, search, favourites, profile via `Screen` sealed class) are NOT migrated — they already use the resource system and don't appear as hardcoded literals.

**Note**: Dynamic API response strings (recipe names, categories, areas, etc.) are NOT touched per user requirement.
