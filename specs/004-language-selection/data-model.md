# Data Model: Language Selection

**Branch**: `004-language-selection` | **Date**: 2026-05-28

---

## Domain Entity: AppLang

```kotlin
// features/language/domain/entities/AppLang.kt
enum class AppLang(val code: String, val displayName: String) {
    EN("en", "English"),
    FR("fr", "French");

    companion object {
        val default: AppLang = EN
        fun fromCode(code: String): AppLang =
            entries.firstOrNull { it.code == code } ?: default
    }
}
```

**Fields**:
- `code: String` — ISO 639-1 language code persisted to DataStore
- `displayName: String` — name shown in the bottom sheet list

**Constraints**:
- Fixed set — no dynamic loading; enum exhausts all valid values
- `fromCode` degrades gracefully to `EN` for unknown/null codes (empty DataStore on first launch)

---

## UI State: LanguageUiState

```kotlin
// features/language/ui/LanguageUiState.kt
data class LanguageUiState(
    val currentLang: AppLang = AppLang.default,
    val pendingLang: AppLang = AppLang.default,
    val isBottomSheetVisible: Boolean = false,
    val isLoading: Boolean = false
)
```

**Fields**:
- `currentLang` — the applied, persisted language (drives `CompositionLocalProvider` in `App.kt`)
- `pendingLang` — the selection made in the bottom sheet, not yet applied (discarded on dismiss)
- `isBottomSheetVisible` — controls `ModalBottomSheet` visibility from `ProfileScreen`
- `isLoading` — true while reading persisted lang on startup

**State transitions**:
```
Initial → isLoading=true
         ↓ (DataStore read)
Loaded  → currentLang=savedLang, pendingLang=savedLang, isLoading=false
         ↓ (Language row tapped)
SheetOpen → isBottomSheetVisible=true
         ↓ (Radio tapped)
Selecting → pendingLang=newLang (currentLang unchanged)
         ↓ (Apply tapped)
Applied   → currentLang=pendingLang, isBottomSheetVisible=false (persisted to DataStore)
         ↓ (Dismissed without Apply)
Dismissed → pendingLang=currentLang, isBottomSheetVisible=false (discard pending)
```

---

## Repository Interface: LanguageRepository

```kotlin
// features/language/domain/repositories/LanguageRepository.kt
interface LanguageRepository {
    fun getSelectedLang(): Flow<Result<AppLang>>
    suspend fun saveSelectedLang(lang: AppLang): Result<Unit>
}
```

---

## DataSource Interface: LanguageDataSource

```kotlin
// features/language/data/datasources/LanguageDataSource.kt
interface LanguageDataSource {
    fun getLangCode(): Flow<String>
    suspend fun saveLangCode(code: String)
}
```

---

## Persistence: DataStore Preferences Key

```kotlin
// LanguageDataSourceImpl.kt
private val LANGUAGE_KEY = stringPreferencesKey("selected_language_code")
```

DataStore file: `app_preferences.preferences_pb`

---

## Localization: AppStrings Interface

```kotlin
// features/common/localization/AppStrings.kt
interface AppStrings {
    // Feed
    val hiThere: String
    val gotTastyDish: String
    val topRecommendations: String
    val recipesOfTheWeek: String
    // Search
    val searchRecipes: String
    val searchHint: String
    val noResults: String
    // Detail
    val description: String
    val ingredients: String
    val instructions: String
    val watchVideo: String
    val goBack: String
    // Profile
    val profileTitle: String
    val qrCodeScan: String
    val qrCodeGeneration: String
    val connectBluetooth: String
    val notification: String
    val language: String
    val logout: String
    // Language Bottom Sheet
    val selectLanguage: String
    val english: String
    val french: String
    val apply: String
    // Favourites
    val favourites: String
    // Common
    val errorLoadingItems: String
}
```

`EnStrings` and `FrStrings` are `object` implementations of `AppStrings`.

`LocalAppStrings` is `compositionLocalOf<AppStrings> { EnStrings }`.

`AppLang` extension: `val AppLang.strings: AppStrings get() = when(this) { EN -> EnStrings; FR -> FrStrings }`

---

## DataStoreManager (generic)

```kotlin
// features/common/data/datastore/DataStoreManager.kt
class DataStoreManager(private val dataStore: DataStore<Preferences>) {
    suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    fun <T> get(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[key] ?: defaultValue }
}
```

No hardcoded keys inside `DataStoreManager` — keys are defined by each consuming DataSource.

---

## Koin DI Graph (additions)

```
initKoin()
├── cacheModule()        (unchanged)
├── networkModule()      (unchanged)
├── dataModule()
│   ├── (existing)
│   ├── single<DataStore<Preferences>> { createDataStore(path) }   ← new
│   ├── single { DataStoreManager(get()) }                         ← new
│   ├── single<LanguageDataSource> { LanguageDataSourceImpl(get()) } ← new
│   └── single<LanguageRepository> { LanguageRepositoryImpl(get()) } ← new
└── viewModelModule()
    ├── (existing ViewModels)
    └── viewModel { LanguageViewModel(get()) }                     ← new

Android initKoin: androidModule provides DataStore path via applicationContext.filesDir
iOS     initKoin: iosModule provides DataStore path via NSHomeDirectory()
```
