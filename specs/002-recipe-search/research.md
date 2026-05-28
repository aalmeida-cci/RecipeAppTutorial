# Research: Recipe Search

**Branch**: `002-recipe-search` | **Date**: 2026-05-27

## Decision 1 — Reactive search pipeline

**Decision**: `MutableStateFlow<String>` → `.debounce(300L)` → `.flatMapLatest { … }` → `.stateIn(…)`

**Rationale**: `debounce` batches rapid keystrokes; `flatMapLatest` automatically cancels the coroutine for any superseded query, satisfying FR-008 without any manual `Job` tracking. Both operators are in `kotlinx.coroutines` which is already a project dependency and is fully KMP-compatible in `commonMain`.

**Alternatives considered**:
- Manual `Job` cancel + relaunch — more boilerplate, equivalent semantics, rejected.
- `switchMap` (RxJava) — not in the project stack.
- `distinctUntilChanged` before `debounce` — useful optimisation but not required for correctness; can be added as a follow-up.

---

## Decision 2 — Case-insensitive LIKE query in SQLDelight

**Decision**: Use `lower(column) LIKE ('%' || lower(:query) || '%')` across `title`, `description`, and `ingredients` columns.

**Rationale**: SQLite's default `LIKE` is already case-insensitive for ASCII characters. Wrapping both sides in `lower()` makes the intent explicit and future-safe if the collation changes. Parameterised (`:query`) query prevents SQL injection. No full-text-search (FTS) extension is needed for the expected data volume (< 1000 rows).

**Alternatives considered**:
- SQLite FTS5 — more powerful but requires schema migration and additional SQLDelight setup; overkill for the dataset size, rejected.
- Application-side filtering on `getAllRecipes()` result — avoids new SQL but loads the full table into memory on every keystroke; rejected in favour of a DB-level query.
- `GLOB` operator — case-sensitive by default; rejected.

---

## Decision 3 — No new domain entity for search results

**Decision**: Reuse `RecipeItem` as the result type throughout all search layers.

**Rationale**: `RecipeItem` already contains every field needed for the result row (imageUrl, title, duration, rating) and for navigation (id). Introducing a `SearchResultItem` wrapper would duplicate fields and violate Constitution Principle IV (no duplication). The result row simply reads the subset it needs.

**Alternatives considered**:
- Dedicated `SearchResultItem(id, title, imageUrl, duration, rating)` projection — cleaner display model but adds a mapper with no architectural benefit for this use case; rejected.

---

## Decision 4 — No RemoteDataSource for search

**Decision**: `search/data/datasources/` contains only a `SearchLocalDataSource` (interface + impl). No `SearchRemoteDataSource`.

**Rationale**: The spec (FR-014 and Assumptions) states search operates entirely over the local collection. Adding a remote source would violate the spec scope and introduce complexity without benefit.

**Alternatives considered**:
- Remote fallback if local is empty — out of scope per spec; the Feed feature handles population of the local DB.

---

## Decision 5 — Sealed interface for SearchUiState

**Decision**: `sealed interface SearchUiState` with five variants: `Idle`, `Loading`, `Success`, `Empty`, `Error`.

**Rationale**: The spec (FR-012 + clarifications) requires four distinct, immutable states. `Idle` is the fifth state representing "blank query / initial open" (clarified in Session 2026-05-27). A sealed interface is exhaustively matched by `when`, eliminating impossible state combinations that a nullable-field data class would permit. This is idiomatic Kotlin for finite state machines.

**Alternatives considered**:
- Data class with nullable/boolean fields (pattern used by FeedUiState) — simpler but allows illegal intermediate states (e.g., both `isLoading = true` and `results != null`); rejected for this feature where the states are mutually exclusive.
