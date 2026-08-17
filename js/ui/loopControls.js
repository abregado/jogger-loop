import { state, setLoopCount, setFinishTone, setFinishVibrate } from "../state.js";
import { formatMs } from "../utils.js";

const panel = document.getElementById("loopSettings");
const valueEl = document.getElementById("loopCountValue");
const decBtn = document.getElementById("loopDecBtn");
const incBtn = document.getElementById("loopIncBtn");
const toneToggle = document.getElementById("finishToneToggle");
const vibrateToggle = document.getElementById("finishVibrateToggle");
const loopLengthEl = document.getElementById("loopLengthValue");
const totalLengthEl = document.getElementById("totalLengthValue");

export function initLoopControls() {
  decBtn.addEventListener("click", () => setLoopCount(state.settings.loopCount - 1));
  incBtn.addEventListener("click", () => setLoopCount(state.settings.loopCount + 1));
  toneToggle.addEventListener("change", () => setFinishTone(toneToggle.checked));
  vibrateToggle.addEventListener("change", () => setFinishVibrate(vibrateToggle.checked));
}

export function updateLoopControls() {
  panel.classList.toggle("hidden", !state.editMode);
  valueEl.textContent = String(state.settings.loopCount);
  decBtn.disabled = state.settings.loopCount <= 1;
  incBtn.disabled = state.settings.loopCount >= 99;
  toneToggle.checked = state.settings.finishTone;
  vibrateToggle.checked = state.settings.finishVibrate;

  const loopLengthMs = state.timers.reduce((sum, t) => sum + t.durationMs, 0);
  const totalLengthMs = loopLengthMs * state.settings.loopCount;
  loopLengthEl.textContent = formatMs(loopLengthMs);
  totalLengthEl.textContent = formatMs(totalLengthMs);
}
