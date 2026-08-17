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

| Phase | Doc | Goal |
|---|---|---|
| 0 | [Environment setup](docs/phase-0-environment-setup.md) | Android Studio installed, empty Compose project running on your phone |
| 1 | [Data model & persistence](docs/phase-1-data-model.md) | Kotlin data classes + JSON storage, versioned for future migrations |
| 2 | [Timer engine](docs/phase-2-timer-engine.md) | Pure, unit-tested Kotlin port of the countdown/loop logic |
| 3 | [Foreground service](docs/phase-3-foreground-service.md) | The piece that survives the lock screen — notification, vibration, tone, wake lock |
| 4 | [Main UI](docs/phase-4-main-ui.md) | Compose timer list + control panel, bound to the service |
| 5 | [Edit mode UI](docs/phase-5-edit-mode-ui.md) | Add/reorder/rename/delete timers, loop settings panel |
| 6 | [Notifications & permissions](docs/phase-6-notifications-permissions.md) | Lock-screen notification actions, `POST_NOTIFICATIONS` flow |
| 7 | [CI/CD & release](docs/phase-7-cicd-release.md) | Signed APK build + GitHub Release automation |
| 8 | [Updates via Obtainium](docs/phase-8-updates-obtainium.md) | One-time end-user setup, no app code |

Each doc has a goal, concrete tasks, the key Android APIs involved, and acceptance criteria for "this phase is done." None of this code exists yet — these are planning documents to work from, not a finished implementation.
