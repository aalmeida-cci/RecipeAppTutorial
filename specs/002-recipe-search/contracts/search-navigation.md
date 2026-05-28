# Contract: Search Screen Navigation

**Branch**: `002-recipe-search` | **Date**: 2026-05-27

## Updated NavGraphBuilder Extension

The existing `searchNavGraph()` function (no parameters) is replaced with:

```
searchNavGraph(
    navigateBack: () -> Unit,
    navigateToDetail: (Long) -> Unit,
)
```

### Caller (AppNavHost.kt)

```
searchNavGraph(
    navigateBack     = appState::navigateBack,
    navigateToDetail = appState::navigateToDetail,
)
```

`appState::navigateBack` and `appState::navigateToDetail` already exist on `AppState`. No new navigation functions are needed.

### Callee (SearchRoute)

```
SearchRoute(
    navigateBack:     () -> Unit,
    navigateToDetail: (Long) -> Unit,
    viewModel:        SearchViewModel = koinViewModel(),
)
```

`navigateBack` — invoked when the user taps the back arrow in the top app bar.
`navigateToDetail(id: Long)` — invoked when the user taps a recipe result row; `id` is `RecipeItem.id`.

### Invariants

- `navigateBack` and `navigateToDetail` are **never stored** in the ViewModel. They are passed directly to the `Screen` composable.
- The `SearchViewModel` has no reference to `NavController` or any navigation type.
- Back-stack state (query + results) is preserved on back-navigation because the `SearchViewModel` remains alive while the composable is on the back stack.
