// No-ops silently on platforms without the Vibration API (notably iOS Safari/PWA).
export function vibrate(pulseMode) {
  if (!("vibrate" in navigator)) return;
  if (pulseMode === "triple") {
    navigator.vibrate([140, 120, 140, 120, 140]);
  } else {
    navigator.vibrate(550);
  }
}

// Distinct "all loops complete" signal: 5 slow pulses (matches playFinishTone's timing).
export function vibrateFinish() {
  if (!("vibrate" in navigator)) return;
  navigator.vibrate([450, 350, 450, 350, 450, 350, 450, 350, 450]);
}
