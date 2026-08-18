# Phase 6 — Notifications & Permissions

## Goal

Turn the foreground service's notification (Phase 3) into a genuinely useful lock-screen control surface: Pause/Resume/Reset action buttons, plus the runtime permission flow Android requires before any notification can be shown at all.

## Tasks

1. **Notification action buttons**: `NotificationCompat.Builder.addAction(icon, label, pendingIntent)` for Pause/Resume (toggling based on current state) and Reset, each a `PendingIntent.getService(...)` targeting `TimerService` with the corresponding `ACTION_START`/`ACTION_STOP`/`ACTION_RESET` intent extras already defined in Phase 3. This is what lets the user control a running timer without unlocking the phone.

2. **`POST_NOTIFICATIONS` runtime permission** (relevant Android 13+ / API 33+ only): request via `ActivityResultContracts.RequestPermission()` on first launch.

   **Correction, found during implementation:** the paragraph originally here claimed denying this permission makes it impossible to run a timer at all, and had the in-app dialog block Start until granted. That's wrong — per [Android's own docs](https://developer.android.com/develop/ui/compose/notifications/notification-permission), a foreground service keeps running exactly as before even without `POST_NOTIFICATIONS`; the system just doesn't display its notification (it also won't show on the lock screen, and on API 33+ it appears in the Task Manager's running-services list rather than the drawer). The engine, tone, vibration, and wake lock are all untouched by this permission. So this **is** a "degraded experience" case like the PWA's platform-conditional features, not a hard requirement — treat it that way: explain the trade-off once, offer a settings shortcut, but never block starting a timer on it.

3. **Notification content updates**: confirm the throttled update cadence from Phase 3 (roughly once per second) reads well on an actual lock screen — test on the real device rather than assuming, since lock-screen notification rendering/animation varies by OEM skin.

## Acceptance criteria

- Fresh install, first launch: permission prompt appears, and the app clearly explains why before or alongside the system prompt.
- Denying the permission does **not** block starting a timer — the timer runs normally, and the app shows a small non-blocking note that notifications are off (with a deep-link to `Settings.ACTION_APP_NOTIFICATION_SETTINGS` to turn them back on), not a gate.
- With permission granted: start a timer, lock the phone, confirm the notification shows the live countdown and that tapping Pause/Reset from the lock screen (may require swiping to reveal actions, depending on notification style) correctly controls the running service without unlocking.

## Notes

- Keep this phase's scope to Pause/Resume/Reset only — don't add per-timer skip/next controls to the notification; that's more surface area than the PWA itself ever exposed via its own control panel, and isn't something that was asked for.
