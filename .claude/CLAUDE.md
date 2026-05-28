# RecipeApp — Kotlin Multiplatform (Android & iOS)

**Application ID:** `com.adrian.recipeapp`
**API:** [TheMealDB](https://www.themealdb.com/api/json/v1/1/)
**Platforms:** Android (minSdk 24, targetSdk 36) · iOS (iosArm64, iosSimulatorArm64)
**Architecture:** Clean Architecture + MVVM
**UI Framework:** Compose Multiplatform (CMP) — 95%+ shared UI

---

## Tech Stack

| Layer         | Library                                    | Version        |
|---------------|--------------------------------------------|----------------|
| UI            | Compose Multiplatform                      | 1.10.3         |
| UI Components | Material 3                                 | 1.10.0-alpha05 |
| DI            | Koin (Core + Compose + ViewModel)          | 4.2.1          |
| Networking    | Ktor (Core + ContentNegotiation + Logging) | 3.4.3          |
| Serialization | Kotlin Serialization (JSON)                | —              |
| Local DB      | SQLDelight (Async, Native/Android drivers) | 2.3.2          |
| Image Loading | Coil 3 + Ktor3 network fetcher             | 3.4.0          |
| Navigation    | Jetbrains Navigation Compose               | 2.9.2          |
| Preferences   | Multiplatform Settings (russhwolf)         | 1.3.0          |
| Date/Time     | kotlinx-datetime                           | 0.8.0          |
| Lifecycle     | Jetbrains Lifecycle ViewModel Compose      | 2.10.0         |
| Kotlin        | 2.3.20                                     |
| AGP           | 8.11.2                                     |

---

## Directory Structure

composeApp/
└── src/
├── commonMain/                          ← PRIMARY WORKSPACE (99% of all code)
│   ├── kotlin/com/adrian/recipeapp/
│   │   ├── App.kt                       ← Root Composable, theme entry point
│   │   ├── db/
│   │   │   └── DatabaseDriverFactory.kt ← expect class for platform driver
│   │   ├── di/
│   │   │   ├── Koin.kt                  ← initKoin() — called from each platform
│   │   │   ├── CacheModule.kt           ← DbHelper, RecipeDao, CoroutineScope
│   │   │   ├── NetworkModule.kt         ← HttpClient singleton
│   │   │   ├── DataModule.kt            ← DataSources + Repositories
│   │   │   └── ViewModelModule.kt       ← all ViewModels
│   │   └── features/
│   │       ├── app/
│   │       │   ├── data/
│   │       │   │   ├── AppState.kt      ← @Stable nav state holder
│   │       │   │   └── Screen.kt        ← sealed class route definitions
│   │       │   └── navigation/
│   │       │       └── AppNavHost.kt    ← root NavHost
│   │       ├── common/
│   │       │   ├── data/
│   │       │   │   ├── api/
│   │       │   │   │   └── HttpClient.kt       ← Ktor client config + BASE_URL
│   │       │   │   ├── database/
│   │       │   │   │   ├── DbHelper.kt         ← Mutex-guarded lazy DB init
│   │       │   │   │   ├── ListOfStringsAdapter.kt
│   │       │   │   │   ├── RecipeEntityMapper.kt
│   │       │   │   │   └── daos/RecipeDao.kt   ← all DB queries (async SQLDelight)
│   │       │   │   └── models/
│   │       │   │       ├── RecipeApiItem.kt    ← @Serializable DTO + toRecipe() mapper
│   │       │   │       └── RecipeListApiResponse.kt
│   │       │   └── domain/
│   │       │       └── entities/RecipeItem.kt  ← domain entity (pure Kotlin)
│   │       ├── designSystem/
│   │       │   └── theme/
│   │       │       ├── Color.kt         ← full light/dark/contrast token set
│   │       │       ├── Theme.kt         ← RecipeAppTheme composable
│   │       │       └── Type.kt          ← AppTypography
│   │       ├── feed/                    ← recipe list feature
│   │       │   ├── data/
│   │       │   │   ├── datasource/      ← FeedLocalDataSource + FeedRemoteDataSource
│   │       │   │   └── repositories/    ← FeedRepositoryImpl (cache-first strategy)
│   │       │   ├── domain/repositories/ ← FeedRepository interface
│   │       │   ├── navigation/          ← feedNavGraph()
│   │       │   └── ui/                  ← FeedScreen, FeedViewModel, FeedUiState
│   │       ├── detail/                  ← single recipe detail feature
│   │       │   ├── data/
│   │       │   │   ├── datasources/     ← local + remote data sources
│   │       │   │   └── repository/      ← RecipeDetailRepositoryImpl
│   │       │   ├── repositories/        ← RecipeDetailRepository interface
│   │       │   ├── navigation/          ← detailNavGraph(), RECIPE_ID_ARG
│   │       │   └── ui/                  ← DetailScreen, RecipeDetailViewModel, UiState
│   │       ├── tabs/                    ← bottom navigation host
│   │       │   ├── navigation/          ← tabsNavGraph()
│   │       │   └── ui/TabsScreen.kt     ← Scaffold + NavigationBar + nested NavHost
│   │       ├── favourites/
│   │       │   ├── navigation/
│   │       │   └── ui/FavouriteScreen.kt
│   │       ├── search/
│   │       │   ├── navigation/
│   │       │   └── ui/SearchScreen.kt
│   │       └── profile/
│   │           ├── navigation/
│   │           └── ui/ProfileScreen.kt
│   └── composeResources/
│       ├── drawable/                    ← all SVG/PNG/WebP assets
│       └── values/strings.xml           ← all string resources
│
├── androidMain/
│   └── kotlin/com/adrian/recipeapp/
│       ├── MainActivity.kt              ← ComponentActivity entry point
│       ├── MainApplication.kt           ← Application class, Koin init
│       ├── Platform.android.kt          ← actual Platform implementation
│       └── db/DatabaseDriverFactory.kt  ← actual: AndroidSqliteDriver
│
└── iosMain/
└── kotlin/com/adrian/recipeapp/
├── MainViewController.kt        ← ComposeUIViewController for Xcode
├── KoinIos.kt                   ← initKoinIOS() called from Swift
├── Platform.ios.kt              ← actual Platform implementation
└── db/DatabaseDriverFactory.kt  ← actual: NativeSqliteDriver



---

## Architecture Principles

### Clean Architecture Layers

UI Layer          →  Composables, UiState data classes
ViewModel Layer   →  ViewModel (androidx.lifecycle), StateFlow
Repository Layer  →  Interface in domain/, Impl in data/
DataSource Layer  →  Local (SQLDelight) + Remote (Ktor)
Entity Layer      →  Pure Kotlin data classes in domain/entities/



### Data Flow

Composable
└── collectAsStateWithLifecycle(viewModel.uiState)
└── ViewModel (viewModelScope.launch)
└── Repository.get*() : Result<T>
├── LocalDataSource  ← SQLDelight (cache-first)
└── RemoteDataSource ← Ktor HTTP



### Cache-First Strategy

All repository implementations follow this pattern:
1. Query local SQLDelight database first.
2. If cache is empty, fetch from remote API.
3. Persist API response to local DB before returning.
4. Wrap all outcomes in `kotlin.Result<T>` — never throw from a repository.

---

## Koin DI Module Graph

initKoin()
├── cacheModule()      → CoroutineContext, CoroutineScope, DbHelper, RecipeDao
├── networkModule()    → HttpClient (singleton)
├── dataModule()       → DataSources (single), Repositories (single)
└── viewModelModule()  → ViewModels (viewModel { })

Android: MainApplication.onCreate() calls initKoin(additionalModule = [androidModules])
androidModules provides DatabaseDriverFactory(applicationContext)

iOS:     initKoinIOS() called from Swift before ComposeUIViewController is created
iosModules provides DatabaseDriverFactory() (no-arg)



---

## Navigation Architecture

### Two-Level NavController Pattern

AppNavHost (root NavController)
├── tabsNavGraph          → TabsScreen (owns tabNavController)
│     ├── feedNavGraph    → FeedScreen
│     ├── favouritesNavGraph → FavouriteScreen
│     └── profileNavGraph → ProfileScreen
├── searchNavGraph        → SearchScreen
└── detailNavGraph        → DetailScreen (receives RECIPE_ID_ARG: Long)



- `AppState` (`@Stable`) wraps the root `NavHostController` and exposes typed navigation functions: `navigateToDetail(id)`, `navigateBack()`.
- Tab-level navigation uses `saveState = true` / `restoreState = true` to preserve back stacks.
- Routes are defined in `Screen` sealed class. Use `Screen.SomeName.route` — never hardcode route strings.

### Route Definitions

| Screen    | Route                          | Notes                 |
|-----------|--------------------------------|-----------------------|
| Tabs      | `"tabs"`                       | Shell with bottom nav |
| Home      | `"home"`                       | Tab — feed            |
| Favorites | `"favorites"`                  | Tab — bookmarked      |
| Profile   | `"profile"`                    | Tab — user profile    |
| Detail    | `"detail?recipeId={recipeId}"` | Receives `Long` arg   |
| Search    | `"search"`                     | Top-level             |

---

## SQLDelight Database

- **Database name:** `RecipeAppDatabase`
- **Generated package:** `com.adrian.dailypulsetutorial.db`
- **Async mode:** `generateAsync = true` (use `awaitAsList()`, `awaitAsOneOrNull()`)
- **Custom adapters:** `ListOfStringsAdapter` for `ingredients` and `instructions` columns
- **Access pattern:** Always through `RecipeDao` — never access `RecipeAppDatabase` directly from above the DAO layer
- `DbHelper` lazily initialises the database behind a `Mutex` — thread-safe on both platforms

---

## Networking

- **Base URL:** `https://www.themealdb.com/api/json/v1/1/`
- Ktor client is a **singleton** provided by Koin (`networkModule`)
- `ContentNegotiation` with `kotlinx.serialization.json` (`ignoreUnknownKeys = true`, `isLenient = true`)
- `Logging` at `LogLevel.ALL` — set to `LogLevel.NONE` before release builds
- Platform engines: `ktor-client-android` (Android), `ktor-client-darwin` (iOS)

---

## Design System

- Theme entry: `RecipeAppTheme(darkTheme, content)` in `features/designSystem/theme/Theme.kt`
- Full Material 3 token set: light, dark, medium-contrast, high-contrast schemes defined in `Color.kt`
- Typography defined in `Type.kt` as `AppTypography`
- Dynamic color is **disabled** (commented out) — use static color scheme only

---

## Resource Management Rules

- All images, icons, strings → `composeApp/src/commonMain/composeResources/`
- Reference assets via `Res.drawable.*` and `Res.string.*` — never use Android `R` or iOS asset catalog names directly
- Bottom nav icons follow the naming convention: `{name}_selected.png` / `{name}_unselected.png`

---

## Source Set Rules

| What                                                                 | Where                                         |
|----------------------------------------------------------------------|-----------------------------------------------|
| All Composables, ViewModels, Repositories, UseCases, Domain entities | `commonMain`                                  |
| Koin setup / `Application` class                                     | `androidMain`                                 |
| `DatabaseDriverFactory` (Android driver)                             | `androidMain`                                 |
| `MainViewController`, `initKoinIOS()`                                | `iosMain`                                     |
| `DatabaseDriverFactory` (Native driver)                              | `iosMain`                                     |
| Hardware APIs (Camera, Biometrics, Haptics)                          | `androidMain` / `iosMain` via `expect/actual` |

**Rule:** If you are writing a Composable and reach for `androidMain` or `iosMain`, stop and reconsider. 99% of UI belongs in `commonMain`.

---

## State & Concurrency Rules

- ViewModels expose **only** `StateFlow` — never `LiveData`, `SharedFlow`, or raw suspend functions as public API
- All coroutines launched inside `viewModelScope`
- Long-running IO (Ktor, SQLDelight) dispatched on `Dispatchers.IO` or `Dispatchers.Default` via DI-provided `CoroutineContext`
- Never use `java.util.concurrent`, Android `Handler`, or iOS GCD directly

---

## Build & Run Commands

```bash
# Android debug install
./gradlew :composeApp:installDebug

# Android assemble only
./gradlew :composeApp:assembleDebug

# iOS (requires macOS + Xcode)
open iosApp/iosApp.xcworkspace   # then Run from Xcode

# Lint & format
./gradlew spotlessApply

# Clean build
./gradlew clean :composeApp:assembleDebug

# Run all tests
./gradlew :composeApp:allTests
Adding a New Feature — Checklist

[ ] Create package: features/{featureName}/
[ ] domain/entities/       ← pure Kotlin data class (no Android/Compose imports)
[ ] domain/repositories/   ← interface only
[ ] data/models/           ← @Serializable DTO + mapper extension fun
[ ] data/datasources/      ← interface + Impl (Local = RecipeDao, Remote = Ktor)
[ ] data/repositories/     ← RepositoryImpl (cache-first with Result<T>)
[ ] ui/                    ← UiState data class, ViewModel (StateFlow), Screen composable
[ ] navigation/            ← NavGraphBuilder extension fun + route constants
[ ] Register in DataModule, ViewModelModule, AppNavHost / TabsNavHost
[ ] Resources (strings, drawables) added to commonMain/composeResources
Code Review Checklist for Claude
Modifier is the first optional parameter in every Composable signature
All DTOs are annotated with @Serializable and use @SerialName for every field
Repository methods return Result<T> — no raw exceptions cross the repository boundary
No platform-specific imports (android.*, platform.*) inside commonMain
ViewModels are scoped and retrieved via koinViewModel() — not constructed manually
Navigation arguments are typed (pass Long IDs, not strings) via nav argument definitions
collectAsStateWithLifecycle() used in Composables — not collectAsState()
No hardcoded colour values or Color(0xFF…) inside Composables — use MaterialTheme.colorScheme.*
Resources accessed via Res.drawable.* / Res.string.* — never Android R.*
SQLDelight queries always go through RecipeDao — never raw database access above DAO layer
Known Patterns & Conventions
Feature navigation is defined as NavGraphBuilder extension functions (feedNavGraph, detailNavGraph, etc.), one file per feature.
Screen sealed class is the single source of truth for route strings, tab icons, and tab labels.
AppState (@Stable, remember-ed) centralises all root-level navigation actions to prevent NavController leaking into ViewModels.
DbHelper uses a Mutex for lazy, thread-safe database initialisation — do not bypass it.
Ingredient/instruction data is stored as List<String> in SQLDelight using ListOfStringsAdapter (comma-separated serialisation).
toRecipe() extension on RecipeApiItem is the single mapping point from API DTO → domain entity — keep all field-level transformations here.
---

<!-- SPECKIT START -->
## Active Feature Plan

**Feature**: Recipe Search (`002-recipe-search`)
**Plan**: [specs/002-recipe-search/plan.md](../specs/002-recipe-search/plan.md)
**Spec**: [specs/002-recipe-search/spec.md](../specs/002-recipe-search/spec.md)
<!-- SPECKIT END -->
