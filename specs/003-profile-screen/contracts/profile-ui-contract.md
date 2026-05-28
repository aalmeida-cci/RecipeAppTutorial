# UI Contract: Profile Screen

**Feature**: 003-profile-screen
**Date**: 2026-05-28

This feature has no external API, no network protocol, and no IPC surface. The only "contract" is the boundary between the `ProfileViewModel` (state producer) and the `ProfileScreen` composable (state consumer). This file documents that boundary so future contributors can swap the data source (e.g., wire in a real repository) without changing the screen.

---

## ViewModel ↔ Composable contract

### State producer: `ProfileViewModel`

**Package**: `com.adrian.recipeapp.features.profile.ui`

**Public surface**:

```kotlin
class ProfileViewModel : androidx.lifecycle.ViewModel() {
    val uiState: kotlinx.coroutines.flow.StateFlow<ProfileUiState>
}
```

**Guarantees**:

- `uiState.value` is always non-null and has a non-null `email` field.
- Initial emission carries `ProfileUiState(email = "test@gmail.com")`.
- ViewModel exposes `StateFlow<ProfileUiState>` only — no `MutableStateFlow`, no `SharedFlow`, no `LiveData`, no raw `suspend` functions on the public surface.
- No constructor parameters in v1. Future versions may inject a repository; consumers must not depend on a parameterless constructor — always obtain via `koinViewModel()`.

### State consumer: `ProfileRoute` → `ProfileScreen`

**Stateful composable**:

```kotlin
@Composable
fun ProfileRoute(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel()
)
```

- Collects state via `collectAsStateWithLifecycle()` (constitution gate — `collectAsState()` is forbidden).
- Delegates rendering to the stateless `ProfileScreen`.

**Stateless composable**:

```kotlin
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
)
```

- Pure function of `uiState`. No side effects, no `LaunchedEffect`, no Koin lookups.
- Reads `uiState.email` and renders it inside the email `Text` composable.
- `Modifier` is the first optional parameter (constitution gate).

### DI registration

Module: `com.adrian.recipeapp.di.viewModelModule()`

Add:

```kotlin
viewModel { ProfileViewModel() }
```

### Navigation entry point

Existing — no change required:

- `Screen.Profile.route` ("profile") in `features/app/data/Screen.kt`
- `NavGraphBuilder.profileNavGraph()` in `features/profile/navigation/ProfileNavigation.kt` — already calls `ProfileRoute()`.

After the new `ProfileRoute` signature accepts a `modifier` parameter, the existing call site `ProfileRoute()` continues to work because `modifier` has a default value.

---

## Visual contract (acceptance surface)

The screen MUST render the following hierarchy under the tab's safe-area padding (verified visually during `/run` and code-reviewed against `FR-001..FR-012`):

```
Scaffold
├── topBar = TopAppBar(title = "Profile", colors = background)
└── content (Column with verticalScroll)
    ├── HorizontalDivider (thin, alpha 0.5 outline)
    ├── Spacer (24.dp)
    ├── ProfileAvatar (Box, centered)
    │   ├── Image (120.dp circular, profile_dummy.png)
    │   └── SmallFloatingActionButton (PhotoCamera icon, bottom-right overlay)
    ├── Spacer (12.dp)
    ├── Text(uiState.email, single-line, ellipsis, centered)
    ├── Spacer (24.dp)
    └── SettingsGroup (Surface, shape = shapes.large, color = surfaceContainer)
        └── Column
            ├── SettingsRow(QrCodeScanner, "QR Code Scan", ChevronRight)
            ├── HorizontalDivider
            ├── SettingsRow(QrCode2, "QR Code Generation", ChevronRight)
            ├── HorizontalDivider
            ├── SettingsRow(Bluetooth, "Connect to Bluetooth Device", ChevronRight)
            ├── HorizontalDivider
            ├── SettingsRow(Notifications, "Notification", ChevronRight)
            ├── HorizontalDivider
            ├── SettingsRow(Language, "Language", ChevronRight)
            ├── HorizontalDivider
            └── SettingsRow(AutoMirrored.Logout, "Logout", ChevronRight)
```

### Invariants enforced by code review

- No `Color(0xFF...)` literal anywhere in the new files.
- No raw string literal `"test@gmail.com"` inside any `@Composable` function — only inside `ProfileViewModel`.
- `ProfileScreen` does not import `org.koin.*`.
- All Material icons referenced via `androidx.compose.material.icons.*` — no new drawable asset added.
- `HorizontalDivider` is the only separator between rows. No `Card`, no `Divider` (deprecated alias).
- The `Logout` row uses identical styling to every other row (per FR-007a).
