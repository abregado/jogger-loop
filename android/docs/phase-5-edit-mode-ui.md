# Phase 5 — Edit Mode UI

## Goal

Compose port of everything the PWA's edit mode does: add/reorder/rename/delete timers, per-timer tone/vibrate/pulse toggles, numeric duration entry, and the loop settings panel (loop count stepper + finish-alert tone/vibrate + one-loop/all-loops length display).

This phase is the most direct 1:1 translation of existing, already-designed UI — the interaction design was worked out and refined over several rounds on the PWA (icon toggles, the pencil-to-rename pattern, the segmented pulse toggle, the merged/centered loop panel). The job here is porting that design to Compose idioms, not redesigning it.

## Tasks

1. **Edit mode state**: a simple `editMode: Boolean` — this is pure UI state, not something the background service needs to know about, so it can live in the Activity/ViewModel layer rather than `TimerService`.

2. **Per-timer edit row** (mirrors `editControls.js` + `renderList.js`'s `buildEditControls`):
   - Pencil `IconButton` → shows an inline `TextField` in place of the label text for renaming. This is actually *simpler* natively than the PWA's DOM-swap hack (`labelEdit.js`) — Compose's conditional rendering (`if (isRenaming) TextField(...) else Text(...)`) handles this naturally without manual element replacement.
   - Tone/Vibrate `IconToggleButton`s — highlighted when active, same visual language as the PWA's `.icon-toggle-btn.is-active`.
   - Pulse mode: two-option segmented toggle (`●` / `● ● ●` dot glyphs, matching the PWA's final design) — a `Row` of two `FilterChip`s or a custom two-button group.
   - Move up/down `IconButton`s stacked vertically on the right (`Column`), disabled at the list boundaries.
   - Delete `IconButton` (trash icon).

3. **Duration entry**: an `OutlinedTextField` with `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)`. Unlike the PWA — which had to hand-roll digit-shifting logic (`keypadInput.js`) to fake MM:SS formatting on top of a plain text input — native Compose gets a real numeric keyboard for free; a `VisualTransformation` can still auto-insert the `:` at the right position as digits are typed if we want the same MM:SS-while-typing feel, but the underlying mechanism is much simpler than the web version's workaround.

4. **Loop settings panel** (mirrors the final merged/centered `loopControls.js` design):
   - Stepper: `-`/`+` `IconButton`s flanking a `"N Loop(s)"` text (pluralized), centered on a row together with the finish-alert tone/vibrate icon toggles.
   - Below that, a single centered line: `"One loop: MM:SS   All loops: MM:SS"`.
   - No separate "Loops" label and no "When all loops finish" hint text — both were deliberately removed in the PWA's final pass in favor of the icons/values speaking for themselves; keep that decision.

5. **Persistence**: every change (rename, toggle, reorder, delete, duration edit, loop count change) writes through `TimerRepository` (Phase 1) immediately — same "save on every change, no explicit save button" philosophy as the PWA's `state.js`.

## Acceptance criteria

Build and check off a literal parity list against the PWA before considering this phase done:

- [ ] Add timer, delete timer, reorder (up/down) all persist correctly and update the list immediately.
- [ ] Rename via pencil icon works and reflects immediately in both edit and non-edit views.
- [ ] Tone/Vibrate icon toggles highlight correctly and persist.
- [ ] Pulse mode toggle (single/triple) persists and highlights the selected option.
- [ ] Duration entry produces the correct `durationMs`, minimum-clamped the same way `digitsToMs` was (never save a 0-duration timer).
- [ ] Loop count stepper respects the 1–99 bounds, disables at the edges.
- [ ] Finish-alert tone/vibrate icons persist independently of per-timer tone/vibrate.
- [ ] One-loop/all-loops length values update live as durations or loop count change.
- [ ] Start/Reset/edit-toggle disabled states match the PWA exactly (can't enter edit mode while running/paused, can't start with zero timers, etc.).

## Notes

- Since this phase covers the largest surface area of UI, it's the one most worth spreading across multiple sessions rather than one sitting — natural sub-splits are (a) the per-timer row and its controls, then (b) the loop settings panel.
