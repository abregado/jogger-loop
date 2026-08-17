import { state, notify } from "./state.js";
import { playTone, playFinishTone } from "./audio.js";
import { vibrate, vibrateFinish } from "./vibration.js";
import { acquireWakeLock, releaseWakeLock } from "./wakeLock.js";

const TICK_MS = 100;
const FINISH_DISPLAY_MS = 4000; // let the full 5-pulse finish alert land before auto-reset

let intervalId = null;

export function canRun() {
  return state.timers.length > 0 && !state.editMode;
}

export function start() {
  if (!canRun()) return;
  if (state.run.status === "idle") {
    state.run.currentIndex = 0;
    state.run.accumulatedMs = 0;
    state.run.loopsRemaining = Math.max(0, state.settings.loopCount - 1);
  }
  state.run.status = "running";
  state.run.segmentStartedAt = Date.now();
  acquireWakeLock();
  startTicking();
  notify();
}

export function stop() {
  if (state.run.status !== "running") return;
  state.run.accumulatedMs += Date.now() - state.run.segmentStartedAt;
  state.run.segmentStartedAt = null;
  state.run.status = "paused";
  stopTicking();
  releaseWakeLock();
  notify();
}

export function reset() {
  state.run.status = "idle";
  state.run.currentIndex = 0;
  state.run.accumulatedMs = 0;
  state.run.segmentStartedAt = null;
  state.run.loopsRemaining = 0;
  stopTicking();
  releaseWakeLock();
  notify();
}

function startTicking() {
  stopTicking();
  intervalId = setInterval(tick, TICK_MS);
}

function stopTicking() {
  if (intervalId) {
    clearInterval(intervalId);
    intervalId = null;
  }
}

function currentElapsedMs() {
  const base = state.run.accumulatedMs;
  if (state.run.status === "running" && state.run.segmentStartedAt != null) {
    return base + (Date.now() - state.run.segmentStartedAt);
  }
  return base;
}

// Progress in [0, 1] for a given timer index, for rendering the fill.
export function getProgress(index) {
  if (state.run.status === "idle") return 0;
  if (index < state.run.currentIndex) return 1;
  if (index > state.run.currentIndex) return 0;
  const timer = state.timers[index];
  if (!timer || timer.durationMs <= 0) return 1;
  return Math.min(1, currentElapsedMs() / timer.durationMs);
}

export function getRemainingMs(index) {
  const timer = state.timers[index];
  if (!timer) return 0;
  if (state.run.status === "idle" || index > state.run.currentIndex) return timer.durationMs;
  if (index < state.run.currentIndex) return 0;
  return Math.max(0, timer.durationMs - currentElapsedMs());
}

function tick() {
  let timer = state.timers[state.run.currentIndex];
  if (!timer) {
    reset();
    return;
  }

  let elapsed = currentElapsedMs();
  const visible = document.visibilityState === "visible";

  while (timer && elapsed >= timer.durationMs) {
    const isLast = state.run.currentIndex >= state.timers.length - 1;
    const isFinalCompletion = isLast && state.run.loopsRemaining <= 0;

    if (visible) {
      if (isFinalCompletion) {
        // End-of-all-loops signal replaces this timer's own alert.
        if (state.settings.finishTone) playFinishTone();
        if (state.settings.finishVibrate) vibrateFinish();
      } else {
        if (timer.tone) playTone(timer.pulseMode);
        if (timer.vibrate) vibrate(timer.pulseMode);
      }
    }

    if (isLast) {
      if (state.run.loopsRemaining > 0) {
        state.run.loopsRemaining -= 1;
        elapsed -= timer.durationMs;
        state.run.currentIndex = 0;
        state.run.accumulatedMs = 0;
        state.run.segmentStartedAt = Date.now() - elapsed;
        timer = state.timers[0];
        continue;
      }

      notify(); // render the final timer as fully complete
      stopTicking();
      releaseWakeLock();
      setTimeout(reset, FINISH_DISPLAY_MS);
      return;
    }

    elapsed -= timer.durationMs;
    state.run.currentIndex += 1;
    state.run.accumulatedMs = 0;
    state.run.segmentStartedAt = Date.now() - elapsed;
    timer = state.timers[state.run.currentIndex];
  }

  notify();
}
