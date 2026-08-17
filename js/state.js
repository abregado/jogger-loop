import { loadTimers, saveTimers, loadSettings, saveSettings } from "./storage.js";
import { genId } from "./utils.js";

const listeners = new Set();

const DEFAULT_SETTINGS = {
  loopCount: 1,
  finishTone: true,
  finishVibrate: true,
};

export const state = {
  timers: loadTimers(),
  editMode: false,
  settings: { ...DEFAULT_SETTINGS, ...loadSettings() },
  run: {
    status: "idle", // idle | running | paused
    currentIndex: 0,
    accumulatedMs: 0,
    segmentStartedAt: null,
    loopsRemaining: 0,
  },
};

export function subscribe(fn) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

export function notify() {
  for (const fn of listeners) fn(state);
}

function persistTimers() {
  saveTimers(state.timers);
}

export function persistSettings() {
  saveSettings(state.settings);
}

export function setEditMode(on) {
  state.editMode = on;
  notify();
}

export function setLoopCount(n) {
  state.settings.loopCount = Math.max(1, Math.min(99, Math.round(n) || 1));
  persistSettings();
  notify();
}

export function setFinishTone(on) {
  state.settings.finishTone = on;
  persistSettings();
  notify();
}

export function setFinishVibrate(on) {
  state.settings.finishVibrate = on;
  persistSettings();
  notify();
}

export function addTimer() {
  state.timers.push({
    id: genId(),
    label: "",
    durationMs: 30000,
    tone: true,
    vibrate: true,
    pulseMode: "single",
  });
  persistTimers();
  notify();
}

export function deleteTimer(id) {
  state.timers = state.timers.filter((t) => t.id !== id);
  persistTimers();
  notify();
}

export function moveTimer(id, dir) {
  const idx = state.timers.findIndex((t) => t.id === id);
  const newIdx = idx + dir;
  if (idx === -1 || newIdx < 0 || newIdx >= state.timers.length) return;
  const [item] = state.timers.splice(idx, 1);
  state.timers.splice(newIdx, 0, item);
  persistTimers();
  notify();
}

export function updateTimer(id, patch) {
  const t = state.timers.find((t) => t.id === id);
  if (!t) return;
  Object.assign(t, patch);
  persistTimers();
  notify();
}
