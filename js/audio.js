let audioCtx = null;

function getCtx() {
  if (!audioCtx) {
    const Ctx = window.AudioContext || window.webkitAudioContext;
    if (!Ctx) return null;
    audioCtx = new Ctx();
  }
  return audioCtx;
}

// Must be called from within a user-gesture handler (e.g. the Start tap)
// so mobile browsers allow audio to play later without further gestures.
export function unlockAudio() {
  const ctx = getCtx();
  if (ctx && ctx.state === "suspended") ctx.resume();
}

function beep(durationMs, freq = 880) {
  const ctx = getCtx();
  if (!ctx) return;
  if (ctx.state === "suspended") ctx.resume();

  const osc = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.type = "sine";
  osc.frequency.value = freq;

  const now = ctx.currentTime;
  gain.gain.setValueAtTime(0.0001, now);
  gain.gain.exponentialRampToValueAtTime(0.5, now + 0.015);
  gain.gain.exponentialRampToValueAtTime(0.0001, now + durationMs / 1000);

  osc.connect(gain).connect(ctx.destination);
  osc.start(now);
  osc.stop(now + durationMs / 1000 + 0.03);
}

export function playTone(pulseMode) {
  if (pulseMode === "triple") {
    beep(140, 988);
    setTimeout(() => beep(140, 988), 210);
    setTimeout(() => beep(140, 988), 420);
  } else {
    beep(550, 880);
  }
}

// Distinct "all loops complete" signal: 5 slow, low-pitched pulses.
const FINISH_PULSE_COUNT = 5;
const FINISH_PULSE_MS = 450;
const FINISH_GAP_MS = 350;
const FINISH_PITCH_HZ = 440; // lower than the per-timer single (880) and triple (988) tones

export function playFinishTone() {
  const period = FINISH_PULSE_MS + FINISH_GAP_MS;
  for (let i = 0; i < FINISH_PULSE_COUNT; i++) {
    setTimeout(() => beep(FINISH_PULSE_MS, FINISH_PITCH_HZ), i * period);
  }
}
