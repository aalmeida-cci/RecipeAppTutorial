# Implementation Plan: Profile Screen UI

**Branch**: `003-profile-screen` | **Date**: 2026-05-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/003-profile-screen/spec.md`

## Summary

Add a UI-only Profile screen to the existing bottom-tab shell. Screen renders: left-aligned "Profile" `TopAppBar` (matching Favourites styling), a centered 120dp circular avatar (`profile_dummy.png`) with a `SmallFloatingActionButton` overlay containing a `PhotoCamera` icon at its bottom-right, the hardcoded email `test@gmail.com` below the avatar, and a single rounded grouped surface holding six rows (QR Code Scan, QR Code Generation, Connect to Bluetooth Device, Notification, Language, Logout) separated by `HorizontalDivider`s. Each row shows a leading Material icon, label, and trailing `ChevronRight`. No tap actions are wired in v1.

A new `ProfileViewModel` exposes `StateFlow<ProfileUiState>` with the hardcoded email; the composable consumes state via `collectAsStateWithLifecycle()` and `koinViewModel()`.

## Technical Context

**Language/Version**: Kotlin 2.3.20 (Multiplatform)

**Primary Dependencies**: Compose Multiplatform 1.10.3, Material 3 1.10.0-alpha05, Koin 4.2.1 (Core + Compose + ViewModel), Jetbrains Navigation Compose 2.9.2, Jetbrains Lifecycle ViewModel Compose 2.10.0, Coil 3.4.0 (already in project; not used here — `Image` with `painterResource` suffices for a static bundled drawable)

**Storage**: N/A (UI-only feature; email is in-memory hardcoded inside ViewModel)

**Testing**: Manual smoke verification on Android emulator + iOS simulator. No new unit tests required (no business logic introduced). Existing `:composeApp:allTests` must continue to pass.

**Target Platform**: Android (minSdk 24, targetSdk 36) and iOS (iosArm64, iosSimulatorArm64) via `composeApp/src/commonMain`

**Project Type**: Mobile (Kotlin Multiplatform + Compose Multiplatform shared UI)

**Performance Goals**: First-frame render of Profile screen ≤ 16ms on mid-tier hardware (single composition, no async loads). 60fps scroll over the grouped settings container.

**Constraints**:
- All UI in `commonMain` — zero `android.*` / `platform.*` imports.
- No hardcoded colors; use `MaterialTheme.colorScheme.*`.
- No literal email string inside the composable — must come from `ProfileUiState`.
- No new persistence, no new network calls, no new repository.

**Scale/Scope**: One new screen (Profile), one new ViewModel, one new UiState data class, one DI registration update, ~150 LOC total in `commonMain`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Clean Architecture + MVVM | ✅ Pass (UI-Only Exception) | UI + ViewModel + UiState present. `domain/` and `data/` layers omitted under the constitution v1.0.1 UI-Only Feature Exception — no data to fetch, persist, or map; email is a hardcoded constant in the ViewModel. Complexity Tracking documents justification and follow-up obligation. |
| II. commonMain First | ✅ Pass | All new code lives in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/profile/`. No platform-specific imports. |
| III. Cache-First Data Strategy | ✅ N/A | No data layer exists for this feature; cache-first rule has no surface to apply. When real auth/profile data lands, the future feature MUST introduce `domain/` + `data/` layers and follow cache-first. |
| IV. Code Reuse — No Duplication | ✅ Pass | Reuses existing `profile_dummy.png`, `MaterialTheme.colorScheme.*`, `AppTypography`, Material icons. No new HTTP client, DAO, mapper, or theme tokens introduced. |
| V. Feature Package Structure | ✅ Pass (UI-Only Exception) | Only `ui/` + `navigation/` packages per constitution v1.0.1 UI-Only Feature Exception. `domain/` and `data/` not created — no domain entity or data source exists for this feature. |

**Architecture & Source Set Constraints checklist** (all enforced in code review of the implementing PR):
- ✅ Navigation graph remains a `NavGraphBuilder` extension function (`profileNavGraph()` already exists).
- ✅ `Screen.Profile.route` remains the single source of truth for the route string.
- ✅ ViewModel registered with `viewModel { }` in `ViewModelModule`, retrieved via `koinViewModel()` in `ProfileRoute`.
- ✅ `collectAsStateWithLifecycle()` used in the composable.
- ✅ `Modifier` is the first optional parameter in every new composable (`ProfileScreen`, private content composables).
- ✅ No DTOs, no SQLDelight queries, no DAO usage — none required.
- ✅ No `Color(0xFF…)` literals.

**Gates**: PASS. Both Partials resolved by constitution v1.0.1 UI-Only Feature Exception (see Complexity Tracking for full justification).

## Project Structure

### Documentation (this feature)

```text
specs/003-profile-screen/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── profile-ui-contract.md   # ViewModel ↔ Screen UI binding contract
├── checklists/
│   └── requirements.md  # Spec quality checklist (already exists)
└── tasks.md             # Phase 2 output (NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
composeApp/src/commonMain/kotlin/com/adrian/recipeapp/
├── di/
│   └── ViewModelModule.kt              ← MODIFIED: add `viewModel { ProfileViewModel() }`
└── features/profile/
    ├── navigation/
    │   └── ProfileNavigation.kt        ← UNCHANGED (already wires `ProfileRoute`)
    └── ui/
        ├── ProfileScreen.kt            ← REWRITTEN: Route + Screen composables + private content sub-composables
        ├── ProfileUiState.kt           ← NEW: `data class ProfileUiState(val email: String)`
        └── ProfileViewModel.kt        ← NEW: exposes `StateFlow<ProfileUiState>` with hardcoded email
```

**Structure Decision**: Use the existing `features/profile/{ui,navigation}` package layout (already created on disk). Do NOT create `features/profile/domain/` or `features/profile/data/` for this feature — see Complexity Tracking justification. When real authentication arrives in a follow-up feature, those layers will be added then.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| Principle I deviation: no Repository / DataSource layer | Feature has no data to fetch, persist, or map — email is a UI-only placeholder until real auth is built. Creating empty `ProfileRepository` / `ProfileLocalDataSource` / `ProfileRemoteDataSource` would be dead code that violates Principle IV (Code Reuse — No Duplication) and the "no half-finished implementations" guideline. | Adding a no-op repository that just returns `Result.success("test@gmail.com")` rejected because it ships unused abstractions that future readers must understand and that confuse the cache-first pattern (there is no cache and no remote). |
| Principle V deviation: no `domain/` and `data/` packages for this feature | Same reason as above — there is no domain entity to model (the email is a primitive `String`) and no DTO to serialize. Creating those packages with placeholder files would conflict with the constitution's own "no duplication / no premature abstraction" stance. | Creating empty `domain/entities/Profile.kt` with a single `email: String` field rejected because the existing `ProfileUiState` already serves as the typed carrier for the email. A second wrapper type adds zero behavior. |

**Follow-up obligation**: When the next Profile-related feature lands (e.g., real auth, dynamic email loading, avatar upload), it MUST introduce `features/profile/domain/` + `features/profile/data/` and migrate the hardcoded email from `ProfileViewModel` into a proper `ProfileRepository` returning `Result<UserProfile>` per cache-first strategy. Track this in the follow-up feature spec.
