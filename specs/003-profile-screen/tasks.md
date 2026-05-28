---

description: "Task list for Profile Screen UI feature"
---

# Tasks: Profile Screen UI

**Input**: Design documents from `/specs/003-profile-screen/`

**Prerequisites**: plan.md (loaded), spec.md (loaded), research.md (loaded), data-model.md (loaded), contracts/profile-ui-contract.md (loaded), quickstart.md (loaded)

**Tests**: Not requested in spec. No automated test tasks are generated. The acceptance surface is manual verification per quickstart.md §4.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (US1, US2)
- Exact file paths included in every implementation task

## Path Conventions

- Kotlin Multiplatform / Compose Multiplatform mobile project.
- All new feature code lives in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/profile/`.
- DI registration in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/di/ViewModelModule.kt`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm project state — no new dependencies, no new modules.

- [X] T001 Verify branch is `003-profile-screen` and `git status` is clean before starting (run `git branch --show-current && git status` from repo root)
- [X] T002 Confirm `composeApp/src/commonMain/composeResources/drawable/profile_dummy.png` exists (no new asset needed)
- [X] T003 Confirm `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/profile/navigation/ProfileNavigation.kt` already wires `ProfileRoute()` for `Screen.Profile.route` (no navigation change needed)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: No foundational work needed. This feature introduces no shared infrastructure (no DB schema, no auth, no networking). User Story phases can begin immediately after Phase 1.

**⚠️ CRITICAL**: This phase is intentionally empty — proceed directly to Phase 3.

---

## Phase 3: User Story 1 - View Profile Identity (Priority: P1) 🎯 MVP

**Goal**: User opens the Profile tab and sees toolbar, circular avatar (`profile_dummy.png`) with a camera-icon FAB overlay, and the email `test@gmail.com` below the avatar.

**Independent Test**: Launch app → tap Profile tab → verify the toolbar shows "Profile" left-aligned, a centered 120dp circular avatar appears with a small floating `PhotoCamera` button at its bottom-right edge, and `test@gmail.com` appears centered below the avatar. (Per spec.md User Story 1 Acceptance Scenarios 1-3.)

### Implementation for User Story 1

- [X] T004 [P] [US1] Create `ProfileUiState` data class with single `email: String` field in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/profile/ui/ProfileUiState.kt`
- [X] T005 [P] [US1] Create `ProfileViewModel` extending `androidx.lifecycle.ViewModel` with private `MutableStateFlow(ProfileUiState(email = "test@gmail.com"))` and public `StateFlow<ProfileUiState>` in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/profile/ui/ProfileViewModel.kt`
- [X] T006 [US1] Register `viewModel { ProfileViewModel() }` inside `viewModelModule()` in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/di/ViewModelModule.kt` (depends on T005)
- [X] T007 [US1] Rewrite `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/profile/ui/ProfileScreen.kt`: add `ProfileRoute(modifier: Modifier = Modifier, viewModel: ProfileViewModel = koinViewModel())` that collects state via `collectAsStateWithLifecycle()` and delegates to a stateless `ProfileScreen(uiState: ProfileUiState, modifier: Modifier = Modifier)`. Inside `ProfileScreen`, scaffold matches Favourites: `TopAppBar` with title "Profile" (left-aligned), `MaterialTheme.colorScheme.background` container, thin `HorizontalDivider` below toolbar, `systemBarsPadding()`, body as a `Column` with `verticalScroll(rememberScrollState())` (depends on T004, T005, T006)
- [X] T008 [US1] Inside `ProfileScreen.kt`, add private `ProfileAvatar` composable: `Box(Modifier.align(CenterHorizontally))` containing (a) `Image(painter = painterResource(Res.drawable.profile_dummy), modifier = Modifier.size(120.dp).clip(CircleShape), contentDescription = null)` and (b) overlaid `SmallFloatingActionButton(onClick = {}, modifier = Modifier.align(Alignment.BottomEnd))` containing `Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)`. Wire spacing per research.md §7 (depends on T007)
- [X] T009 [US1] Inside `ProfileScreen.kt`, render `Text(text = uiState.email, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.align(CenterHorizontally).padding(top = 12.dp))` directly below the avatar (depends on T008)

**Checkpoint**: User Story 1 is rendered. Tab into Profile and verify toolbar + avatar + camera FAB + email are visible. Settings list is NOT yet present.

---

## Phase 4: User Story 2 - Browse Settings Options (Priority: P2)

**Goal**: A single rounded grouped container below the email holds six rows (QR Code Scan, QR Code Generation, Connect to Bluetooth Device, Notification, Language, Logout), each with leading icon + label + trailing chevron, separated by `HorizontalDivider`s.

**Independent Test**: Per spec.md User Story 2 Acceptance Scenarios 1-4: scroll below the email, verify exactly one rounded container holds six rows in the specified order; each row has icon + label + trailing right-chevron; adjacent rows separated by a thin divider (no divider before row 1 or after row 6); content remains scrollable on 360dp-wide devices; the toolbar stays anchored.

### Implementation for User Story 2

- [X] T010 [US2] Inside `ProfileScreen.kt`, add a private `data class SettingsRowItem(val icon: ImageVector, val label: String)` (file-private) and a private `val settingsRows = listOf(...)` containing the six items in this exact order: (`QrCodeScanner`, "QR Code Scan"), (`QrCode2`, "QR Code Generation"), (`Bluetooth`, "Connect to Bluetooth Device"), (`Notifications`, "Notification"), (`Language`, "Language"), (`AutoMirrored.Filled.Logout`, "Logout") (depends on T009). **Icon verification**: all six icons are from `material-icons-extended` (already in `commonMain` dependencies at v1.7.3). If `Icons.Default.QrCode2` fails to compile, substitute `Icons.Default.QrCode` for the "QR Code Generation" row — both render a QR-code glyph, `QrCode2` is the filled variant.
- [X] T011 [US2] Inside `ProfileScreen.kt`, add a private `SettingsRow(item: SettingsRowItem, modifier: Modifier = Modifier)` composable: `Row(modifier.fillMaxWidth().clickable {}.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = CenterVertically)` containing leading `Icon(item.icon, tint = MaterialTheme.colorScheme.onSurface)`, `Spacer(Modifier.width(16.dp))`, label `Text(item.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))`, trailing `Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))` (depends on T010)
- [X] T012 [US2] Inside `ProfileScreen.kt`, add a private `SettingsGroup(items: List<SettingsRowItem>, modifier: Modifier = Modifier)` composable: `Surface(modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) { Column { items.forEachIndexed { index, item -> SettingsRow(item); if (index < items.lastIndex) HorizontalDivider(thickness = 0.3.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) } } }` (depends on T011)
- [X] T013 [US2] In `ProfileScreen.kt`, call `Spacer(Modifier.height(24.dp))` followed by `SettingsGroup(items = settingsRows)` below the email `Text`, and add a trailing `Spacer(Modifier.height(24.dp))` at the bottom of the scrolling `Column` (depends on T010, T012)

**Checkpoint**: User Story 2 is rendered. Profile tab now shows the full screen — toolbar, avatar with FAB, email, and the six-row grouped settings list with dividers. No tap actions wired.

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Validation, lint, and verification before merge.

- [X] T014 [P] Run `./gradlew spotlessApply` from repo root to apply formatting
- [X] T015 [P] Run `./gradlew :composeApp:assembleDebug` from repo root and confirm clean build (no warnings on new files)
- [X] T016 [P] Run `./gradlew :composeApp:allTests` from repo root and confirm all existing tests still pass
- [ ] T017 Install on Android emulator (`./gradlew :composeApp:installDebug`) and step through quickstart.md §4 manual verification checklist in light theme
- [ ] T018 Toggle Android emulator to dark theme and re-run quickstart.md §4 manual verification checklist
- [ ] T019 Verify on a 360dp-wide emulator profile (e.g., Pixel 4a) that all six rows in the settings group are reachable via scroll without clipping (SC-003)
- [ ] T020 Smoke-check on iOS simulator: open `iosApp/iosApp.xcworkspace`, run, navigate to Profile tab, and confirm toolbar + avatar + FAB + email + settings group render (no platform-specific regressions)
- [X] T021 Code review against constitution gate checklist in plan.md → Constitution Check (verify Modifier ordering, `collectAsStateWithLifecycle()`, `koinViewModel()`, no `Color(0xFF…)`, no hardcoded email in composable, no `android.*` / `platform.*` imports)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No code dependencies — pure verification.
- **Foundational (Phase 2)**: Empty — no work to do.
- **User Story 1 (Phase 3)**: Can start immediately after Phase 1.
- **User Story 2 (Phase 4)**: Depends on User Story 1 completion because both stories edit `ProfileScreen.kt`. They cannot be staffed truly in parallel without merge conflicts on that file. However, US2 is still independently deliverable and testable per spec.md User Story 2 priority.
- **Polish (Phase N)**: Depends on US1 and US2 completion.

### User Story Dependencies

- **User Story 1 (P1)**: No dependencies on other stories. MVP-deliverable on its own.
- **User Story 2 (P2)**: Independently testable but file-level depends on US1 because both stories edit `ProfileScreen.kt`. Sequential implementation recommended.

### Within Each User Story

- T004, T005 can run in parallel (different new files).
- T006 depends on T005 (registers the VM).
- T007 depends on T004, T005, T006 (consumes the VM + UI state).
- T008 depends on T007 (extends `ProfileScreen.kt`).
- T009 depends on T008 (renders email below avatar block).
- T010–T013 are sequential within US2 (all touch `ProfileScreen.kt`).

### Parallel Opportunities

- T004 and T005 (different new files) can be done in parallel.
- T014, T015, T016 (independent gradle invocations) can run in parallel.
- All polish tasks marked [P] are gradle/CLI invocations on independent artifacts.

---

## Parallel Example: User Story 1

```bash
# Launch the two new-file tasks together:
Task: "Create ProfileUiState in composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/profile/ui/ProfileUiState.kt"
Task: "Create ProfileViewModel in composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/profile/ui/ProfileViewModel.kt"
```

## Parallel Example: Polish

```bash
# Independent gradle invocations:
Task: "./gradlew spotlessApply"
Task: "./gradlew :composeApp:assembleDebug"
Task: "./gradlew :composeApp:allTests"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (verification).
2. Skip Phase 2 (empty).
3. Complete Phase 3 (US1: T004 → T009).
4. **STOP and VALIDATE**: Install on emulator, verify avatar + camera FAB + email render. Demo-ready.

### Incremental Delivery

1. US1 → demoable: toolbar + avatar + camera FAB + email.
2. US2 → demoable: settings group with six rows.
3. Polish phase (lint, build, manual checklist, iOS smoke).

### Single-Developer Strategy

Because both stories edit `ProfileScreen.kt`, parallel staffing is not advised. One developer executes T004 → T021 sequentially.

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks.
- [Story] label maps each task to its user story for traceability.
- Manual verification only — no automated tests requested in spec.
- Constitution gates re-evaluated in T021 before merge.
- Commit after each task or logical group (after T009 for US1 checkpoint, after T013 for US2 checkpoint, after T021 for final).
- Avoid: introducing `Color(0xFF…)` literals, raw `"test@gmail.com"` inside a composable, `android.*` / `platform.*` imports in commonMain, `collectAsState()`, manual ViewModel construction, per-row `Card` (use `Surface` + `Column` + `HorizontalDivider` per Q1).
