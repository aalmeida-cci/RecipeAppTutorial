# Quickstart: Profile Screen UI

**Feature**: 003-profile-screen
**Date**: 2026-05-28

Step-by-step path to add, build, and visually verify the Profile screen on Android (primary) and iOS (smoke check).

---

## 1. Files to add / modify

| Action | Path |
|--------|------|
| **NEW** | `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/profile/ui/ProfileUiState.kt` |
| **NEW** | `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/profile/ui/ProfileViewModel.kt` |
| **REWRITE** | `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/profile/ui/ProfileScreen.kt` |
| **MODIFY** | `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/di/ViewModelModule.kt` (add `viewModel { ProfileViewModel() }`) |

No changes to navigation, theme, resources, or any data layer.

## 2. Skeleton (do NOT paste this into the codebase — it lives here for orientation only)

`ProfileUiState.kt`:

```kotlin
package com.adrian.recipeapp.features.profile.ui

data class ProfileUiState(
    val email: String,
)
```

`ProfileViewModel.kt`:

```kotlin
package com.adrian.recipeapp.features.profile.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState(email = "test@gmail.com"))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
}
```

`ViewModelModule.kt` (add line):

```kotlin
viewModel { ProfileViewModel() }
```

`ProfileScreen.kt` — `ProfileRoute` (stateful) + `ProfileScreen` (stateless) + private content composables (`ProfileAvatar`, `SettingsGroup`, `SettingsRow`). See contracts/profile-ui-contract.md for the exact hierarchy.

## 3. Build & run

```bash
# Android (debug install on running emulator/device)
./gradlew :composeApp:installDebug

# Android (assemble only, no install)
./gradlew :composeApp:assembleDebug

# All tests (must remain green)
./gradlew :composeApp:allTests

# iOS — open Xcode workspace and run on simulator
open iosApp/iosApp.xcworkspace
```

## 4. Manual verification checklist

Launch app → bottom-nav → Profile tab. Verify each of the following:

- [ ] Toolbar shows "Profile" left-aligned, divider line directly below toolbar (matches Favourites styling).
- [ ] Centered 120dp circular avatar uses `profile_dummy.png` (silhouette visible).
- [ ] Small floating button overlays the avatar's bottom-right edge, contains the camera icon (`PhotoCamera`), has FAB elevation/shadow.
- [ ] Text `test@gmail.com` appears centered below the avatar (single line).
- [ ] Below the email, ONE rounded grouped container holds six rows in this order:
  1. QR Code Scan  (icon: QrCodeScanner)
  2. QR Code Generation  (icon: QrCode2)
  3. Connect to Bluetooth Device  (icon: Bluetooth)
  4. Notification  (icon: Notifications)
  5. Language  (icon: Language)
  6. Logout  (icon: Logout, auto-mirrored)
- [ ] Each row shows leading icon, label, trailing right-chevron.
- [ ] Adjacent rows separated by a thin `HorizontalDivider` — no per-row card and no divider before row 1 or after row 6.
- [ ] Logout row visually identical to the other five (no red tint).
- [ ] Tapping the camera button and any row shows a ripple but performs no navigation.
- [ ] Toggle device dark mode — every surface, icon, divider, and text remains legible (no white-on-white or black-on-black).
- [ ] On a 360dp-wide emulator (e.g., Pixel 4a), vertical scroll reaches the Logout row without clipping.

## 5. Static checks (constitution gates — block merge if any fail)

```bash
# Lint / format
./gradlew spotlessApply

# Build (must succeed without warnings related to the new files)
./gradlew :composeApp:assembleDebug
```

Code review must confirm:

- [ ] No `Color(0xFF…)` literal in any of the new files.
- [ ] No `"test@gmail.com"` literal inside any `@Composable`.
- [ ] `Modifier` is the first optional parameter in every new composable.
- [ ] `collectAsStateWithLifecycle()` (not `collectAsState()`).
- [ ] `koinViewModel()` (no manual `ProfileViewModel()` construction in `@Composable`).
- [ ] No `android.*` / `platform.*` imports in any new file.
- [ ] No new dependency added to `composeApp/build.gradle.kts`.

## 6. Out of scope

- Wiring tap actions on the camera button or any settings row.
- Replacing the hardcoded email with a real authenticated user identity.
- Localizing row labels to `Res.string.*`.
- Introducing `features/profile/domain/` or `features/profile/data/` packages.

These are deferred to follow-up features and tracked under `plan.md` → Complexity Tracking → Follow-up obligation.
