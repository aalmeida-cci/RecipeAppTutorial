---

description: "Task list for Language Selection Bottom Sheet feature"
---

# Tasks: Language Selection Bottom Sheet

**Input**: Design documents from `/specs/004-language-selection/`

**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅

**Tests**: Not requested — no test tasks included.

**Organization**: Tasks grouped by user story for independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3)
- Paths relative to `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add DataStore dependency and string resources that all phases depend on.

- [X] T001 Add `datastore = "1.1.7"` version and `datastore-preferences` library alias to `gradle/libs.versions.toml`
- [X] T002 Add `implementation(libs.datastore.preferences)` to `composeApp/build.gradle.kts` commonMain dependencies block
- [X] T003 [P] Add 25 new string keys to `composeApp/src/commonMain/composeResources/values/strings.xml` — keys and English values: `hi_there="Hi there!"`, `got_tasty_dish="Got a tasty dish in mind?"`, `top_recommendations="Top Recommendations"`, `recipes_of_the_week="Recipes Of the Week"`, `search_recipes="Search Recipes"`, `search_hint="Search Recipe Items..."`, `no_results="No results"`, `description="Description"`, `ingredients="Ingredients"`, `instructions="Instructions"`, `watch_video="Watch Video"`, `go_back="Go Back"`, `profile_title="Profile"`, `qr_code_scan="QR Code Scan"`, `qr_code_generation="QR Code Generation"`, `connect_bluetooth="Connect to Bluetooth Device"`, `notification="Notification"`, `language="Language"`, `logout="Logout"`, `select_language="Select Language"`, `english="English"`, `french="French"`, `apply="Apply"`, `favourites="Favourites"`, `error_loading_items="Error in loading Items"`
- [X] T004 [P] Create `composeApp/src/commonMain/composeResources/values-fr/strings.xml` with French translations for all 25 keys: `hi_there="Bonjour!"`, `got_tasty_dish="Un plat savoureux en tête?"`, `top_recommendations="Meilleures recommandations"`, `recipes_of_the_week="Recettes de la semaine"`, `search_recipes="Rechercher des recettes"`, `search_hint="Rechercher des recettes..."`, `no_results="Aucun résultat"`, `description="Description"`, `ingredients="Ingrédients"`, `instructions="Instructions"`, `watch_video="Regarder la vidéo"`, `go_back="Retour"`, `profile_title="Profil"`, `qr_code_scan="Scan QR Code"`, `qr_code_generation="Génération QR Code"`, `connect_bluetooth="Connecter un appareil Bluetooth"`, `notification="Notification"`, `language="Langue"`, `logout="Déconnexion"`, `select_language="Sélectionner la langue"`, `english="Anglais"`, `french="Français"`, `apply="Appliquer"`, `favourites="Favoris"`, `error_loading_items="Erreur lors du chargement"`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure required before any user story can be implemented.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T005 Create `DataStoreManager` in `features/common/data/datastore/DataStoreManager.kt` — generic `save<T>` and `get<T>` methods using `Preferences.Key<T>`; `DataStore<Preferences>` injected via constructor
- [X] T006 Create `composeApp/src/androidMain/kotlin/com/adrian/recipeapp/di/DataStoreModule.android.kt` — Koin module providing `DataStore<Preferences>` via `PreferenceDataStoreFactory.createWithPath` using `applicationContext.filesDir.path + "/app_preferences.preferences_pb"`
- [X] T007 Create `composeApp/src/iosMain/kotlin/com/adrian/recipeapp/di/DataStoreModule.ios.kt` — Koin module providing `DataStore<Preferences>` via `PreferenceDataStoreFactory.createWithPath` using `NSHomeDirectory() + "/app_preferences.preferences_pb"`
- [X] T008 Wire platform DataStore modules into Koin: add `DataStore<Preferences>` and `DataStoreManager` singles to `di/DataModule.kt`; include `DataStoreModule` in `androidMain/MainApplication.kt` (or `Koin.kt`) and `iosMain/KoinIos.kt`
- [X] T009 [P] Create `AppLang` enum in `features/language/domain/entities/AppLang.kt` — fields `code: String`, `displayName: String`; `companion object` with `default = EN` and `fromCode(code: String): AppLang`
- [X] T010 [P] Create `AppStrings` interface in `features/common/localization/AppStrings.kt` — 25 string properties per data-model.md Localization section
- [X] T011 [P] Create `EnStrings` object in `features/common/localization/EnStrings.kt` implementing `AppStrings` with English values
- [X] T012 [P] Create `FrStrings` object in `features/common/localization/FrStrings.kt` implementing `AppStrings` with French values
- [X] T013 Create `LocalAppStrings` in `features/common/localization/LocalAppStrings.kt` — `compositionLocalOf<AppStrings> { EnStrings }` and `val AppLang.strings: AppStrings` extension

**Checkpoint**: Foundation ready — DataStore wired, localization objects exist, `AppLang` defined. User story phases can now begin.

---

## Phase 3: User Story 1 — Select and Apply Language (Priority: P1) 🎯 MVP

**Goal**: User opens Profile, taps Language row, bottom sheet appears with EN/FR options, selects one, taps Apply — sheet closes, language persists, UI switches immediately.

**Independent Test**: Open Profile screen → tap Language row → bottom sheet appears with two options → tap non-active option → tap Apply → verify sheet closes, language saved, visible UI strings reflect new language.

### Implementation for User Story 1

- [X] T014 [US1] Create `LanguageRepository` interface in `features/language/domain/repositories/LanguageRepository.kt` — `getSelectedLang(): Flow<Result<AppLang>>` and `suspend fun saveSelectedLang(lang: AppLang): Result<Unit>`
- [X] T015 [P] [US1] Create `LanguageDataSource` interface in `features/language/data/datasources/LanguageDataSource.kt` — `getLangCode(): Flow<String>` and `suspend fun saveLangCode(code: String)`
- [X] T016 [US1] Create `LanguageDataSourceImpl` in `features/language/data/datasources/LanguageDataSourceImpl.kt` — wraps `DataStoreManager`; key = `stringPreferencesKey("selected_language_code")`; default = `AppLang.default.code`
- [X] T017 [US1] Create `LanguageRepositoryImpl` in `features/language/data/repositories/LanguageRepositoryImpl.kt` — maps `String → AppLang` via `AppLang.fromCode`; wraps results in `Result<T>`; implements `LanguageRepository`
- [X] T018 [US1] Register `LanguageDataSource` and `LanguageRepository` singles in `di/DataModule.kt`
- [X] T019 [US1] Create `LanguageUiState` data class in `features/language/ui/LanguageUiState.kt` — fields: `currentLang`, `pendingLang`, `isBottomSheetVisible`, `isLoading` per data-model.md state transitions
- [X] T020 [US1] Create `LanguageViewModel` in `features/language/ui/LanguageViewModel.kt` — loads persisted lang on init via `viewModelScope`; exposes `StateFlow<LanguageUiState>`; public functions: `onLanguageRowTapped()`, `onLanguageSelected(lang: AppLang)`, `onApply()`, `onDismiss()`; `onApply` persists via repository and sets `currentLang = pendingLang`; `onDismiss` resets `pendingLang = currentLang`
- [X] T021 [US1] Register `LanguageViewModel` in `di/ViewModelModule.kt` via `viewModel { LanguageViewModel(get()) }`
- [X] T022 [US1] Create `LanguageBottomSheet` composable in `features/language/ui/LanguageBottomSheet.kt` — `modifier: Modifier = Modifier` first optional param; `ModalBottomSheet` with radio list of `AppLang.entries`; Apply button calls `onApply`; `onDismissRequest = onDismiss`; signature matches plan.md Key Design Contracts
- [X] T023 [US1] Update `features/profile/ui/ProfileScreen.kt` — add `langState: LanguageUiState` param and `onLanguageRowTap: () -> Unit` param; wire Language settings row tap to `onLanguageRowTap`
- [X] T024 [US1] Update `ProfileRoute` composable in `features/profile/ui/ProfileScreen.kt` — inject `LanguageViewModel` via `koinViewModel()`; collect `langState`; pass to `ProfileScreen`; conditionally show `LanguageBottomSheet` when `langState.isBottomSheetVisible` per plan.md ProfileRoute integration pattern
- [X] T025 [US1] Update `App.kt` — inject `LanguageViewModel` via `koinViewModel()`; collect `langState`; wrap `AppNavHost` in `CompositionLocalProvider(LocalAppStrings provides langState.currentLang.strings)` per plan.md App.kt integration pattern

**Checkpoint**: User Story 1 fully functional — language bottom sheet opens, selection works, Apply switches language.

---

## Phase 4: User Story 2 — Persist Language Across App Restarts (Priority: P2)

**Goal**: Selected language survives app kill/reopen; bottom sheet pre-selects previously saved language; English default on first launch.

**Independent Test**: Select French → apply → kill and reopen app → open Language bottom sheet → verify French is pre-selected.

### Implementation for User Story 2

- [X] T026 [US2] Verify `LanguageViewModel.init` block (T020) reads from `LanguageRepository.getSelectedLang()` and sets `currentLang` and `pendingLang` from persisted value; `isLoading` transitions correctly per data-model.md state transitions — if incomplete, fix init block in `features/language/ui/LanguageViewModel.kt`; these are checkpoint tasks: if Phase 3 implementation is correct, mark complete without changes
- [X] T027 [US2] Verify `LanguageDataSourceImpl` (T016) returns `AppLang.default.code` as default flow value when DataStore is empty — covers first-launch English default (FR-009); if not handled, add default value to `DataStoreManager.get()` call in `features/language/data/datasources/LanguageDataSourceImpl.kt`; these are checkpoint tasks: if Phase 3 implementation is correct, mark complete without changes

**Checkpoint**: User Stories 1 AND 2 fully functional — language persists across restarts, default EN on fresh install.

---

## Phase 5: User Story 3 — Dismiss Without Saving (Priority: P3)

**Goal**: Dismiss bottom sheet without Apply discards pending selection; previously saved language remains active on reopen.

**Independent Test**: Open bottom sheet → change selection → swipe to dismiss → reopen bottom sheet → verify original language still selected.

### Implementation for User Story 3

- [X] T028 [US3] Verify `LanguageViewModel.onDismiss()` (T020) resets `pendingLang = currentLang` and sets `isBottomSheetVisible = false` — no DataStore write occurs on dismiss; if not implemented correctly, fix `onDismiss` in `features/language/ui/LanguageViewModel.kt`; these are checkpoint tasks: if Phase 3 implementation is correct, mark complete without changes
- [X] T029 [US3] Verify `LanguageBottomSheet` (T022) wires `ModalBottomSheet`'s `onDismissRequest` to `onDismiss` callback — swipe-down and outside-tap both trigger dismiss; if missing, add `onDismissRequest = onDismiss` to `ModalBottomSheet` in `features/language/ui/LanguageBottomSheet.kt`; these are checkpoint tasks: if Phase 3 implementation is correct, mark complete without changes

**Checkpoint**: All user stories functional — dismiss correctly discards pending selection.

---

## Phase 6: String Migration

**Purpose**: Replace hardcoded UI strings across 6 screens with `LocalAppStrings.current.*` — enables runtime language switching.

**⚠️ Prerequisite**: Phase 2 (T010–T013) and Phase 3 T025 (App.kt wiring) must be complete.

- [X] T030 [P] Migrate `features/feed/ui/FeedScreen.kt` — replace 4 hardcoded strings: `"Hi there!"` → `LocalAppStrings.current.hiThere`, `"Got a tasty dish in mind?"` → `gotTastyDish`, `"Top Recommendations"` → `topRecommendations`, `"Recipes Of the Week"` → `recipesOfTheWeek`
- [X] T031 [P] Migrate `features/search/ui/SearchScreen.kt` — replace 3 hardcoded strings: `"Search Recipes"` → `searchRecipes`, `"Search Recipe Items..."` → `searchHint`, `"No results"` → `noResults`
- [X] T032 [P] Migrate `features/detail/ui/DetailScreen.kt` — replace 5 hardcoded strings: `"Description"` → `description`, `"Ingredients"` → `ingredients`, `"Instructions"` → `instructions`, `"Watch Video"` → `watchVideo`, `"Go Back"` → `goBack`
- [X] T033 [P] Migrate `features/favourites/ui/FavouriteScreen.kt` — replace 1 hardcoded string: `"Favourites"` → `favourites`
- [X] T034 [P] Migrate `features/profile/ui/ProfileScreen.kt` — replace 8 hardcoded strings: `"Profile"` → `profileTitle`, `"QR Code Scan"` → `qrCodeScan`, `"QR Code Generation"` → `qrCodeGeneration`, `"Connect to Bluetooth Device"` → `connectBluetooth`, `"Notification"` → `notification`, `"Language"` → `language`, `"Logout"` → `logout`
- [X] T035 [P] Migrate `features/common/ui/components/ErrorContent.kt` (confirmed exists) — replace `"Error in loading Items"` → `LocalAppStrings.current.errorLoadingItems`

---

## Phase 7: Polish & Cross-Cutting Concerns

- [X] T036 Run `./gradlew spotlessApply` — fix any ktlint formatting issues introduced by new files
- [X] T037 Run `./gradlew :composeApp:assembleDebug` — verify clean build on Android; fix any compilation errors
- [X] T038 Verify iOS build compiles via `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` — confirm no missing `actual` declarations or iOS-specific issues

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately; T003 and T004 are independent [P]
- **Phase 2 (Foundational)**: Depends on Phase 1 (T001, T002 must complete for DataStore); T009–T012 are independent [P] after T001/T002
- **Phase 3 (US1)**: Depends on Phase 2 fully complete
- **Phase 4 (US2)**: Depends on Phase 3 (verifies T020 init block)
- **Phase 5 (US3)**: Depends on Phase 3 (verifies T020 dismiss + T022 sheet)
- **Phase 6 (String Migration)**: Depends on Phase 2 T010–T013 and Phase 3 T025
- **Phase 7 (Polish)**: Depends on all phases complete

### User Story Dependencies

- **US1 (P1)**: Starts after Phase 2 — no dependency on US2 or US3
- **US2 (P2)**: Verifies US1 init behavior — review after US1 complete
- **US3 (P3)**: Verifies US1 dismiss behavior — review after US1 complete

### Parallel Opportunities

- T003, T004 in Phase 1 run in parallel
- T009, T010, T011, T012 in Phase 2 run in parallel (after T001/T002 complete)
- T015 in Phase 3 runs in parallel with T014
- T030–T035 in Phase 6 all run in parallel

---

## Parallel Example: Phase 2

```
# After T001/T002 complete, launch simultaneously:
Task T009: Create AppLang enum
Task T010: Create AppStrings interface
Task T011: Create EnStrings object
Task T012: Create FrStrings object
```

## Parallel Example: Phase 6 (String Migration)

```
# After Phase 3 T025 complete, launch simultaneously:
Task T030: Migrate FeedScreen.kt
Task T031: Migrate SearchScreen.kt
Task T032: Migrate DetailScreen.kt
Task T033: Migrate FavouriteScreen.kt
Task T034: Migrate ProfileScreen.kt
Task T035: Migrate ErrorContent.kt
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T004)
2. Complete Phase 2: Foundational (T005–T013) — CRITICAL
3. Complete Phase 3: User Story 1 (T014–T025)
4. **STOP and VALIDATE**: Language bottom sheet opens, Apply switches language, UI reflects change
5. Demo if ready

### Incremental Delivery

1. Phase 1 + Phase 2 → Foundation ready
2. Phase 3 (US1) → Core feature working — demo/validate
3. Phase 4 (US2) → Persistence verified — validate cold start
4. Phase 5 (US3) → Dismiss behavior correct — validate cancel flow
5. Phase 6 (String Migration) → All strings translatable — validate FR strings display correctly
6. Phase 7 (Polish) → Clean build, formatted code

---

## Notes

- [P] tasks = different files, no blocking dependencies between them
- [USx] label maps task to specific user story for traceability
- US2 and US3 are primarily verification tasks — most logic implemented in Phase 3 (US1)
- String migration (Phase 6) independent of US2/US3 — can run after US1 validates
- `ProfileScreen.kt` appears in both US1 (T023/T024) and Phase 6 (T034) — complete T023/T024 before T034 to avoid conflicts
- No `navigation/` package for `language/` feature — bottom sheet shown via UI state, not nav route (per research.md Decision 3)
- DataStore KMP path injection via Koin avoids `expect/actual` (per research.md Decision 2)
