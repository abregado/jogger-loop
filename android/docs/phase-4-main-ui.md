# Phase 4 — Main UI

## Goal

The Compose equivalent of the PWA's non-edit-mode screen: the timer list with its live progress fill, and the control panel (edit toggle, Play/Pause with total-loop-time label, Reset) — all bound to `TimerService`'s `StateFlow` from Phase 3.

## Tasks

1. **Bind to the service**: `MainActivity` binds via `bindService()`/`ServiceConnection` (or a small `ViewModel` that owns the binding, keeping `MainActivity` itself thin). Collect the exposed `StateFlow<EngineSnapshot>` with `collectAsStateWithLifecycle()` so recomposition only happens while the UI is actually visible.

2. **`TimerListScreen`**: a `LazyColumn` of `TimerRow` composables. Each row shows:
   - Label (or a placeholder like `"Timer ${index + 1}"` when empty — same fallback as the PWA's `data-placeholder` trick).
   - Remaining time, formatted MM:SS.
   - A progress fill — port the PWA's bottom-up gradient concept using `Modifier.drawBehind` or a `Box` with an animated height/weight, driven by `animateFloatAsState` on the engine's per-timer progress value so it fills smoothly rather than snapping every tick.

3. **`ControlPanel`**:
   - Edit-mode toggle `IconButton` (pencil icon).
   - Play/Pause `Button` — shows a Play icon + the total loop time (`sum(timer durations) × loopCount`, formatted MM:SS) when idle/paused; shows just a Pause icon with no time label while running. Direct port of the rule from the PWA's `renderList.js`.
   - Reset `IconButton` (replay icon).
   - This button's disabled states mirror the PWA exactly: Start/Reset greyed out with no timers or in edit mode; Reset also disabled while idle.

4. **Icons**: define each needed icon (pencil, tone, vibrate, trash, play, pause, replay) as a custom `ImageVector` using `Icons.Builder` or `ImageVector.Builder`, reusing the exact SVG path data already written in [`icons.js`](../../js/ui/icons.js) — these paths translate close to directly into Compose's vector path DSL. This avoids pulling in `material-icons-extended` (a multi-MB dependency) for seven icons we've already designed.

## Acceptance criteria

- With the service running a test timer, the UI reflects live countdown and fill progress smoothly, with no visible jank or polling artifacts.
- Backgrounding and reopening the app (without killing the process) shows the UI correctly re-syncing to the service's current state rather than resetting.
- Rotating the device (if rotation isn't locked) doesn't lose or reset timer progress — proves the service, not the Activity, is the source of truth.
- Play/Pause/Reset button states and total-time display match the PWA's behavior exactly, verified against the same scenarios we tested manually in the browser (via `renderList.js`'s `updateControls()`).

## Notes

- Resist adding a ViewModel-per-screen or heavier state-management pattern than needed — a single small ViewModel wrapping the service binding is enough for an app this size. This mirrors the "no premature abstraction" approach we kept throughout the PWA build.
