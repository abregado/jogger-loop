# Jogger Loop — Native Android App

A native Kotlin/Jetpack Compose rewrite of the [Jogger Loop PWA](../README.md), built to solve what a PWA fundamentally can't: a live timer status on the lock screen, and reliable tone/vibration alerts while the phone is locked or backgrounded.

This app is **sideload-only** — built and distributed as a signed APK via GitHub Releases, installed with "install from unknown sources" / developer mode. It is never intended for a store.

## Why this exists

The PWA (in the repo root) suspends its JavaScript the moment the screen locks or the app backgrounds — that's a platform restriction, not a bug, and it means no web technology can deliver a live lock-screen countdown or guaranteed background vibration on Android. The only way to get that is a native **foreground service**, which is what this app is built around. See the project's chat history / commit log for the full reasoning if you want the background.

## Key decisions (locked in before work started)

- **Language/UI**: Kotlin + Jetpack Compose.
- **Dependencies kept minimal on purpose**: no Room, no Hilt, no Retrofit/networking, no `material-icons-extended`. Persistence is plain JSON via `org.json` (built into Android, zero extra dependency). Icons are hand-defined `ImageVector`s reusing the same SVG path data from the PWA's `icons.js`. The only real third-party-ish dependency is `kotlinx.coroutines`, which is effectively standard Kotlin infrastructure at this point.
- **minSdk**: targeting around Android 11 (API 30) — a couple of years of headroom below the primary device (Android 17), without dragging in real legacy-compatibility code.
- **Data migration**: skipped. Only 3 timers exist in the PWA today — they'll just be re-entered by hand in the new app. No import/export feature planned.
- **Updates**: no in-app update checker. [Obtainium](https://github.com/ImperiumLabs/Obtainium) (a separate, user-installed app) tracks this repo's GitHub Releases and handles update notifications/installs. This app has zero code or dependency related to it — see [phase 8](docs/phase-8-updates-obtainium.md).
- **CI/CD**: GitHub Actions builds, signs, and publishes a release APK on tagged pushes (`android-v*`), so releases are just `git tag && git push --tags`. See [phase 7](docs/phase-7-cicd-release.md).

## Relationship to the PWA

The PWA in the repo root keeps running unchanged, deployed to the same GitHub Pages link. The `.github/workflows/deploy.yml` workflow explicitly excludes this `android/` directory from the Pages deployment and doesn't re-trigger on android-only pushes, so the two apps are fully decoupled in the same repo.

## Build plan

Work is broken into phases, each with its own planning document. Roughly one phase = one focused work session.

| Phase | Doc | Goal | Status |
|---|---|---|---|
| 0 | [Environment setup](docs/phase-0-environment-setup.md) | Android Studio installed, empty Compose project running on your phone | ✅ Done |
| 1 | [Data model & persistence](docs/phase-1-data-model.md) | Kotlin data classes + JSON storage, versioned for future migrations | ✅ Done |
| 2 | [Timer engine](docs/phase-2-timer-engine.md) | Pure, unit-tested Kotlin port of the countdown/loop logic | ✅ Done |
| 3 | [Foreground service](docs/phase-3-foreground-service.md) | The piece that survives the lock screen — notification, vibration, tone, wake lock | ✅ Done |
| 4 | [Main UI](docs/phase-4-main-ui.md) | Compose timer list + control panel, bound to the service | ✅ Done |
| 5 | [Edit mode UI](docs/phase-5-edit-mode-ui.md) | Add/reorder/rename/delete timers, loop settings panel | ✅ Done |
| 6 | [Notifications & permissions](docs/phase-6-notifications-permissions.md) | Lock-screen notification actions, `POST_NOTIFICATIONS` flow | ✅ Done |
| 7 | [CI/CD & release](docs/phase-7-cicd-release.md) | Signed APK build + GitHub Release automation | ✅ Done (built ahead of schedule to validate the pipeline early) |
| 8 | [Updates via Obtainium](docs/phase-8-updates-obtainium.md) | One-time end-user setup, no app code | ⬜ Not started |

Each doc has a goal, concrete tasks, the key Android APIs involved, and acceptance criteria for "this phase is done." Phases 0–7 are implemented and committed — see git log for the detailed history of what was built and fixed along the way.

## Implementation notes / deviations worth knowing before touching this code

- **Vibration**: `VibrationEffect.createWaveform()` with a 1-element array (and separately, `createOneShot()`) didn't reliably fire on the test device, despite multi-element waveforms working fine. Both pulse modes now use multi-pulse patterns differentiated by count (3 vs 4) rather than 1-vs-3 — see the comment in `TimerService.vibrate()`. Tone keeps the original 1-beep/3-beep distinction; only vibration needed this workaround.
- **Tone**: `ToneGenerator` only offers fixed preset tones, not arbitrary frequencies like the PWA's Web Audio oscillator (880/988/440 Hz) — picked distinguishable presets, not exact parity. Possible later polish: bundled WAV files via `SoundPool` for exact pitch matching.
- **Service lifecycle**: `TimerService.start()` calls `ContextCompat.startForegroundService()` on itself before actually starting — required so the service survives `MainActivity` unbinding (e.g. screen lock), since being bound alone doesn't keep a service alive independent of its binder. See the comment there before changing service start/stop logic.
- **Local Gradle CLI builds don't work on this dev machine** — `./gradlew` from a terminal hits a Windows/JDK bug (`Unable to establish loopback connection`, a `WEPollSelectorProvider`/Unix-domain-socket issue), unrelated to the project. Use Android Studio's Run button or its Gradle tool window instead; CI (Linux) is unaffected.
- **Physical test device**: Pixel 6, adb serial `25051FDF600591`. An emulator is also often running alongside it — `adb` commands need `-s 25051FDF600591` to target the physical device specifically.
- **Release signing keystore**: generated locally, lives outside the repo at `C:\Users\Ben Buckton\joggerloop-signing\release.keystore` (never committed — `*.keystore`/`*.jks` are gitignored). Its base64 form and passwords are already in the repo's GitHub Actions secrets (`RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`) — Phase 7 is fully working, releases are just `git tag android-vX.Y.Z && git push --tags`.
- **Notification permission flow (Phase 6)**: the first-launch rationale dialog (`NotificationPermissionRationaleDialog`) is shown once, decided in `MainActivity.onCreate()` — it deliberately isn't re-shown on every `onStart()` return-to-app, since that would be noisy for a user who already made a choice. **`POST_NOTIFICATIONS` denial never blocks starting a timer** — a foreground service keeps running fine without it (per Android's docs, the system just doesn't display the notification; nothing else about the service is affected). `MainScreen` instead shows a small non-blocking `NotificationsDisabledBanner` with a settings shortcut when the permission is off. An earlier version of this flow put up a modal claiming the permission was required and blocked Start on denial — that was factually wrong and got called out, correctly, as a dark pattern; don't reintroduce that gate.
- **Notification channel is classified "Silent" by Android — intentional**: `createNotificationChannel()` uses `IMPORTANCE_LOW` specifically so the once-per-second `notify()` calls that update the countdown don't trigger a heads-up popup or system sound on every tick. `IMPORTANCE_LOW` unconditionally bars the *channel* from making sound, which is exactly why Android's own settings UI buckets it under "Silent" — even though the app clearly makes noise. That noise (`playTone()`/`vibrate()` in `TimerService`) goes through `ToneGenerator`/`Vibrator` directly, entirely independent of the notification channel. One real consequence: some devices have a separate "hide silent notifications on lock screen" toggle that, if enabled, hides this notification even with `POST_NOTIFICATIONS` granted — found and fixed on-device by disabling that toggle. Bumping to `IMPORTANCE_DEFAULT` would avoid that, but risks Android's own default notification sound stacking with the app's tone on every run's first post — deliberately left as `IMPORTANCE_LOW`.
- **Notification's second action is "Open," not "Reset"**: the notification only ever shows Pause/Resume plus one more action, and that second slot is `openAppPendingIntent()` (opens `MainActivity`), not a Reset button. Reset used to live there, but since `reset()` makes the whole notification disappear (`stopForeground(STOP_FOREGROUND_REMOVE)`), it read as an unexpected "End"/"Cancel" button. Resetting is still available from within the app; the notification only offers a safe, non-destructive way back in. Also deliberately *not* a `setContentIntent()` on the notification body — too easy to fat-finger while reaching for the adjacent action button.
