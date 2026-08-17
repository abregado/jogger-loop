# Phase 7 — CI/CD & Release

## Goal

A GitHub Actions workflow that builds, signs, and publishes a downloadable, installable APK to GitHub Releases whenever a version tag is pushed — so shipping an update is just `git tag android-v1.1.0 && git push --tags`.

## Why signing consistency is the critical detail

Android refuses to install an update over an existing app if the signing certificate doesn't match ("app not installed — conflicts with an existing package"), forcing an uninstall (and data loss) to work around it. Every release, forever, must be signed with the **same** key. Since there's no Play Store managing this for us, we own it entirely — get this right once, at the start, and it's a non-issue from then on.

## Tasks

1. **Generate the release keystore, once, ever**:
   ```bash
   keytool -genkeypair -v -keystore release.keystore -alias joggerloop \
     -keyalg RSA -keysize 2048 -validity 10000
   ```
   Store the resulting `release.keystore` file and its passwords in a password manager. **Never commit this file to git** — it's the one artifact in this entire project that must never appear in the repo, since anyone with it could sign a malicious update that Android would accept as legitimate.

2. **Add GitHub Secrets** (repo Settings → Secrets and variables → Actions):
   - `RELEASE_KEYSTORE_BASE64` — `base64 -w0 release.keystore` output.
   - `RELEASE_KEYSTORE_PASSWORD`
   - `RELEASE_KEY_ALIAS`
   - `RELEASE_KEY_PASSWORD`

3. **Configure signing in `android/app/build.gradle.kts`**: a `signingConfigs.release` block that reads the keystore path/passwords from environment variables rather than hardcoding them, so local development builds stay debug-signed (unaffected) and only CI builds — where those env vars are actually set — produce release-signed output.

4. **Workflow** (`.github/workflows/android-release.yml`, separate from the PWA's `deploy.yml`):
   - Trigger: `on: push: tags: ['android-v*']` — a distinct tag prefix, so this workflow and the PWA's deploy workflow never collide or trigger on each other's changes.
   - `actions/checkout@v4`
   - `actions/setup-java@v4` (Temurin, whichever JDK version the Android Gradle Plugin in use requires — check `android/build.gradle.kts` once Phase 0 has generated it).
   - Cache Gradle (`actions/cache` keyed on `gradle-wrapper.properties`/`*.gradle.kts` hashes, or `gradle/actions/setup-gradle` which handles this automatically).
   - Decode the keystore secret to a temp file: `echo "$RELEASE_KEYSTORE_BASE64" | base64 -d > release.keystore`.
   - `./gradlew assembleRelease` with the signing env vars set from secrets.
   - Derive `versionName` from the pushed tag (strip the `android-v` prefix) and `versionCode` from an incrementing source (e.g. the GitHub Actions run number, or a count of prior `android-v*` tags) — inject via Gradle properties so `build.gradle.kts` doesn't need hardcoded version bumps per release.
   - Upload the signed APK from `android/app/build/outputs/apk/release/*.apk` to a GitHub Release via `softprops/action-gh-release`, tagged with the pushed tag, optionally alongside a checksum file.

5. **Note this workflow doesn't exist yet and can't be written meaningfully until there's a real Gradle project to build** — this phase's concrete work starts once Phase 0–6 produce something `assembleRelease` can actually act on. Treat this document as the spec to implement against once we get here, not something to build prematurely.

## Acceptance criteria

- Pushing a tag like `android-v1.0.0` produces a GitHub Release with a signed APK attached, within a few minutes, without any manual signing step.
- Freshly sideloading that APK on a device with no prior install works.
- Pushing `android-v1.0.1` later and sideloading it **over** the existing `1.0.0` install succeeds without an uninstall prompt and preserves the app's existing data — this is the real proof that signing consistency is working, and worth testing explicitly rather than assuming.

## Notes

- Keep the debug build (`assembleDebug`, what Android Studio's Run button uses day-to-day) entirely separate from this release-signing config — local development should never need the release keystore at all.
