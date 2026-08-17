import { state, setLoopCount, setFinishTone, setFinishVibrate } from "../state.js";
import { formatMs, computeLoopLengths } from "../utils.js";
import { createIconButton } from "./icons.js";

const panel = document.getElementById("loopSettings");
const valueEl = document.getElementById("loopCountValue");
const decBtn = document.getElementById("loopDecBtn");
const incBtn = document.getElementById("loopIncBtn");
const loopLengthEl = document.getElementById("loopLengthValue");
const totalLengthEl = document.getElementById("totalLengthValue");
const finishIconsEl = document.getElementById("loopFinishIcons");

let toneBtn;
let vibrateBtn;

export function initLoopControls() {
  decBtn.addEventListener("click", () => setLoopCount(state.settings.loopCount - 1));
  incBtn.addEventListener("click", () => setLoopCount(state.settings.loopCount + 1));

  toneBtn = createIconButton("tone", "Tone when all loops finish");
  toneBtn.addEventListener("click", () => setFinishTone(!state.settings.finishTone));

  vibrateBtn = createIconButton("vibrate", "Vibrate when all loops finish");
  vibrateBtn.addEventListener("click", () => setFinishVibrate(!state.settings.finishVibrate));

  finishIconsEl.append(toneBtn, vibrateBtn);
}

export function updateLoopControls() {
  panel.classList.toggle("hidden", !state.editMode);
  valueEl.textContent = `${state.settings.loopCount} Loop${state.settings.loopCount === 1 ? "" : "s"}`;
  decBtn.disabled = state.settings.loopCount <= 1;
  incBtn.disabled = state.settings.loopCount >= 99;

  toneBtn.classList.toggle("is-active", state.settings.finishTone);
  toneBtn.setAttribute("aria-pressed", String(state.settings.finishTone));
  vibrateBtn.classList.toggle("is-active", state.settings.finishVibrate);
  vibrateBtn.setAttribute("aria-pressed", String(state.settings.finishVibrate));

  const { loopLengthMs, totalLengthMs } = computeLoopLengths(state.timers, state.settings.loopCount);
  loopLengthEl.textContent = formatMs(loopLengthMs);
  totalLengthEl.textContent = formatMs(totalLengthMs);
}
