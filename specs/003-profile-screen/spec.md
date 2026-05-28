# Feature Specification: Profile Screen UI

**Feature Branch**: `003-profile-screen`

**Created**: 2026-05-28

**Status**: Draft

## Clarifications

### Session 2026-05-28

- Q: Settings-list card grouping style → A: Single rounded grouped container holding all six rows separated by horizontal dividers (no per-row `Card`).
- Q: Logout row visual distinction → A: No distinction — Logout row uses identical icon/text styling to the other five rows.
- Q: Avatar size → A: 120dp circular diameter (Material 3 large-avatar default for profile headers).
- Q: Floating button icon and component style → A: Use a camera icon (`Icons.Default.PhotoCamera`) rendered inside a Material 3 `SmallFloatingActionButton` (40dp, with FAB elevation/shadow). Supersedes the original "plus icon" wording.

**Input**: User description: "Create just UI for profile screen without any implementation. The screen should contain a top toolbar with the title 'Profile' aligned to the left (just like in FavouriteScreen), followed by a centered circular profile image placeholder (profile_dummy.png) with a small floating plus icon button attached to the profile image for future upload functionality. Below the profile image, display the hardcoded email text 'test@gmail.com'. Under the email, create a stylish settings-style list with rounded cards containing icons, text, and trailing arrow indicators for the following options: QR Code Scan, QR Code Generation, Connect to Bluetooth Device, Notification, Language, Logout. Do create a viewmodel containing the hardcoded email id and a Ui state class to pass the email id."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Profile Identity (Priority: P1)

A signed-in user opens the Profile tab and sees their identity at a glance: a profile photo placeholder, an attached upload-affordance button, and their email address.

**Why this priority**: Profile identity is the primary purpose of the screen; without it the screen has no informational value.

**Independent Test**: Launch the app, navigate to the Profile tab. Verify the top toolbar shows "Profile" left-aligned, a centered circular image placeholder is shown using `profile_dummy.png`, a small floating camera-icon button overlays the bottom-right of the image, and the email `test@gmail.com` appears below the image.

**Acceptance Scenarios**:

1. **Given** the user is on the bottom-nav Profile tab, **When** the screen renders, **Then** the toolbar title "Profile" appears left-aligned matching the Favourites screen toolbar style.
2. **Given** the screen has rendered, **When** the user inspects the avatar area, **Then** a circular placeholder image (`profile_dummy.png`) is displayed centered horizontally with a small floating camera-icon button (Material 3 `SmallFloatingActionButton` containing `Icons.Default.PhotoCamera`) overlapping its bottom-right edge.
3. **Given** the avatar has rendered, **When** the user scans below it, **Then** the text `test@gmail.com` appears beneath the avatar, horizontally centered.

---

### User Story 2 - Browse Settings Options (Priority: P2)

The same user scans a settings-style list below their email to discover available account/device actions.

**Why this priority**: Without the list, the screen offers identity only; the list is what makes the screen useful as a settings hub, but the screen still has value (identity) without it. UI-only delivery is acceptable for v1.

**Independent Test**: Scroll the Profile screen. Verify a single rounded-corner grouped container appears under the email holding six rows separated by horizontal dividers. Each row shows a leading icon, a label, and a trailing chevron (right-arrow) indicator. Labels in order: QR Code Scan, QR Code Generation, Connect to Bluetooth Device, Notification, Language, Logout.

**Acceptance Scenarios**:

1. **Given** the screen is rendered, **When** the user looks below the email, **Then** a single rounded-corner grouped container holds six rows listed vertically in this order: QR Code Scan, QR Code Generation, Connect to Bluetooth Device, Notification, Language, Logout.
2. **Given** any row in the list, **When** the user looks at its layout, **Then** the row shows a leading icon on the left, the label text in the middle, and a trailing right-pointing chevron icon on the right.
3. **Given** two adjacent rows, **When** the user looks between them, **Then** a thin horizontal divider separates them (no per-row card or surrounding gap).
4. **Given** the screen content does not fit on smaller devices, **When** the user scrolls, **Then** the avatar/email/list region scrolls smoothly while the toolbar remains anchored at the top.

---

### Edge Cases

- Smaller screen heights (e.g., 5" phones): content below the toolbar must be vertically scrollable so all six settings rows remain reachable.
- Long email strings: the email text must truncate with ellipsis after a single line rather than wrapping/expanding the avatar block (current hardcoded value fits, but the UI must not break for longer values).
- Tap behavior: the floating camera button and all six list rows are visible and clickable, but tapping them performs no navigation or business action in v1 (UI-only feature).
- Dark mode: all surfaces (toolbar, grouped container, dividers, icons, text) must use `MaterialTheme.colorScheme.*` tokens so they remain legible in both light and dark themes.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Profile screen MUST display a top toolbar with the title "Profile" left-aligned, matching the existing Favourites screen toolbar styling (background = `MaterialTheme.colorScheme.background`, thin horizontal divider below).
- **FR-002**: Profile screen MUST display a centered circular avatar with a 120dp diameter using the bundled drawable `profile_dummy.png` rendered with circular clipping.
- **FR-003**: Profile screen MUST overlay a small floating camera-icon button on the bottom-right edge of the circular avatar, implemented as a Material 3 `SmallFloatingActionButton` containing `Icons.Default.PhotoCamera`; the button is visible and tappable but performs no action in v1.
- **FR-004**: Profile screen MUST display the email string `test@gmail.com` directly below the avatar, horizontally centered, sourced from a UI state value (not hardcoded in the composable).
- **FR-005**: Profile screen MUST display, under the email, a single rounded-corner grouped container holding six rows in this exact order: (1) QR Code Scan, (2) QR Code Generation, (3) Connect to Bluetooth Device, (4) Notification, (5) Language, (6) Logout. Per-row `Card` components MUST NOT be used.
- **FR-006**: Each settings row MUST contain a leading icon, the label text, and a trailing right-pointing chevron indicator.
- **FR-006a**: Adjacent rows MUST be separated by a thin `HorizontalDivider` (inset to align with the row text where appropriate); the divider MUST NOT appear above the first row or below the last row.
- **FR-007**: Rows MUST NOT trigger navigation or business logic when tapped in v1; ripple/visual feedback is acceptable but no callbacks are wired.
- **FR-007a**: All six rows (including Logout) MUST share identical styling for icon color, text color, chevron, padding, and divider treatment — no row receives a destructive/error tint or other visual distinction in v1.
- **FR-008**: A `ProfileViewModel` MUST be introduced that exposes a `StateFlow<ProfileUiState>` whose `email` field is initialized to the hardcoded value `test@gmail.com`.
- **FR-009**: A `ProfileUiState` data class MUST be introduced to carry the email value to the screen; the composable MUST read the email from this state, not from a literal string in the UI layer.
- **FR-010**: The ViewModel MUST be registered in `ViewModelModule` and obtained in the composable via `koinViewModel()`.
- **FR-011**: The screen MUST be scrollable when content exceeds the viewport so all six list items are reachable on smaller devices.
- **FR-012**: All colors, typography, and shapes MUST be sourced from the design system (`MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, `MaterialTheme.shapes.*`) — no hardcoded `Color(0xFF...)` values inside the composable.

### Key Entities *(include if feature involves data)*

- **ProfileUiState**: UI state holder for the Profile screen. Attribute: `email: String`. Provided by the ViewModel and consumed by the composable.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user navigating to the Profile tab sees the toolbar, avatar, floating camera button, email, and full six-item settings list appear immediately on first composition — no skeleton screens, shimmer placeholders, or staggered loading states. All content is statically available from the ViewModel's initial state.
- **SC-002**: 100% of UI elements on the Profile screen render correctly in both light and dark themes without color contrast failures (text and icons remain legible against their card/background surfaces).
- **SC-003**: The Profile screen content is fully reachable via vertical scroll on devices as small as 5" / 360dp width without any element being clipped off-screen, and all six rows remain visually contained within the single grouped container.
- **SC-004**: Code review confirms zero hardcoded color literals, zero hardcoded email strings inside the composable, and the email originates from `ProfileUiState`.

## Assumptions

- The screen is presented inside the existing bottom-tabs shell (`tabsNavGraph`) and inherits its safe-area padding from there; this spec does not modify navigation registration beyond ensuring the existing Profile tab route renders the new screen.
- `profile_dummy.png` already exists at `composeApp/src/commonMain/composeResources/drawable/profile_dummy.png` and is the placeholder asset to use.
- Icons for the six settings rows are sourced from `androidx.compose.material.icons.*` (built-in Material icons) — no new drawable assets are bundled for this feature.
- The hardcoded `test@gmail.com` value is a placeholder for v1; real authentication and dynamic email loading are out of scope.
- Tap actions for the camera button and all six list rows are out of scope for v1 (UI-only feature); navigation and business logic will be wired in a follow-up feature.
- Localization/strings: labels can be hardcoded in English for v1 since the rest of the app currently uses inline English strings; moving to `Res.string.*` is a separate concern.
