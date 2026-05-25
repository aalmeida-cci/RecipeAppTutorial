# MVVM + Clean Architecture — Kotlin Multiplatform (Android & iOS)

---

## Overview

This document defines the canonical MVVM architecture pattern for Kotlin Multiplatform projects
targeting Android and iOS using Compose Multiplatform. All architectural rules apply to
`commonMain` unless explicitly noted.

---

## Layer Definitions

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
│  Composables · UiState data classes · Theme                 │
├─────────────────────────────────────────────────────────────┤
│                     ViewModel Layer                         │
│  ViewModel (androidx.lifecycle) · StateFlow · viewModelScope│
├─────────────────────────────────────────────────────────────┤
│                    Repository Layer                         │
│  Interface (domain/) · Implementation (data/)               │
├─────────────────────────────────────────────────────────────┤
│                    DataSource Layer                         │
│  LocalDataSource (SQLDelight) · RemoteDataSource (Ktor)     │
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                           │
│  Entities · Repository Interfaces · (Optional) Use Cases    │
└─────────────────────────────────────────────────────────────┘
```

---

## Feature Package Structure

Every feature follows this identical package layout inside `commonMain`:

```
features/
└── {featureName}/
    ├── domain/
    │   ├── entities/          ← Pure Kotlin data classes — zero framework imports
    │   └── repositories/      ← Repository interface (contract only)
    ├── data/
    │   ├── models/            ← @Serializable DTOs + mapper extension functions
    │   ├── datasources/       ← Local + Remote DataSource interfaces and Impls
    │   └── repositories/      ← RepositoryImpl (implements domain interface)
    ├── navigation/            ← NavGraphBuilder extension function + route constants
    └── ui/
        ├── {Feature}UiState.kt    ← Immutable data class representing screen state
        ├── {Feature}ViewModel.kt  ← ViewModel exposing StateFlow<UiState>
        └── {Feature}Screen.kt     ← Stateless Composable consuming UiState
```

---

## Data Flow

```
{Feature}Screen  (Composable)
        │
        │  collectAsStateWithLifecycle()
        ▼
{Feature}ViewModel  (ViewModel)
        │
        │  viewModelScope.launch { }
        │  _uiState: MutableStateFlow<{Feature}UiState>
        │  uiState: StateFlow<{Feature}UiState>  ← exposed to UI
        ▼
{Feature}Repository  (interface, domain layer)
        │
        │  suspend fun get*(): Result<T>
        ▼
{Feature}RepositoryImpl  (data layer)
        ├──▶  LocalDataSource  (SQLDelight — cache read/write)
        └──▶  RemoteDataSource  (Ktor — network fetch)
```

---

## Cache-First Repository Strategy

All `RepositoryImpl` classes must follow this pattern:

```kotlin
override suspend fun getData(): Result<List<DomainEntity>> {
    return try {
        val cached = localDataSource.getAll()
        if (cached.isNotEmpty()) {
            Result.success(cached)
        } else {
            val remote = remoteDataSource.fetchAll()
            localDataSource.saveAll(remote)
            Result.success(remote)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Rules:**
- Always check local cache before making a network call.
- Always persist remote data to local before returning it.
- Never let exceptions cross the repository boundary — wrap in `Result.failure(e)`.
- Return type is always `Result<T>` — never raw nullable or suspend throwing.

---

## ViewModel Contract

```kotlin
class {Feature}ViewModel(
    private val repository: {Feature}Repository   // injected via Koin
) : ViewModel() {

    private val _uiState = MutableStateFlow({Feature}UiState())
    val uiState = _uiState.asStateFlow()           // only StateFlow exposed to UI

    init {
        viewModelScope.launch { load() }           // trigger on creation
    }

    private suspend fun load() {
        val result = repository.getData()
        _uiState.update { state ->
            if (result.isSuccess) {
                state.copy(data = result.getOrDefault(emptyList()), isLoading = false)
            } else {
                state.copy(error = result.exceptionOrNull()?.message, isLoading = false)
            }
        }
    }
}
```

**Rules:**
- Expose `StateFlow` only — never `MutableStateFlow`, `LiveData`, or `SharedFlow` publicly.
- All coroutines launched inside `viewModelScope`.
- IO work dispatched via DI-provided `CoroutineContext` (`Dispatchers.IO` / `Dispatchers.Default`).
- Never use `java.util.concurrent`, Android `Handler`, or iOS GCD.

---

## UiState Contract

```kotlin
data class {Feature}UiState(
    val data: List<DomainEntity>? = null,   // null = not yet loaded
    val isLoading: Boolean = true,
    val error: String? = null
)
```

**Rules:**
- Always an immutable `data class`.
- Use `copy()` for all state mutations — never mutate fields directly.
- `isLoading = true` is the default starting state.
- `error` holds a human-readable message — never an exception object.

---

## Composable Contract

```kotlin
@Composable
fun {Feature}Route(
    viewModel: {Feature}ViewModel = koinViewModel()   // DI entry point
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    {Feature}Screen(uiState = uiState)
}

@Composable
fun {Feature}Screen(
    modifier: Modifier = Modifier,     // Modifier is always first optional param
    uiState: {Feature}UiState
) {
    // Stateless — renders from uiState only
    // No ViewModel reference, no side effects here
}
```

**Rules:**
- Split every screen into a `Route` (stateful, owns ViewModel) and `Screen` (stateless, pure render).
- `Modifier` is always the first optional parameter.
- Collect state with `collectAsStateWithLifecycle()` — never `collectAsState()`.
- No hardcoded colors — always use `MaterialTheme.colorScheme.*`.
- No platform imports (`android.*`, `platform.*`) in `commonMain` Composables.

---

## Domain Entity Contract

```kotlin
data class DomainEntity(
    val id: Long,
    val name: String,
    // ... domain-relevant fields only
)
```

**Rules:**
- Pure Kotlin — zero Android, Compose, or Ktor imports.
- No serialisation annotations — those belong on DTOs only.
- No database-specific types — SQLDelight types stay in the data layer.

---

## DTO & Mapper Contract

```kotlin
@Serializable
data class ApiDto(
    @SerialName("api_field") val apiField: String?,
    // ...
)

fun ApiDto.toDomainEntity(): DomainEntity? {
    return if (id != null) DomainEntity(id = id, name = name ?: "") else null
}
```

**Rules:**
- Every DTO field annotated with `@SerialName` — no implicit name mapping.
- Mapper is an extension function on the DTO — one mapping function per DTO.
- Nullable API fields are handled at the mapper level — domain entities should be non-nullable where possible.

---

## Dependency Injection Structure (Koin)

```
initKoin(platformModules)
    ├── cacheModule()       → CoroutineContext · CoroutineScope · DbHelper · DAOs
    ├── networkModule()     → HttpClient (singleton)
    ├── dataModule()        → DataSources (single) · Repositories (single)
    └── viewModelModule()   → ViewModels (viewModel { })
```

**Platform bootstrap:**
- **Android** — `Application.onCreate()` calls `initKoin(additionalModule = [androidModule])`.
  `androidModule` provides the platform-specific `DatabaseDriverFactory(applicationContext)`.
- **iOS** — `initKoinIOS()` called from Swift before `ComposeUIViewController` is created.
  `iosModules` provides `DatabaseDriverFactory()` (no-arg).

**Rules:**
- All DataSources and Repositories registered as `single`.
- All ViewModels registered with `viewModel { }` DSL.
- Never construct a ViewModel manually — always use `koinViewModel()` in Composables.

---

## Navigation Structure

```
RootNavHost  (owns root NavHostController)
    ├── tabsNavGraph            → TabsScreen  (owns nested tabNavController)
    │     ├── {tab1}NavGraph    → {Tab1}Screen
    │     ├── {tab2}NavGraph    → {Tab2}Screen
    │     └── {tab3}NavGraph    → {Tab3}Screen
    ├── {feature}NavGraph       → {Feature}Screen  (top-level, no bottom nav)
    └── detailNavGraph          → DetailScreen  (receives typed ID argument)
```

**Rules:**
- All navigation graphs defined as `NavGraphBuilder` extension functions — one file per feature.
- A single `sealed class Screen` is the source of truth for all route strings, icons, and labels.
- An `@Stable` `AppState` class wraps the root `NavHostController` and exposes only typed navigation functions — the `NavController` never reaches a ViewModel.
- Tab navigation uses `saveState = true` / `restoreState = true` to preserve back stacks.
- Arguments passed as typed primitives (e.g. `Long` IDs) — never serialise domain objects into routes.

---

## Source Set Discipline

| Code type | Source set |
|---|---|
| Composables, ViewModels, Repositories, Entities, Use Cases | `commonMain` |
| DI platform bootstrap (`Application` / Koin init) | `androidMain` / `iosMain` |
| `DatabaseDriverFactory` implementation | `androidMain` (Android driver) / `iosMain` (Native driver) |
| `MainViewController` (iOS entry point) | `iosMain` |
| Hardware APIs via `expect/actual` (Camera, Biometrics, Haptics) | `androidMain` / `iosMain` |

**Rule:** If a Composable requires a platform import, move the platform behaviour behind an `expect/actual` or a Koin-injected interface — never add Android/iOS imports directly to `commonMain`.

---

## SQLDelight Access Pattern

- All queries go through a single DAO class per entity — never access the generated database class directly above the DAO layer.
- The database is initialised lazily behind a `Mutex` — guaranteed thread-safe on both platforms.
- Always use async drivers (`generateAsync = true`) with `awaitAsList()` / `awaitAsOneOrNull()`.
- `List<String>` columns require a custom `ColumnAdapter` — never store delimited strings without one.

---

## Resource Management

- All images, icons, and strings live in `commonMain/composeResources/`.
- Reference assets via the generated `Res` object (`Res.drawable.*`, `Res.string.*`).
- Never use Android `R.*` or iOS asset catalog names directly in shared code.
- Icon pairs follow the convention: `{name}_selected` / `{name}_unselected`.

---

## Adding a New Feature — Checklist

```
[ ] Create package: features/{featureName}/
[ ] domain/entities/       ← pure Kotlin data class, no framework imports
[ ] domain/repositories/   ← interface only, suspend functions returning Result<T>
[ ] data/models/           ← @Serializable DTO + mapper extension fun (toDomainEntity)
[ ] data/datasources/      ← interface + LocalImpl (DAO) + RemoteImpl (Ktor)
[ ] data/repositories/     ← RepositoryImpl — cache-first, Result<T> wrapping
[ ] ui/{Feature}UiState    ← immutable data class, isLoading = true default
[ ] ui/{Feature}ViewModel  ← StateFlow, viewModelScope, Koin-injected repo
[ ] ui/{Feature}Screen     ← Route composable + stateless Screen composable
[ ] navigation/            ← NavGraphBuilder extension + route/arg constants
[ ] Register in: dataModule(), viewModelModule(), and the appropriate NavHost
[ ] Add strings/drawables to commonMain/composeResources if needed
```

---

## Review Checklist

- `Modifier` is the first optional parameter in every Composable
- `@Serializable` + `@SerialName` on every DTO field
- Repository returns `Result<T>` — no raw exceptions cross the boundary
- No `android.*` / `platform.*` imports inside `commonMain`
- ViewModels retrieved via `koinViewModel()` — never constructed manually
- Navigation arguments are typed primitives — no serialised objects in routes
- `collectAsStateWithLifecycle()` used — not `collectAsState()`
- Colors via `MaterialTheme.colorScheme.*` — no hardcoded `Color(0xFF…)`
- Resources via `Res.*` — no Android `R.*`
- DB queries via DAO only — no direct database access above DAO layer
