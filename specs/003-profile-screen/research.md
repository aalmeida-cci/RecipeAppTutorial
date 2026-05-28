# Research: Profile Screen UI

**Feature**: 003-profile-screen
**Date**: 2026-05-28

All `NEEDS CLARIFICATION` items from the plan's Technical Context were resolved during `/speckit-clarify` (recorded in spec under `## Clarifications` § Session 2026-05-28). This document captures the technical decisions that back the implementation.

---

## Decision 1: Floating action button — component choice

- **Decision**: `androidx.compose.material3.SmallFloatingActionButton` (40dp).
- **Rationale**: Matches the "small floating" wording in the spec, provides built-in M3 elevation/shadow tokens that visually distinguish the affordance against the avatar, and is the idiomatic component for `add`-style entry points in Material 3. Container color defaults to `colorScheme.primaryContainer` which contrasts well with the neutral grayscale `profile_dummy.png` in both light and dark themes.
- **Alternatives considered**:
  - `IconButton` inside a `Surface(shape = CircleShape, color = colorScheme.primary)` — rejected because it loses the FAB elevation token and requires hand-tuning shadow to match.
  - `FilledIconButton` — rejected because it has no elevation by default; "floating" affordance is lost.

## Decision 2: Camera icon

- **Decision**: `androidx.compose.material.icons.filled.PhotoCamera`.
- **Rationale**: Universally recognized as "take/upload photo", part of the Material icons bundle already on the classpath via `compose.material.icons` (no new dependency). Pairs naturally with the avatar upload affordance the spec describes.
- **Alternatives considered**:
  - `AddAPhoto` — rejected because it includes an embedded "+" decoration, redundant given the previous "plus icon" wording was explicitly superseded by "camera icon".
  - `CameraAlt` — visually similar but slightly less commonly used; `PhotoCamera` is the canonical choice across Android system Settings.

## Decision 3: Avatar component

- **Decision**: `androidx.compose.foundation.Image` with `painterResource(Res.drawable.profile_dummy)` clipped to `CircleShape` and sized `120.dp`.
- **Rationale**: `profile_dummy.png` is a static bundled drawable in `composeApp/src/commonMain/composeResources/drawable/`. Coil's `AsyncImage` is reserved for remote URLs (used elsewhere in the project for recipe images); using it here would route a local resource through unnecessary async machinery. `painterResource` from `org.jetbrains.compose.resources` is the canonical commonMain API for bundled drawables.
- **Alternatives considered**:
  - `coil3.compose.AsyncImage(model = Res.drawable.profile_dummy)` — rejected: misuse of Coil for a local bundled resource.

## Decision 4: Settings list — layout primitive

- **Decision**: A single `Surface` (`shape = MaterialTheme.shapes.large`, `color = MaterialTheme.colorScheme.surfaceContainer`) wrapping a `Column` of six row composables, with `HorizontalDivider` between adjacent rows. No `LazyColumn`.
- **Rationale**: Spec Q1 clarified that rows are grouped inside one rounded container with dividers (no per-row `Card`). The list has a fixed length of six items, so `LazyColumn` would add lifecycle complexity (item keys, recomposition scoping) with zero scrolling-recycling benefit. Wrapping the whole screen in `verticalScroll(rememberScrollState())` instead of using `LazyColumn` lets the avatar + email + list scroll as one block on small screens.
- **Alternatives considered**:
  - `LazyColumn` with six items — rejected as over-engineered for a fixed-length, fully-visible list.
  - Six individual `Card`s — rejected by Q1 clarification.

## Decision 5: Row icons (per-row Material icon picks)

- **Decision**:
  - QR Code Scan: `Icons.Default.QrCodeScanner`
  - QR Code Generation: `Icons.Default.QrCode2`
  - Connect to Bluetooth Device: `Icons.Default.Bluetooth`
  - Notification: `Icons.Default.Notifications`
  - Language: `Icons.Default.Language`
  - Logout: `Icons.AutoMirrored.Filled.Logout`
- **Rationale**: All are present in `androidx.compose.material.icons.*` (Material Icons bundle already on classpath). `AutoMirrored.Filled.Logout` correctly mirrors in RTL locales — required to follow the project's stance on accessibility-friendly icons.
- **Alternatives considered**:
  - `Icons.Default.ExitToApp` for Logout — rejected because it is not auto-mirrored for RTL.
  - `Icons.Default.QrCode` for both QR rows — rejected because two visually identical rows confuse the user.

## Decision 6: Trailing chevron

- **Decision**: `Icons.AutoMirrored.Filled.KeyboardArrowRight` rendered with `colorScheme.onSurfaceVariant` at 18dp.
- **Rationale**: Auto-mirroring is critical for RTL languages; the chevron must flip to point left in RTL. `onSurfaceVariant` is the M3 token for low-emphasis trailing affordances and remains legible in both themes.
- **Alternatives considered**:
  - `Icons.Default.ChevronRight` — rejected, not auto-mirrored.

## Decision 7: Spacing values

- **Decision**:
  - Toolbar → avatar top spacing: `24.dp`
  - Avatar diameter: `120.dp` (per Q3 clarification)
  - Avatar → email spacing: `12.dp`
  - Email → settings list spacing: `24.dp`
  - Settings list horizontal padding: `16.dp`
  - Settings row internal padding: `vertical = 14.dp, horizontal = 16.dp`
  - Settings row icon → label gap: `16.dp`
  - Bottom safe-area spacing: `24.dp`
- **Rationale**: Values align with Material 3 settings-screen reference layouts (Android 14 Settings) and the existing Favourites screen's `16.dp` horizontal padding language. They satisfy SC-003 (no clipping at 360dp width) verified by hand math: avatar (120) + 16dp padding × 2 (32) = 152dp < 360dp.
- **Alternatives considered**: Tighter spacing rejected because the camera FAB overlap at the avatar's bottom-right needs ~8dp clear room to avoid colliding with the email below.

## Decision 8: ViewModel implementation

- **Decision**:

  ```kotlin
  class ProfileViewModel : ViewModel() {
      private val _uiState = MutableStateFlow(ProfileUiState(email = "test@gmail.com"))
      val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
  }
  ```

  Base class: `androidx.lifecycle.ViewModel` (commonMain via Jetbrains Lifecycle ViewModel Compose 2.10.0).
- **Rationale**: Matches the pattern used by `FavouriteScreenViewModel`, `FeedViewModel`, and other ViewModels in the project. `MutableStateFlow` is private; only `StateFlow` is exposed (Principle I requirement). No constructor parameters — no dependencies to inject.
- **Alternatives considered**:
  - Plain `object` singleton — rejected: violates Principle V (ViewModel layer required), breaks Compose-aware lifecycle.

## Decision 9: Composable layering

- **Decision**: Three composables in `ProfileScreen.kt`:
  1. `ProfileRoute(viewModel = koinViewModel())` — stateful, owns state collection.
  2. `ProfileScreen(uiState, modifier)` — stateless, takes `ProfileUiState`, renders content.
  3. Private content sub-composables: `ProfileAvatar`, `SettingsGroup`, `SettingsRow`.
- **Rationale**: Mirrors the `FavouritesRoute` / `FavouritesScreen` split already used in the project. Stateless `ProfileScreen` enables future preview/testing without Koin. `Modifier` is the first optional parameter on every composable.
- **Alternatives considered**: Single monolithic composable — rejected: violates readability and the route/screen split convention.

## Decision 10: Scaffold + scroll strategy

- **Decision**: `Scaffold(topBar = { TopAppBar(...) })` exactly mirroring `FavouritesScreen`. Body uses `Column(Modifier.verticalScroll(rememberScrollState()).padding(top = innerPadding.calculateTopPadding()))`.
- **Rationale**: Consistent with the Favourites toolbar styling (`MaterialTheme.colorScheme.background` container, thin `HorizontalDivider` below). Matches FR-001 and the spec's "just like in FavouriteScreen" anchor.
- **Alternatives considered**: `LazyColumn` for the whole body — rejected: fixed content, no recycling needed.
