# Jogger Loop

A phone-first PWA interval timer. Build a list of timers, run through them in sequence with tone/vibration alerts, and optionally loop the whole set multiple times. No build step, no dependencies — plain HTML, CSS, and JS.

## Features

- Sequential timer list with a live progress fill per timer
- Tone and/or vibration alerts per timer (single or triple pulse)
- Loop the whole list a set number of times, with a distinct end-of-loops alert (5 slow pulses)
- Edit mode for adding/reordering/renaming/deleting timers, with a numeric keypad for setting durations
- All data saved to `localStorage` — no backend, no account
- Installable as a home-screen PWA with offline support (service worker app-shell caching)
- Optional fullscreen mode when launched from the home screen
- Keeps the screen awake while a timer is running (Wake Lock API, where supported)

## Running locally

This is a static site with ES modules, so it needs to be served over `http://`, not opened directly as a `file://` URL (module imports will fail under `file://`).

Any static file server works. For example, from the project root:

```bash
python -m http.server 8123
```

or

```bash
npx serve .
```

Then open `http://localhost:8123` in a browser. A `.claude/launch.json` config is included for the Python option if you're using Claude Code's preview tooling.

## Project structure

```
index.html              App shell
manifest.webmanifest    PWA manifest
service-worker.js       Offline app-shell caching
css/styles.css          All styling
js/
  app.js                Entry point — wires up buttons and init calls
  state.js               Central state store + localStorage persistence
  storage.js              localStorage read/write helpers
  timerEngine.js         Countdown/loop logic (timestamp-based, survives backgrounding)
  audio.js               Web Audio oscillator tones
  vibration.js            Vibration API wrapper
  wakeLock.js             Wake Lock API wrapper
  utils.js                Formatting helpers
  ui/
    renderList.js         Timer list rendering
    editControls.js       Per-timer edit-mode control wiring
    keypadInput.js         Inline numeric time entry
    labelEdit.js            Inline timer rename
    loopControls.js         Loop count / finish-alert settings panel
    icons.js                 Shared SVG icon buttons
    installBanner.js         "Add to home screen" prompt
    fullscreenPrompt.js       Fullscreen-on-launch prompt
icons/                   App icons (192/512/maskable/apple-touch/favicon)
.github/workflows/deploy.yml   GitHub Pages deployment
```

## Deployment

Pushing to `main` triggers [.github/workflows/deploy.yml](.github/workflows/deploy.yml), which deploys the repo root straight to GitHub Pages (no build step). The only one-time setup required is in the repo settings: **Settings → Pages → Source: GitHub Actions**.

## Platform notes

- **Vibration** is not supported on iOS Safari/PWA — the app silently skips it there. Tone alerts still work.
- **Fullscreen** on launch only applies on platforms with the Fullscreen API (not iOS Safari, where home-screen PWAs are already effectively fullscreen). The prompt is skipped there.
- **Wake Lock** support varies (Android Chrome and iOS 16.4+ Safari support it); the app degrades gracefully where it isn't available.
