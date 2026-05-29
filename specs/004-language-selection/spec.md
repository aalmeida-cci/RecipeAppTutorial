# Feature Specification: Language Selection Bottom Sheet

**Feature Branch**: `004-language-selection`

**Created**: 2026-05-28

**Status**: Draft

**Input**: User description: "Change language UI and implementation. The bottom sheet should allow users to change the application language and currently support two language options: English and French, the bottom sheet would be shown on click in ProfileScreen.kt for languages item on click. Display the languages in a selectable list where each item contains the language name along with a checkbox or radio-style selection indicator, ensuring that only one language can be selected at a time and one option is always selected by default. Add a confirmation button at the bottom labeled "Confirm" or "Apply" to save the selected language. Use a modern Material Design UI with proper spacing, rounded corners, clean typography, and responsive alignment. Implement a dedicated UiState data class to manage the selected language state and a ViewModel to handle language selection, UI updates, persistence, and restoring the previously selected language when the app is reopened."

## Clarifications

### Session 2026-05-28

- Q: After tapping Apply in v1, what does the user observe — selection saved only, or immediate UI string language switch? → A: Apply triggers immediate UI language switch; radio tap alone does NOT switch language, only Apply does.
- Q: ViewModel architecture — separate LanguageViewModel or extend ProfileViewModel? → A: Separate LanguageViewModel — owns LanguageUiState, language persistence, and locale switching independently.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Select and Apply Language (Priority: P1)

A user opens the Profile screen, taps the "Language" settings row, and a bottom sheet slides up showing two language options — English and French — with a radio-style indicator showing the currently active language. The user selects their preferred language and taps "Apply". The bottom sheet closes, the language is persisted, and the app UI switches to the selected language immediately.

**Why this priority**: Core feature requirement. Without the ability to select and persist a language, nothing else in this feature is testable.

**Independent Test**: Open Profile screen → tap Language row → bottom sheet appears with two options → tap the non-active option → tap Apply → verify sheet closes, language is saved, and visible UI strings reflect the new language.

**Acceptance Scenarios**:

1. **Given** the app is open on the Profile screen, **When** the user taps the "Language" row, **Then** a bottom sheet appears displaying "English" and "French" as selectable options.
2. **Given** the bottom sheet is open, **When** the user taps a language option, **Then** that option is marked as selected and the previous selection is deselected (only one can be selected at a time) — the UI does NOT switch language yet.
3. **Given** a language is selected in the bottom sheet, **When** the user taps "Apply", **Then** the bottom sheet dismisses, the selected language is persisted, and the app UI immediately reflects the new language.
4. **Given** the bottom sheet is open, **When** no change is made, **Then** the currently active language remains selected by default.

---

### User Story 2 - Persist Language Across App Restarts (Priority: P2)

A user selects French as their preferred language and applies the change. After closing and reopening the app, the Profile screen's language setting still shows French as selected, and the bottom sheet pre-selects French when opened again.

**Why this priority**: Without persistence, language selection is stateless and useless beyond a single session.

**Independent Test**: Select French → apply → kill and reopen app → open Language bottom sheet → verify French is pre-selected.

**Acceptance Scenarios**:

1. **Given** the user previously selected French and applied, **When** the app is relaunched, **Then** French is still the active language and is pre-selected in the bottom sheet.
2. **Given** no language has been previously saved, **When** the bottom sheet opens for the first time, **Then** English is selected by default.

---

### User Story 3 - Dismiss Without Saving (Priority: P3)

A user opens the language bottom sheet, changes the selection, then dismisses the sheet (swipe down or tap outside) without tapping Apply. The previously saved language remains unchanged.

**Why this priority**: Expected user experience — changes should only commit on explicit confirmation.

**Independent Test**: Open bottom sheet → change selection → swipe to dismiss → reopen bottom sheet → verify original language still selected.

**Acceptance Scenarios**:

1. **Given** the bottom sheet is open with a new selection pending, **When** the user swipes the sheet down or taps outside it, **Then** the sheet closes and the previously persisted language remains active.
2. **Given** the bottom sheet is dismissed without applying, **When** the bottom sheet is reopened, **Then** the last saved language is pre-selected, not the dismissed pending selection.

---

### Edge Cases

- What happens when no language preference has been persisted yet? → English is selected by default.
- What happens if the user rapidly taps Apply multiple times? → Only one save operation occurs; the sheet closes normally.
- How does the system handle the case where only one language exists? → Both options are always shown (English and French are hardcoded for v1); this edge case does not apply.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Profile screen's "Language" settings row MUST open a bottom sheet when tapped.
- **FR-002**: The bottom sheet MUST display exactly two language options: English and French.
- **FR-003**: Each language option MUST show the language name and a radio-style selection indicator.
- **FR-004**: Only one language option MUST be selectable at a time (mutual exclusion).
- **FR-005**: One language option MUST always be selected — the selection cannot be cleared.
- **FR-006**: The bottom sheet MUST include an "Apply" confirmation button at the bottom.
- **FR-007**: Tapping "Apply" MUST persist the selected language, close the bottom sheet, and immediately switch the app UI to display content in the selected language — the language switch does NOT occur on radio tap, only on Apply.
- **FR-008**: The previously persisted language MUST be pre-selected when the bottom sheet is opened.
- **FR-009**: If no language has been saved, English MUST be selected by default.
- **FR-010**: Dismissing the bottom sheet without tapping "Apply" MUST discard any pending selection change.
- **FR-011**: A dedicated `LanguageUiState` data class MUST manage selected language state.
- **FR-012**: A dedicated, separate `LanguageViewModel` MUST handle language selection logic, persistence, locale switching, and state restoration — it MUST NOT be co-located inside `ProfileViewModel`.
- **FR-013**: The bottom sheet UI MUST follow Material Design guidelines with proper spacing, rounded corners, and clean typography.

### Key Entities

- **Language**: A supported application language. Attributes: identifier (e.g., `"en"`, `"fr"`), display name (e.g., `"English"`, `"French"`). Fixed set of two for v1.
- **LanguagePreference**: The persisted user choice. Stores the selected language identifier across app sessions.
- **LanguageUiState**: Transient UI state. Tracks the currently selected language in the bottom sheet and whether the sheet is visible.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can open the language bottom sheet, select a language, tap Apply, and see the UI reflect the new language — all within 10 seconds.
- **SC-002**: The previously selected language is restored correctly 100% of the time when the app is reopened.
- **SC-003**: At no point can the bottom sheet display a state where zero languages are selected.
- **SC-004**: Dismissing the bottom sheet without applying leaves the persisted language unchanged in 100% of cases.

---

## Assumptions

- Runtime locale switching IS in scope for v1: tapping Apply triggers an immediate UI language switch. Tapping a radio option alone does NOT switch the language — the switch occurs only on Apply confirmation.
- English is the default language when the app is first installed with no prior preference saved.
- The two supported languages (English, French) are hardcoded for v1; no dynamic language list loading is required.
- Language preference is stored locally on the device using `androidx.datastore:datastore-preferences-core:1.1.7` (KMP) via a generic `DataStoreManager`. Multiplatform Settings (russhwolf) is NOT used for this feature — DataStore was chosen per research.md Decision 2.
- The Profile screen's ViewModel and UiState already exist; language selection is implemented as a separate `LanguageViewModel` — not co-located in `ProfileViewModel`. The Profile screen wires both ViewModels independently.
- Bottom sheet dismissal via swipe-down or outside-tap is treated as a cancel action.
