export function genId() {
  return Math.random().toString(36).slice(2, 10);
}

export function formatMs(ms) {
  const totalSeconds = Math.ceil(ms / 1000);
  const m = Math.floor(totalSeconds / 60);
  const s = totalSeconds % 60;
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}

export function digitsToMs(digits) {
  const padded = digits.slice(-4).padStart(4, "0");
  const m = parseInt(padded.slice(0, 2), 10);
  const s = parseInt(padded.slice(2, 4), 10);
  return (m * 60 + Math.min(s, 59)) * 1000;
}

export function msToDigits(ms) {
  const totalSeconds = Math.round(ms / 1000);
  const m = Math.floor(totalSeconds / 60);
  const s = totalSeconds % 60;
  return `${String(Math.min(m, 99)).padStart(2, "0")}${String(s).padStart(2, "0")}`;
}

export function computeLoopLengths(timers, loopCount) {
  const loopLengthMs = timers.reduce((sum, t) => sum + t.durationMs, 0);
  return { loopLengthMs, totalLengthMs: loopLengthMs * loopCount };
}
