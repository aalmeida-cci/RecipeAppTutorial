# Data Model: Profile Screen UI

**Feature**: 003-profile-screen
**Date**: 2026-05-28

This feature is UI-only and introduces no domain entities, DTOs, or persistence schemas. The only data structure is the UI state class consumed by the Profile screen composable.

---

## ProfileUiState

**Layer**: UI (`features/profile/ui/ProfileUiState.kt`)
**Package**: `com.adrian.recipeapp.features.profile.ui`
**Visibility**: `internal` to the feature is acceptable; `public` is fine since other features will not reference it.

### Definition

```kotlin
data class ProfileUiState(
    val email: String
)
```

### Fields

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `email` | `String` | Yes | (none — must be supplied by ViewModel) | The email shown beneath the avatar. In v1 this is the hardcoded literal `"test@gmail.com"` initialized inside `ProfileViewModel`. |

### Validation rules

- `email` is treated as opaque display text in v1 — no format validation, no length cap, no scrubbing. The UI must truncate visually with ellipsis on overflow (FR / Edge Case), not validate or reject any value.

### State transitions

None. `ProfileUiState` is immutable; the ViewModel emits a new instance whenever the email changes. In v1 the value never changes after initialization.

### Relationships

- `ProfileViewModel` produces `StateFlow<ProfileUiState>`.
- `ProfileRoute` collects via `collectAsStateWithLifecycle()` and passes the value into `ProfileScreen(uiState, ...)`.

### Out of scope

- No domain entity `Profile` / `User` is introduced — the email is a UI placeholder until real authentication lands (tracked as a follow-up obligation in `plan.md` → Complexity Tracking).
- No persistence, no serialization, no `@Serializable` DTO.
