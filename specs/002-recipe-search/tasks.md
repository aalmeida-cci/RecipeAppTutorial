# Tasks: Recipe Search

**Input**: Design documents from `specs/002-recipe-search/`

**Prerequisites**: plan.md ✅ · spec.md ✅ · research.md ✅ · data-model.md ✅ · contracts/ ✅

**Tests**: Not requested — no test tasks generated.

**Organization**: Tasks grouped by user story (US1 P1 → US2 P1 → US3 P2) to enable independent incremental delivery.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on incomplete tasks in the same phase)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)

---

## Phase 1: Setup (Directory Structure)

**Purpose**: Create the `search` feature package layout so all subsequent tasks have target directories.

- [X] T001 Create feature directory tree: `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/search/domain/repositories/`, `data/datasources/`, `data/repositories/`, `ui/` (navigation/ already exists). Note: `data/models/` is intentionally omitted — search is local-only, no DTO required.

**Checkpoint**: All target directories exist.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add the SQLDelight query and `RecipeDao` method that every upper layer depends on. Nothing in Phase 3+ can compile without these.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T002 Add `searchRecipes` parameterised LIKE query to `composeApp/src/commonMain/sqldelight/com/adrian/recipeapp/db/RecipeEntity.sq` — match `lower(title)`, `lower(description)`, `lower(ingredients)` against `'%' || lower(:query) || '%'`
- [X] T003 Add `suspend fun searchRecipes(query: String): List<RecipeItem>` to `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/common/data/database/daos/RecipeDao.kt` — call `recipeEntityQueries.searchRecipes(query).awaitAsList()` then map each row with `recipeEntityMapper()`

**Checkpoint**: Project compiles; `RecipeDao.searchRecipes()` is callable from any data source.

---

## Phase 3: User Story 1 — Find Recipes by Keyword (Priority: P1) 🎯 MVP

**Goal**: User opens the Search screen, types a keyword, and sees matching recipes displayed in a scrollable list with image, title, duration, and rating. Results update as the user types (300 ms debounce); clearing the field shows an empty list.

**Independent Test**: Launch the app → tap Feed search bar → type "bread" → confirm recipes whose title/description/ingredients contain "bread" appear in the list. Clear the field → confirm the list empties.

### Implementation for User Story 1

- [X] T004 [P] [US1] Create `SearchUiState` sealed interface in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/search/ui/SearchUiState.kt` with five variants: `Idle`, `Loading`, `Success(recipes: List<RecipeItem>)`, `Empty`, `Error(throwable: Throwable)`
- [X] T005 [P] [US1] Create `SearchLocalDataSource` interface in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/search/data/datasources/SearchLocalDataSource.kt` declaring `suspend fun searchRecipes(query: String): List<RecipeItem>`
- [X] T006 [US1] Create `SearchLocalDataSourceImpl` in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/search/data/datasources/SearchLocalDataSourceImpl.kt` — constructor receives `RecipeDao`, delegates to `recipeDao.searchRecipes(query)`
- [X] T007 [P] [US1] Create `SearchRepository` interface in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/search/domain/repositories/SearchRepository.kt` declaring `suspend fun searchRecipes(query: String): Result<List<RecipeItem>>`
- [X] T008 [US1] Create `SearchRepositoryImpl` in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/search/data/repositories/SearchRepositoryImpl.kt` — constructor receives `SearchLocalDataSource`; wraps `localDataSource.searchRecipes(query)` in `try/catch` returning `Result.success` or `Result.failure` (T005, T006, T007 must be done)
- [X] T009 [US1] Create `SearchViewModel` in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/search/ui/SearchViewModel.kt` — `MutableStateFlow<String>("")` for `_query`; `searchUiState: StateFlow<SearchUiState>` derived via `.debounce(300L).flatMapLatest { q -> if (q.isBlank()) flowOf(Idle) else flow { emit(Loading); repo.searchRecipes(q).fold(...) } }.stateIn(viewModelScope, WhileSubscribed(5_000), Idle)`; expose `fun onQueryChange(newQuery: String)` (T004, T008 must be done)
- [X] T010 [US1] Implement `SearchScreen.kt` in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/search/ui/SearchScreen.kt` replacing the empty stub — `SearchRoute` collects `query` and `searchUiState` via `collectAsStateWithLifecycle()`; stateless `SearchScreen` receives both plus `onQueryChange`, `navigateBack` (no-op placeholder for now), `navigateToDetail` (no-op placeholder for now); `Scaffold` with `SearchTopBar` (back arrow + title "Search Recipes") and content area; `SearchInputField` composable (rounded border, search icon, placeholder "Search Recipe Items...", `TextField` bound to `query`/`onQueryChange`); `when(uiState)` branches: `Idle` → nothing rendered, `Loading` → reuse existing `Loader()`, `Success` → `LazyColumn` of `SearchRecipeRow` composables (AsyncImage 80dp×80dp rounded + title with `maxLines = 1` and `overflow = TextOverflow.Ellipsis` + Row of duration icon/text + star icon/rating), `Empty` → simple "No results" text, `Error` → reuse existing `ErrorContent()` (T004, T009 must be done)
- [X] T011 [US1] Register `SearchLocalDataSource`, `SearchRepository` as `single<>` bindings in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/di/DataModule.kt` and register `SearchViewModel` with `viewModel { SearchViewModel(get()) }` in `composeApp/src/commonMain/kotlin/com/adrian/recipeapp/di/ViewModelModule.kt` (T006, T008, T009 must be done)

**Checkpoint**: US1 independently functional — app builds, search screen shows matching recipes when user types.

---

## Phase 4: User Story 2 — Open a Recipe from Results (Priority: P1)

**Goal**: Tapping a result row navigates to the existing Recipe Details screen for that recipe. Back-navigation from detail returns to the search screen with prior query and results intact.

**Independent Test**: With recipes shown in the result list, tap any row → confirm the Recipe Details screen for the correct recipe opens. Tap back → confirm the search screen is restored with the same query and results.

### Implementation for User Story 2

- [X] T012 [US2] Update `SearchScreen.kt` (`composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/search/ui/SearchScreen.kt`) — replace the `navigateToDetail` no-op: pass the real lambda down through `SearchScreen` and `SearchRoute`; wire `SearchRecipeRow` `clickable { navigateToDetail(recipe.id) }`
- [X] T013 [US2] Update `SearchNavGraph.kt` (`composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/search/navigation/SearchNavGraph.kt`) — add `navigateToDetail: (Long) -> Unit` parameter to `searchNavGraph()`; pass it into `SearchRoute(navigateToDetail = navigateToDetail)` (T012 must be done)
- [X] T014 [US2] Update `AppNavHost.kt` (`composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/app/navigation/AppNavHost.kt`) — update `searchNavGraph()` call to pass `navigateToDetail = appState::navigateToDetail` (T013 must be done)

**Checkpoint**: US2 independently functional — tapping a result opens the correct detail screen; back returns to search with state preserved.

---

## Phase 5: User Story 3 — Navigate To and From the Search Screen (Priority: P2)

**Goal**: The back arrow in the Search Recipes top app bar returns the user to the Feed screen (or whichever screen opened search).

**Independent Test**: Tap the Feed screen's search bar → Search screen opens. Tap the back arrow → Feed screen is restored.

### Implementation for User Story 3

- [X] T015 [US3] Update `SearchNavGraph.kt` (`composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/search/navigation/SearchNavGraph.kt`) — add `navigateBack: () -> Unit` parameter; pass it into `SearchRoute(navigateBack = navigateBack)` replacing the no-op placeholder; update `NavController.navigateToSearch()` extension if needed
- [X] T016 [US3] Update `SearchScreen.kt` (`composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/search/ui/SearchScreen.kt`) — wire the `SearchTopBar` back arrow `onClick` to call the real `navigateBack` lambda (T015 must be done)
- [X] T017 [US3] Update `AppNavHost.kt` (`composeApp/src/commonMain/kotlin/com/adrian/recipeapp/features/app/navigation/AppNavHost.kt`) — update `searchNavGraph()` call to also pass `navigateBack = appState::navigateBack` (T015 must be done)

**Checkpoint**: All three user stories functional — full search-to-detail and back-navigation flow works end-to-end.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Visual polish, edge case hardening, and code review gate verification.

- [X] T018 [P] Verify `Modifier` is the first optional parameter in every new composable in `SearchScreen.kt` (constitution code review gate)
- [X] T019 [P] Confirm no `collectAsState()` calls remain in `SearchScreen.kt` — only `collectAsStateWithLifecycle()` used
- [X] T020 [P] Confirm no hardcoded `Color(0xFF…)` values in `SearchScreen.kt` — all colours via `MaterialTheme.colorScheme.*`
- [X] T021 [P] Confirm no `android.*` / `platform.*` imports anywhere in the new `search/` package
- [X] T022 Add bottom padding to the `LazyColumn` in `SearchScreen.kt` (`contentPadding = PaddingValues(bottom = 16.dp)`) to prevent last item being clipped by system bars
- [X] T023 Verify `SearchViewModel` is not constructed manually anywhere — only via `koinViewModel()` in `SearchRoute`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately.
- **Phase 2 (Foundational)**: Depends on Phase 1. **Blocks all user story phases.**
- **Phase 3 (US1)**: Depends on Phase 2. T004–T007 are independent of each other [P]. T008 depends on T005/T006/T007. T009 depends on T004/T008. T010 depends on T004/T009. T011 depends on T006/T008/T009.
- **Phase 4 (US2)**: Depends on Phase 3 completion. T012 → T013 → T014 must be sequential.
- **Phase 5 (US3)**: Depends on Phase 4 completion (not just Phase 3). T015 modifies `SearchNavGraph.kt` and T017 modifies `AppNavHost.kt` — the same files touched by T013 and T014 in Phase 4. Phase 5 MUST run after Phase 4 to avoid file conflicts. T015 → T016 → T017 must be sequential.
- **Phase 6 (Polish)**: Depends on Phases 4 and 5.

### User Story Dependencies

- **US1 (P1)**: Depends on Foundational only. Independent.
- **US2 (P1)**: Depends on US1 complete (needs working result list to wire tap navigation).
- **US3 (P2)**: Depends on US2 complete. T015 and T017 modify `SearchNavGraph.kt` and `AppNavHost.kt` respectively — the same files updated by T013 and T014 in US2. US3 MUST run after US2 to avoid conflicts.

---

## Parallel Opportunities

```
# Phase 2 — sequential (T002 must precede T003)

# Phase 3 — launch these four in parallel immediately after T003:
T004  SearchUiState.kt
T005  SearchLocalDataSource interface
T006  SearchLocalDataSourceImpl
T007  SearchRepository interface

# Then sequentially:
T008  SearchRepositoryImpl   (needs T005, T006, T007)
T009  SearchViewModel        (needs T004, T008)
T010  SearchScreen.kt        (needs T004, T009)
T011  DI registration        (needs T006, T008, T009)

# Phase 4 then Phase 5 — SEQUENTIAL (both touch SearchNavGraph.kt and AppNavHost.kt):
T012 → T013 → T014   (US2 — detail navigation)
T015 → T016 → T017   (US3 — back navigation, starts after T014)

# Phase 6 — T018, T019, T020, T021, T023 are all [P]
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL — blocks everything)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: type a query in the search screen and confirm matching recipes appear
5. Proceed to Phase 4 + 5

### Incremental Delivery

1. Phase 1 + 2 → foundation compiles
2. Phase 3 → search and display works (MVP)
3. Phase 4 → result rows navigate to detail
4. Phase 5 → back arrow navigates back to Feed
5. Phase 6 → polish passes all constitution gates

---

## Notes

- [P] tasks operate on different files with no incomplete-task dependencies — safe to parallelise.
- No test tasks generated (not requested in spec).
- `SearchNavGraph.kt` is updated incrementally across US2 (T013) and US3 (T015); ensure both changes are applied before testing the full flow.
- The existing `searchNavGraph()` call in `AppNavHost.kt` compiles without parameters today; it will fail to compile after T013/T015 add required parameters — apply T014/T017 immediately after the respective NavGraph updates.
- DI registration (T011) must be done before running the app to test US1, even though it is listed last in Phase 3.
