---
description: "Task list for Splash Screen feature implementation"
---

# Tasks: Splash Screen

**Input**: Design documents from `/specs/001-splash-screen/`

**Prerequisites**: [plan.md](plan.md) ✅ · [spec.md](spec.md) ✅

**Tests**: Manual only — no automated test tasks (not requested in spec)

**Organization**: Single user story (P1). All tasks are sequential except where marked [P].

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1)
- Paths relative to `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add the string resource and route entry needed before UI work begins

- [X] T001 Add `<string name="splash">Splash</string>` to `composeApp/src/commonMain/composeResources/values/strings.xml`
- [X] T002 Add `data object Splash : Screen("splash", Res.string.splash)` to `features/app/data/Screen.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core navigation wiring that the splash UI depends on

**⚠️ CRITICAL**: Phase 1 must be complete before this phase begins

- [X] T003 Change `startDestination` from `Screen.Tabs.route` to `Screen.Splash.route` in `features/app/navigation/AppNavHost.kt`
- [X] T004 Add `splashNavGraph(onSplashComplete = { appState.navigateToTabs() })` call inside `AppNavHost` NavHost block in `features/app/navigation/AppNavHost.kt`, ensuring the navigate call uses `popUpTo(Screen.Splash.route) { inclusive = true }` and that the `splashNavGraph` import resolves

**Checkpoint**: Navigation skeleton ready — splash route wired but composable not yet created

---

## Phase 3: User Story 1 — App Launch Splash Display (Priority: P1) 🎯 MVP

**Goal**: Display a full-screen branded splash screen for 3 seconds on cold launch, then auto-navigate to TabsScreen with splash removed from the back stack.

**Independent Test**: Cold-launch the app. Splash screen must appear immediately with the logo centred on the primary-colour background; after 3 seconds it must transition automatically to the home feed. Pressing Back from the feed must exit the app, not return to splash.

### Implementation for User Story 1

- [X] T005 [P] [US1] Create `features/splash/navigation/SplashNavigation.kt` with `fun NavGraphBuilder.splashNavGraph(onSplashComplete: () -> Unit)` composable destination for `Screen.Splash.route`
- [X] T006 [P] [US1] Create `features/splash/ui/SplashScreen.kt` with `SplashRoute` composable (contains `LaunchedEffect(Unit) { delay(3_000L); onSplashComplete() }`) and `SplashScreen` composable (`modifier: Modifier = Modifier`, full-screen `Box` with `MaterialTheme.colorScheme.primary` background, `Image` of `Res.drawable.recipe_app_logo` at 160.dp centred)

**Checkpoint**: Full flow functional — cold launch shows splash for 3s, navigates to tabs, back-press exits app

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Verification and cleanup

- [X] T007 [P] Verify `recipe_app_logo.png` exists at `composeApp/src/commonMain/composeResources/drawable/recipe_app_logo.png` and is referenced correctly via `Res.drawable.recipe_app_logo`
- [X] T008 Run `./gradlew :composeApp:assembleDebug` and confirm zero build errors
- [ ] T009 [P] Install on Android device/emulator with `./gradlew :composeApp:installDebug` and manually verify all acceptance scenarios from spec.md (FR-001 through FR-007, SC-001 Android leg, SC-002, SC-003, SC-004 across at least two screen densities)
- [ ] T010 [P] Open `iosApp/iosApp.xcworkspace` in Xcode, run the iOS target on an iOS simulator, and manually verify the same acceptance scenarios — completes SC-001's iOS leg and confirms commonMain UI renders identically on iOS

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on T001, T002 (string resource + Screen route must exist)
- **User Story 1 (Phase 3)**: T005 and T006 can run in parallel after Phase 2
- **Polish (Phase 4)**: Depends on Phase 3 completion; T009 (Android verify) and T010 (iOS verify) can run in parallel on different machines

### Within User Story 1

- T005 and T006 are parallel (different files, no dependency between them)

### Parallel Opportunities

```
# Phase 3 — launch in parallel:
T005: Create SplashNavigation.kt
T006: Create SplashScreen.kt

# Phase 4 — verification in parallel:
T009: Android device/emulator verification
T010: iOS simulator verification
```

---

## Implementation Strategy

### MVP (This feature is a single story — complete in one pass)

1. Complete Phase 1: Add string resource + Screen.Splash route
2. Complete Phase 2: Update AppNavHost start destination and call site
3. Complete Phase 3: Create composables, confirm build
4. Complete Phase 4: Manual verification on device

### Notes

- No ViewModel, no DI changes, no DB or network — pure UI feature
- `navigateToTabs()` already exists on `AppState` — no new navigation function needed
- `popUpTo(Screen.Splash.route) { inclusive = true }` ensures FR-007 (no back-stack return)
- Timer starts after first composition via `LaunchedEffect(Unit)` — satisfies the "render within one frame" performance goal
