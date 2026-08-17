const TIMERS_KEY = "jogger-loop:timers";
const SETTINGS_KEY = "jogger-loop:settings";

export function loadTimers() {
  try {
    const raw = JSON.parse(localStorage.getItem(TIMERS_KEY));
    return Array.isArray(raw) ? raw : [];
  } catch {
    return [];
  }
}

export function saveTimers(timers) {
  localStorage.setItem(TIMERS_KEY, JSON.stringify(timers));
}

export function loadSettings() {
  try {
    return JSON.parse(localStorage.getItem(SETTINGS_KEY)) || {};
  } catch {
    return {};
  }
}

export function saveSettings(settings) {
  localStorage.setItem(SETTINGS_KEY, JSON.stringify(settings));
}
