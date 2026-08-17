# Phase 6 — Notifications & Permissions

## Goal

Turn the foreground service's notification (Phase 3) into a genuinely useful lock-screen control surface: Pause/Resume/Reset action buttons, plus the runtime permission flow Android requires before any notification can be shown at all.

## Tasks

1. **Notification action buttons**: `NotificationCompat.Builder.addAction(icon, label, pendingIntent)` for Pause/Resume (toggling based on current state) and Reset, each a `PendingIntent.getService(...)` targeting `TimerService` with the corresponding `ACTION_START`/`ACTION_STOP`/`ACTION_RESET` intent extras already defined in Phase 3. This is what lets the user control a running timer without unlocking the phone.

2. **`POST_NOTIFICATIONS` runtime permission** (required Android 13+ / API 33+): request via `ActivityResultContracts.RequestPermission()` on first launch (or the first time the user attempts to start a timer, whichever reads more naturally in the UI). Worth explaining clearly in-app if denied — unlike most permission denials, this one has an unusually direct consequence: **the foreground service still runs and the engine still ticks correctly, but Android requires a visible notification for any foreground service to exist at all**, so without this permission there's effectively no way to run a timer at all on API 33+. This isn't a "degraded experience" case like the PWA's platform-conditional features (vibration on iOS, etc.) — it's closer to a hard requirement, and the in-app messaging should say so plainly rather than failing silently.

3. **Notification content updates**: confirm the throttled update cadence from Phase 3 (roughly once per second) reads well on an actual lock screen — test on the real device rather than assuming, since lock-screen notification rendering/animation varies by OEM skin.

## Acceptance criteria

- Fresh install, first launch: permission prompt appears, and the app clearly explains why before or alongside the system prompt.
- Denying the permission and then trying to start a timer shows a clear in-app message (not a silent failure) explaining that notifications are required and how to grant them (deep-link to the app's notification settings via `Settings.ACTION_APP_NOTIFICATION_SETTINGS` is a nice touch here, not required for a first pass).
- With permission granted: start a timer, lock the phone, confirm the notification shows the live countdown and that tapping Pause/Reset from the lock screen (may require swiping to reveal actions, depending on notification style) correctly controls the running service without unlocking.

## Notes

- Keep this phase's scope to Pause/Resume/Reset only — don't add per-timer skip/next controls to the notification; that's more surface area than the PWA itself ever exposed via its own control panel, and isn't something that was asked for.
