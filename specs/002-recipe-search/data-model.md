# Data Model: Recipe Search

**Branch**: `002-recipe-search` | **Date**: 2026-05-27

## Reused Entity: RecipeItem

No new domain entity is introduced. `RecipeItem` (already in `features/common/domain/entities/RecipeItem.kt`) is the result type for all search layers.

| Field | Type | Used in search row |
|---|---|---|
| `id` | `Long` | Navigation (detail screen) |
| `title` | `String` | Display + LIKE match |
| `description` | `String` | LIKE match only |
| `imageUrl` | `String` | Thumbnail in row |
| `duration` | `String` | Display |
| `rating` | `Long` | Display |
| `ingredients` | `List<String>` | LIKE match (serialised) |
| `category`, `area`, `youtubeLink`, `instructions`, `isFavorite`, `difficulty` | various | Carried through unchanged |

## New SQLDelight Query

**File**: `composeApp/src/commonMain/sqldelight/com/adrian/recipeapp/db/RecipeEntity.sq`

```sql
searchRecipes:
SELECT * FROM Recipe
WHERE lower(title)       LIKE ('%' || lower(:query) || '%')
   OR lower(description) LIKE ('%' || lower(:query) || '%')
   OR lower(ingredients) LIKE ('%' || lower(:query) || '%');
```

- `:query` is the debounced, trimmed user input.
- `lower()` provides case-insensitive ASCII matching.
- `ingredients` is stored as a comma-joined string by `ListOfStringsAdapter`; a substring match against this form correctly finds recipes that contain the ingredient.
- Generated method: `recipeEntityQueries.searchRecipes(query: String): Query<Recipe>`
- Consumed via `awaitAsList()` (async SQLDelight) then mapped with `recipeEntityMapper()`.

## UI State Model

```
SearchUiState (sealed interface)
├── Idle                            — blank/cleared query: show empty list
├── Loading                         — query debounced, DB call in progress
├── Success(recipes: List<RecipeItem>)  — ≥1 match found
├── Empty                           — 0 matches for this query
└── Error(throwable: Throwable)     — DB exception wrapped by repository
```

State transitions:

```
[screen opens] → Idle
Idle ──[user types]──────────────── Loading → Success | Empty | Error
Success | Empty | Error ──[user edits]─── Loading → …
Any ──[user clears field]──────────── Idle
```
