# Feature Specification: Recipe Search

**Feature Branch**: `002-recipe-search`

**Created**: 2026-05-27

**Status**: Draft

**Input**: User description: "Implement a Recipe Search feature where users can search recipes dynamically using a search field. The search must match recipes based on title, description, or ingredients using partial and case-insensitive matching. Reactive real-time search with debounce. Search screen with top app bar (back button + title 'Search Recipes'), a rounded search bar with search icon and placeholder 'Search Recipe Items...', and a list of matching recipe items showing image, title, cooking time, and rating. Clicking a recipe item navigates to the existing Recipe Details screen. Reached from the Feed screen search bar; back arrow returns to the previous screen."

## Clarifications

### Session 2026-05-27

- Q: What should the search screen show when the search field is empty (on first open or after clearing)? → A: The list is always empty when the field is empty — no recipes are shown on open, and clearing the field hides all results. No search runs until the user types.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Find recipes by typing a keyword (Priority: P1)

A user opens the Search Recipes screen from the Feed screen and types a keyword (for example, "bread") into the search field. As they type, the screen shows the recipes whose title, description, or ingredients contain that keyword, regardless of letter casing. Typing "bread" surfaces results like "Bread and Butter Pudding" and "Bread omelette".

**Why this priority**: This is the core value of the feature — without keyword matching against the recipe collection, there is no search. It delivers a usable, demonstrable product on its own.

**Independent Test**: Open the search screen, type a known keyword, and confirm the matching recipes appear in the result list. Fully testable in isolation and delivers immediate value.

**Acceptance Scenarios**:

1. **Given** the search screen is open with an empty field, **When** the user types "bread", **Then** the list shows every recipe whose title, description, or ingredients contain "bread" (case-insensitive, partial match).
2. **Given** the user has typed "bread", **When** the user continues typing to "breakfast", **Then** the list updates to show only recipes matching "breakfast" and the earlier "bread" results that no longer match are removed.
3. **Given** the user has typed a query, **When** the user clears the search field, **Then** the result list returns to its empty/initial state.
4. **Given** the user types upper- or mixed-case text such as "BrEaD", **When** the search runs, **Then** the same results appear as for lowercase "bread".

---

### User Story 2 - Open a recipe from the results (Priority: P1)

After finding a recipe in the search results, the user taps on a result row and is taken to the existing Recipe Details screen for that recipe.

**Why this priority**: Search has little value if the user cannot act on a result. Combined with Story 1, this completes the primary search-to-detail journey.

**Independent Test**: With at least one result shown, tap a result and confirm the correct recipe detail screen opens.

**Acceptance Scenarios**:

1. **Given** search results are displayed, **When** the user taps a result row, **Then** the Recipe Details screen for that specific recipe opens.
2. **Given** the user is on the Recipe Details screen reached from search, **When** the user navigates back, **Then** the user returns to the search screen with their previous query and results intact.

---

### User Story 3 - Navigate to and from the search screen (Priority: P2)

A user reaches the search screen by tapping the search bar on the Feed screen, and leaves it by tapping the back arrow in the top app bar, returning to the screen they came from.

**Why this priority**: Entry and exit make the feature reachable and dismissible, but the search experience itself (Stories 1 and 2) is the differentiating value.

**Independent Test**: Tap the Feed search bar to open the screen, then tap the back arrow and confirm return to the Feed screen.

**Acceptance Scenarios**:

1. **Given** the user is on the Feed screen, **When** the user taps the search bar, **Then** the Search Recipes screen opens.
2. **Given** the user is on the Search Recipes screen, **When** the user taps the back arrow in the top app bar, **Then** the previous screen is restored.

---

### Edge Cases

- **Initial state (no query entered)**: When the screen first opens, the result list is empty and no recipes are displayed. No search runs until the user begins typing.
- **No matches**: When a query matches no recipes, the screen shows a clear empty-results state rather than a blank or frozen view.
- **Empty / whitespace-only query or cleared field**: Whenever the search field is empty (on first open or after the user clears their text), the result list is empty and no recipes are shown. No search runs and no error is shown.
- **Rapid typing**: When the user types quickly, only the result for the latest query is shown; intermediate queries do not flash stale results or cause flicker.
- **Search in progress**: While results for the current query are being prepared, the user sees a loading indicator instead of an empty list.
- **Data retrieval failure**: If results cannot be retrieved, the user sees a clear error state and can retry by editing the query.
- **Long recipe titles**: Titles that exceed the available width are truncated gracefully without breaking the row layout.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a dedicated Search Recipes screen reachable from the Feed screen's search bar.
- **FR-002**: The search screen MUST display a top app bar containing a back navigation control and the title "Search Recipes".
- **FR-003**: The search screen MUST display a rounded search input with a search icon and the placeholder text "Search Recipe Items...".
- **FR-004**: System MUST match the user's query against recipe title, description, and ingredients.
- **FR-005**: Matching MUST be case-insensitive and partial (a query is a substring match, not an exact-word match).
- **FR-006**: System MUST update results in real time as the user types, without requiring a submit action.
- **FR-007**: System MUST debounce input (approximately 300ms) so results update only after the user briefly pauses typing.
- **FR-008**: System MUST ensure that when a new query supersedes an earlier one, only results for the most recent query are presented (earlier in-flight searches are abandoned).
- **FR-009**: Each result row MUST show the recipe image, title, duration (cooking time), and rating.
- **FR-010**: Tapping a result row MUST navigate to the existing Recipe Details screen for that recipe.
- **FR-011**: Tapping the back control MUST return the user to the previous screen.
- **FR-012**: System MUST present distinct, immutable states for loading, results-available, no-results, and error conditions.
- **FR-013**: When the query is empty or whitespace-only (including on first open and after the user clears a previous query), System MUST show an empty list with no recipe items and MUST NOT run a search.
- **FR-014**: Search MUST operate over the recipes already stored in the app's local recipe collection.

### Key Entities *(include if feature involves data)*

- **Recipe**: A dish record in the app's local collection. Relevant attributes for search and display include title, description, ingredients (searchable text), image, duration (cooking time), rating, and a unique identifier used to open its detail screen.
- **Search Query**: The free-text keyword entered by the user; drives which recipes are matched.
- **Search Result Item**: The subset of a Recipe shown in a result row — image, title, duration (cooking time), rating, and identifier for navigation.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can go from the Feed screen to viewing relevant search results in under 5 seconds for a typical query.
- **SC-002**: Typing a keyword returns all recipes containing that keyword in title, description, or ingredients, with 100% of expected matches present and no non-matching recipes shown.
- **SC-003**: Results refresh within roughly 300ms of the user pausing typing, and rapid typing never leaves stale results from a superseded query displayed.
- **SC-004**: 95% of users can locate a known recipe and open its detail screen on their first attempt without external guidance.
- **SC-005**: Every query outcome (loading, results, no results, error) presents a clear, recognizable state to the user with no blank or frozen screens.

## Assumptions

- The recipe collection is already populated locally before the user searches; this feature does not fetch or refresh recipe data from the network.
- "Cooking time" maps to the recipe's existing duration value and "rating" to its existing rating value as already shown elsewhere in the app.
- The Recipe Details screen and its navigation entry point already exist and are reused as-is; this feature only adds navigation into it from search results.
- Search covers the full local recipe collection; pagination or infinite scroll is out of scope for this version.
- Search history, suggestions, filters, and sorting are out of scope for this version.
- Returning from a recipe detail to the search screen preserves the prior query and results (standard back-stack behavior).
- The debounce interval of ~300ms is a reasonable default balancing responsiveness and efficiency; an exact value can be tuned without changing scope.
