<!--
SYNC IMPACT REPORT
==================
Version change: [TEMPLATE] → 1.0.0
Modified principles: All placeholders replaced with project-specific content
Added sections:
  - Core Principles (I–V)
  - Architecture & Source Set Constraints
  - Development Workflow & Quality Gates
  - Governance
Templates requiring updates:
  - .specify/templates/plan-template.md ⚠ pending manual review
  - .specify/templates/spec-template.md ⚠ pending manual review
  - .specify/templates/tasks-template.md ⚠ pending manual review
Deferred TODOs: None

---

Amendment 1.0.1 — 2026-05-28
Added "UI-Only Feature Exception" to Principles I and V.
Rationale: features with no data to fetch, persist, or map (hardcoded/in-memory UI state only)
must not be forced to create empty Repository, DataSource, and domain/ package scaffolding.
Empty abstractions violate Principle IV (Code Reuse — No Duplication) and the project's
"no half-finished implementations" guideline. The exception is bounded and triggers a mandatory
follow-up obligation when real data integration arrives.
Affected principles: I, V.
Templates requiring updates: none (exception is conditional, not a structural change).
-->

# RecipeApp Constitution

## Core Principles

### I. Clean Architecture + MVVM (NON-NEGOTIABLE)

Every feature MUST be structured across five distinct layers in strict top-down dependency order:
UI → ViewModel → Repository Interface → RepositoryImpl → DataSource.
No layer may import or depend on a layer above it.

- UI layer: Composables + UiState data classes only. Stateless `Screen` composable + stateful `Route` composable per feature.
- ViewModel layer: exposes `StateFlow<UiState>` only. All coroutines in `viewModelScope`. Never exposes `MutableStateFlow`, `LiveData`, or `SharedFlow` publicly.
- Repository layer: interface in `domain/repositories/`, implementation in `data/repositories/`. All methods return `kotlin.Result<T>` — exceptions MUST NOT cross the repository boundary.
- DataSource layer: `LocalDataSource` (SQLDelight via `RecipeDao`) + `RemoteDataSource` (Ktor).
- Domain/Entity layer: pure Kotlin data classes — zero Android, Compose, Ktor, or SQLDelight imports.

**UI-Only Feature Exception**: A feature that has no data to fetch, persist, or map (all state is in-memory / hardcoded as a UI placeholder) MAY omit the Repository, DataSource, and Domain layers. Conditions:
1. The feature spec explicitly documents that all state is hardcoded/in-memory (not from a remote or local source).
2. The plan's Complexity Tracking section records the deviation and its justification.
3. A follow-up obligation is documented: when real data integration arrives, the missing layers MUST be introduced in a dedicated feature branch following the full five-layer structure.

### II. Kotlin Multiplatform — commonMain First

All Composables, ViewModels, Repositories, Use Cases, and Domain entities MUST live in `commonMain`.
Platform-specific code (`androidMain` / `iosMain`) is permitted only for:
- DI bootstrap (`Application.onCreate`, `initKoinIOS`)
- `DatabaseDriverFactory` (`actual` implementations)
- `MainViewController` (iOS entry point)
- Hardware APIs behind `expect/actual` or Koin-injected interfaces

If a Composable requires a platform import, stop and introduce an `expect/actual` abstraction or a Koin-injected interface instead of polluting `commonMain`.

### III. Cache-First Data Strategy (NON-NEGOTIABLE)

Every `RepositoryImpl` MUST follow the cache-first pattern:
1. Query `LocalDataSource` (SQLDelight) first.
2. If cache is empty, fetch from `RemoteDataSource` (Ktor).
3. Persist remote result to local DB before returning.
4. Wrap all outcomes in `Result<T>` — never throw from a repository.

Returning stale or remote-only data without persisting is a violation.

### IV. Code Reuse — No Duplication

Before adding any new class, function, DAO query, mapper, or Composable, verify that an equivalent does not already exist.
- Reuse `RecipeDao` for all DB access — do not create parallel DAO classes.
- Reuse `HttpClient` singleton from `networkModule` — do not instantiate a second client.
- Reuse existing mapper extension functions (`toRecipe()`, `RecipeEntityMapper`) — do not write inline mapping logic.
- Reuse `RecipeAppTheme`, `MaterialTheme.colorScheme.*`, and `AppTypography` — no hardcoded colors or text styles.
- Reuse `Res.drawable.*` / `Res.string.*` resource references — no Android `R.*` or raw string literals for UI copy.

### V. Feature Package Structure (NON-NEGOTIABLE)

Every new feature MUST follow this exact layout under `commonMain/kotlin/com/adrian/recipeapp/features/{featureName}/`:

```
{featureName}/
├── domain/
│   ├── entities/          ← pure Kotlin data class
│   └── repositories/      ← interface only, suspend funs returning Result<T>
├── data/
│   ├── models/            ← @Serializable DTO + toDomainEntity() mapper
│   ├── datasources/       ← interface + LocalImpl (RecipeDao) + RemoteImpl (Ktor)
│   └── repositories/      ← RepositoryImpl, cache-first, Result<T>
├── navigation/            ← NavGraphBuilder extension fun + route constants
└── ui/
    ├── {Feature}UiState.kt
    ├── {Feature}ViewModel.kt
    └── {Feature}Screen.kt
```

Deviation from this layout requires explicit justification and constitution amendment.

**UI-Only Feature Exception**: When Principle I's UI-Only Feature Exception applies, the `domain/` and `data/` packages MAY be omitted. Only `navigation/` and `ui/` are required. The same three conditions from Principle I apply (documented spec, Complexity Tracking entry, follow-up obligation).

## Architecture & Source Set Constraints

- All navigation graphs are `NavGraphBuilder` extension functions — one file per feature.
- `Screen` sealed class is the sole source of truth for all route strings, tab icons, and tab labels.
- `AppState` (`@Stable`) wraps the root `NavHostController` and exposes only typed navigation functions. `NavController` MUST NOT reach a ViewModel.
- Navigation arguments MUST be typed primitives (e.g., `Long` IDs) — never serialize domain objects into routes.
- SQLDelight queries MUST go through `RecipeDao` only — no direct `RecipeAppDatabase` access above the DAO layer.
- `DbHelper` lazy init is guarded by a `Mutex` — do not bypass it.
- All ViewModels MUST be registered with `viewModel { }` in `ViewModelModule` and retrieved via `koinViewModel()` in Composables — never constructed manually.
- `collectAsStateWithLifecycle()` MUST be used in Composables — not `collectAsState()`.
- DTO fields MUST use `@Serializable` + `@SerialName` — no implicit name mapping.
- `Modifier` MUST be the first optional parameter in every Composable signature.

## Development Workflow & Quality Gates

**Adding a new feature checklist (all items required before merge):**

- [ ] `features/{featureName}/domain/entities/` — pure Kotlin data class
- [ ] `features/{featureName}/domain/repositories/` — interface, `suspend` funs, `Result<T>`
- [ ] `features/{featureName}/data/models/` — `@Serializable` DTO + mapper
- [ ] `features/{featureName}/data/datasources/` — interface + LocalImpl + RemoteImpl
- [ ] `features/{featureName}/data/repositories/` — cache-first `RepositoryImpl`
- [ ] `features/{featureName}/ui/` — `UiState`, `ViewModel` (`StateFlow`), `Route` + `Screen` composables
- [ ] `features/{featureName}/navigation/` — `NavGraphBuilder` extension + route constants
- [ ] Registered in `DataModule`, `ViewModelModule`, and the appropriate `NavHost`
- [ ] Strings/drawables added to `commonMain/composeResources/` if needed
- [ ] No new duplication of existing DAOs, mappers, HTTP clients, or theme tokens

**Code review gate — block merge if any of the following are violated:**

- `Modifier` not the first optional parameter in a Composable
- DTO field missing `@Serializable` or `@SerialName`
- Repository method throws instead of returning `Result<T>`
- `android.*` / `platform.*` import inside `commonMain`
- ViewModel constructed manually (not via `koinViewModel()`)
- Navigation argument is a serialized object (not a typed primitive)
- `collectAsState()` used instead of `collectAsStateWithLifecycle()`
- Hardcoded `Color(0xFF…)` or raw color value inside a Composable
- `R.*` resource reference inside `commonMain`
- SQLDelight query bypassing `RecipeDao`

## Governance

This constitution supersedes all other coding guidelines, README snippets, or verbal conventions for the RecipeApp project.

**Amendment procedure:**
1. Propose the change in a PR with a rationale comment referencing the specific principle.
2. Increment `CONSTITUTION_VERSION` per semantic versioning:
   - MAJOR: removal or redefinition of a principle (backward-incompatible governance change).
   - MINOR: new principle or material expansion of guidance.
   - PATCH: clarification, wording fix, or non-semantic refinement.
3. Update all dependent templates (plan, spec, tasks) in the same PR.
4. Record the amendment in the Sync Impact Report comment at the top of this file.

**Compliance review:** Every PR touching `commonMain` code MUST be verified against the Code Review Gate checklist above before approval.

**Runtime guidance:** Refer to `.specify/memory/architecture/architecture.md` for canonical layer diagrams, data-flow examples, and Koin module graph details.

**Version**: 1.0.1 | **Ratified**: 2026-05-25 | **Last Amended**: 2026-05-28
