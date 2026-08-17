# Phase 0 — Environment Setup

## Goal

Get Android Studio installed, an empty Jetpack Compose project created inside this `android/` directory, and running on your physical phone via USB debugging. Nothing app-specific yet — this phase just proves the toolchain works end to end.

## Tasks

1. **Install Android Studio** (latest stable channel). It bundles the Android SDK, platform tools (`adb`), and an emulator, though we'll be testing on a real device from the start since background/lock-screen behavior can't be verified in an emulator.

2. **During the setup wizard**, make sure the SDK Manager installs:
   - The SDK Platform matching your phone (Android 17 — confirm the exact API level once Android Studio's SDK Manager lists it; recent Android versions have generally incremented the API level by 1 each year).
   - **SDK Platform-Tools** (this gives you `adb`).
   - A recent **Android SDK Build-Tools** version (Android Studio picks a sensible default).

3. **Enable Developer Options + USB debugging on your phone**:
   - Settings → About phone → tap "Build number" 7 times.
   - Settings → System → Developer options → enable **USB debugging**.
   - Plug the phone in via USB; accept the "Allow USB debugging?" prompt that appears on the phone.

4. **Create the project**:
   - File → New → New Project → **Empty Activity** (this is the Compose template, not the older View-based "Empty Activity" — Android Studio's newer versions default to Compose, but double-check the template says Compose/Kotlin).
   - **Save location**: point it at `<repo root>/android` — Android Studio will populate that directory with `app/`, `gradle/`, `settings.gradle.kts`, etc.
   - **Package name**: pick a reverse-domain identifier now, since it's painful to change later — e.g. `com.abregado.joggerloop` (adjust to whatever you'd like; this becomes the app's permanent identity on the device and in signing).
   - **Minimum SDK**: set to API 30 (Android 11), per the decision in the [android README](../README.md).
   - **Language**: Kotlin. **Build configuration language**: Kotlin DSL (`build.gradle.kts`), not Groovy — keeps things consistent and is Android Studio's current default.

5. **Verify device connection**: open a terminal, run `adb devices` — your phone should appear listed as `device` (not `unauthorized`; if it shows unauthorized, check the phone screen for a pending debugging-authorization prompt).

6. **Run it**: hit the ▶ Run button in Android Studio with your phone selected as the target. The default template screen ("Hello Android" or similar) should appear on your actual phone.

7. **Check `.gitignore`**: Android Studio auto-generates a reasonable `android/.gitignore` (excluding `build/`, `.gradle/`, `local.properties`, `*.iml`, `.idea/`). Confirm it exists and is doing its job — `local.properties` in particular contains a machine-specific SDK path and must never be committed.

## Acceptance criteria

- `adb devices` shows your phone.
- The default template app builds and displays on your physical phone via the Run button, not just the emulator.
- `git status` from the repo root shows only source files under `android/` as new (no `build/`, `.gradle/`, or `local.properties` — confirms the `.gitignore` is working).

## Notes / gotchas

- The first Gradle sync after project creation downloads a fair amount (Gradle itself, the Android Gradle Plugin, dependencies) — expect it to take a few minutes on first run, faster after.
- If `adb devices` doesn't show your phone at all (not even `unauthorized`), it's usually a USB cable/mode issue — some cables are charge-only. Try a different cable or port.
- Don't worry about picking dependencies yet — the template project as generated already has everything Phase 0 needs (Compose BOM, activity-compose). We'll deliberately avoid adding more than necessary in later phases, per the dependency philosophy in the [android README](../README.md).
