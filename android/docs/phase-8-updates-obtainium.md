# Phase 8 — Updates via Obtainium

## Goal

No app code in this phase — [Obtainium](https://github.com/ImperiumLabs/Obtainium) is a separate, user-installed app that tracks GitHub Releases and handles update notifications/installs for sideloaded apps like this one. This document just covers making sure our release format is Obtainium-compatible and the one-time setup needed on each device.

## Why no in-app update checker

Building our own "check GitHub for a newer release" feature was considered and deliberately dropped: it's real code and real testing for something an existing, focused tool already does well, with zero footprint in this app's codebase or dependency list. Obtainium doesn't require any SDK, library, or API integration on our side — it just reads the same public GitHub Releases API anyone can hit, and needs our releases to look like Phase 7 already produces them: one APK attached per tagged release.

## Tasks

1. **Confirm the release tag scheme stays unambiguous for Obtainium**: using the `android-v*` prefix (from Phase 7) rather than bare `v*` tags matters here too — if the PWA side ever grows its own tagged releases for unrelated reasons, Obtainium's version matching against this repo needs a release stream that's clearly just the Android app's.

2. **One-time end-user setup** (per device, not per update):
   - Install Obtainium itself (also sideloaded — from its own GitHub releases or F-Droid).
   - In Obtainium, "Add App" → paste this repository's URL → Obtainium detects it's a GitHub-releases-based Android app and finds the release APK automatically.
   - Configure per-app settings as desired: background check interval, whether to notify on update, Wi-Fi-only downloads, etc.

3. **What actually happens on update**: Obtainium periodically checks in the background (via Android's standard background task scheduling) and can post a system notification when a newer release is found. Tapping through leads to installing the new APK via Android's normal package installer — which still requires a one-tap confirmation (Android doesn't allow fully silent installs to a non-privileged app, Obtainium included, on a stock, non-rooted device). So the real experience is "background check → notification → one tap to install," not a fully silent Play-Store-style update — worth setting that expectation with whoever's using the app.

## Acceptance criteria

- From a real device with Obtainium installed and this repo added as a tracked app: publish a test release via Phase 7's workflow, confirm Obtainium detects it (may need to trigger a manual check rather than waiting for the background schedule, to verify quickly).
- Install the update through Obtainium and confirm it succeeds without an uninstall/data-loss prompt — this is really re-confirming Phase 7's signing consistency, but from the actual update path a real user will use, not just manual `adb install`.

## Notes

- If Obtainium's automatic APK-asset detection ever picks the wrong file (only relevant if a release ever has more than one attached asset — checksums, multiple ABI-split APKs, etc.), it can be pointed at a specific asset name pattern in its per-app config. Not a concern with the current single-APK release setup from Phase 7.
