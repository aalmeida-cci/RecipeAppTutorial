# Implementation Plan: Splash Screen

**Branch**: `001-splash-screen` | **Date**: 2026-05-25 | **Spec**: [spec.md](spec.md)

## Summary

Display a full-screen branded splash screen with `recipe_app_logo.png` for 3 seconds on every cold app launch, then automatically navigate to `Screen.Tabs` (the home feed). The splash screen is removed from the back stack so pressing Back from the home feed exits the app.

## Technical Context

**Language/Version**: Kotlin 2.3.20 / Compose Multiplatform 1.10.3

**Primary Dependencies**: Jetbrains Navigation Compose 2.9.2 · Jetbrains Lifecycle ViewModel Compose 2.10.0 · Koin 4.2.1 · kotlinx-coroutines

**Storage**: N/A — no persistence required

**Testing**: Manual (launch from cold start, verify 3s delay and no back-stack return)

**Target Platform**: Android (minSdk 24) · iOS (iosArm64, iosSimulatorArm64)

**Project Type**: Kotlin Multiplatform mobile app (Compose Multiplatform)

**Performance Goals**: Splash must render within one frame of app start; timer begins after first composition

**Constraints**: Splash is a pure UI screen — no network, DB, or DI dependencies. No ViewModel needed.

**Scale/Scope**: Single new screen + route + nav graph extension + Screen sealed class entry

## Constitution Check

| Gate | Status | Notes |
|------|--------|-------|
| All UI in `commonMain` | ✅ PASS | `SplashScreen.kt` lives in `commonMain` |
| Route defined in `Screen` sealed class | ✅ PASS | `Screen.Splash` added as a new `data object` |
| Navigation via `AppState` typed functions | ✅ PASS | `AppState.navigateToTabs()` already exists — reused |
| No hardcoded colors | ✅ PASS | Background uses `MaterialTheme.colorScheme.primary` |
| Resources via `Res.*` | ✅ PASS | Logo loaded via `Res.drawable.recipe_app_logo` |
| `Modifier` first optional param | ✅ PASS | Enforced in `SplashScreen` signature |
| No platform imports in `commonMain` | ✅ PASS | `delay()` from `kotlinx.coroutines` — no platform API |
| No ViewModel required | ✅ NOTE | Splash has no state — `LaunchedEffect` handles timer directly in the `Route` composable |
| Splash removed from back stack | ✅ PASS | `popUpTo(Screen.Splash.route) { inclusive = true }` on navigate |

**Post-design re-check**: All gates pass with one explicit deviation documented under "Complexity Tracking & Justified Deviations" below.

## Complexity Tracking & Justified Deviations

### Deviation: Principle V (Feature Package Structure) — partial layout

**What is omitted**:
- `splash/domain/entities/` — no domain entity exists for a stateless timed screen
- `splash/domain/repositories/` — no repository required (no data fetched)
- `splash/data/models/` — no DTO required (no API call)
- `splash/data/datasources/` — no local/remote source required
- `splash/data/repositories/` — no `RepositoryImpl` required
- `splash/ui/SplashUiState.kt` — no state exists beyond "showing" (single, implicit)
- `splash/ui/SplashViewModel.kt` — no `StateFlow` required; `LaunchedEffect` is sufficient

**Justification**:
- The splash feature has **zero state, zero data dependencies, and zero side effects beyond a single timed navigation callback**. There is no `UiState` to model (the only "state" is the passage of time, handled by `LaunchedEffect`), no `Result<T>` to wrap, and no `LocalDataSource`/`RemoteDataSource` to feed.
- Creating empty `domain/`, `data/`, `UiState`, and `ViewModel` files purely to satisfy Principle V's layout would directly violate **Principle IV (Code Reuse — No Duplication / No unnecessary abstractions)** by introducing scaffolding without behaviour.
- Principles IV and V are in tension only for stateless UI-only features. Resolving in favour of Principle IV here is consistent with the constitution's overall intent (lean, purposeful code).

**Scope of deviation**:
- Limited to this single feature. All other features (feed, detail, favourites, search, profile) continue to follow Principle V in full.
- The `navigation/` and `ui/` packages ARE retained per Principle V.
- The `Route` + `Screen` composable split IS retained (`SplashRoute` + `SplashScreen`).

**Constitution amendment recommended (separate PR)**:
- Add a clarifying clause to Principle V: "Stateless UI-only features (no data, no state, no side effects beyond navigation) MAY omit `domain/` and `data/` packages and the `UiState`/`ViewModel` files, provided the deviation is documented in the feature's plan.md."

## Project Structure

### Documentation (this feature)

```text
specs/001-splash-screen/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output (N/A — no data entities)
└── tasks.md             ← Phase 2 output (/speckit-tasks)
```

### Source Code Changes

```text
composeApp/src/commonMain/kotlin/com/adrian/recipeapp/
├── features/
│   ├── app/
│   │   ├── data/
│   │   │   └── Screen.kt                          ← ADD Screen.Splash data object
│   │   └── navigation/
│   │       └── AppNavHost.kt                      ← CHANGE startDestination to Screen.Splash; ADD splashNavGraph()
│   └── splash/
│       ├── navigation/
│       │   └── SplashNavigation.kt                ← NEW: splashNavGraph() NavGraphBuilder extension
│       └── ui/
│           └── SplashScreen.kt                    ← NEW: SplashRoute + SplashScreen composables
```

**No new DI modules, no new DAO, no new ViewModel, no new repository.**

## Phase 0: Research

### Decision: Timer mechanism

- **Decision**: `LaunchedEffect(Unit) { delay(3000L); onNavigate() }` inside the `Route` composable
- **Rationale**: Splash has zero state — no `ViewModel` or `StateFlow` needed. `LaunchedEffect` scoped to the composable lifecycle is idiomatic Compose for one-shot side effects. Cancelled automatically if the composable leaves composition early (e.g., process death).
- **Alternatives considered**: ViewModel + `viewModelScope.launch` — over-engineered for a stateless timed transition; rejected per constitution Principle IV (no duplication/unnecessary abstractions).

### Decision: Back stack clearing

- **Decision**: Navigate with `popUpTo(Screen.Splash.route) { inclusive = true }` when transitioning to Tabs
- **Rationale**: Removes the splash destination from the back stack entirely so pressing Back from the home feed exits the app rather than returning to splash. This matches FR-007.
- **Alternatives considered**: `clearBackStack()` — less precise; the `popUpTo` + `inclusive` pattern is the standard Navigation Compose idiom.

### Decision: Add a `splash` string resource

- **Decision**: Add `<string name="splash">Splash</string>` to `strings.xml` and reference it as `Res.string.splash` in `Screen.Splash`.
- **Rationale**: The `Screen` sealed class currently requires a `StringResource` parameter for every entry (used by tab labels). Splash has no visible label, but the parameter is mandatory; a dedicated `splash` string is the cleanest fit and avoids re-purposing an unrelated existing string.
- **Logo size**: The logo is rendered at 160.dp — a conservative size that scales legibly across phone densities (mdpi through xxxhdpi) without dominating small screens. No responsive sizing is required since the splash is shown only on portrait-locked launch.

## Phase 1: Design & Contracts

### Screen.Splash — new route

```kotlin
data object Splash : Screen("splash", Res.string.splash)
```

`AppNavHost` start destination changes from `Screen.Tabs.route` → `Screen.Splash.route`.

### SplashNavigation.kt

```kotlin
fun NavGraphBuilder.splashNavGraph(onSplashComplete: () -> Unit) {
    composable(Screen.Splash.route) {
        SplashRoute(onSplashComplete = onSplashComplete)
    }
}
```

### SplashScreen.kt

```kotlin
@Composable
fun SplashRoute(onSplashComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3_000L)
        onSplashComplete()
    }
    SplashScreen()
}

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.recipe_app_logo),
            contentDescription = null,
            modifier = Modifier.size(160.dp)
        )
    }
}
```

### AppNavHost.kt changes

```kotlin
// startDestination changes to Screen.Splash.route
// splashNavGraph added, passing appState.navigateToTabs() as the callback
// navigate call uses popUpTo(Screen.Splash.route) { inclusive = true }
```

### AppState.kt — no changes needed

`navigateToTabs()` already exists and already calls `navController.navigateToTabs()`.

### strings.xml — add one entry

```xml
<string name="splash">Splash</string>
```

### data-model.md

Not applicable — this feature introduces no domain entities, DTOs, DAOs, or repository layer. No `data-model.md` needed.
