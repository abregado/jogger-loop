import { updateTimer, deleteTimer, moveTimer } from "../state.js";
import { activateLabelEdit } from "./labelEdit.js";

export function bindEditControls({
  timerId,
  labelEl,
  pencilBtn,
  toneBtn,
  vibrateBtn,
  pulseSingleBtn,
  pulseTripleBtn,
  upBtn,
  downBtn,
  delBtn,
}) {
  pencilBtn.addEventListener("click", () => activateLabelEdit(timerId, labelEl));

  toneBtn.addEventListener("click", () => {
    const active = !toneBtn.classList.contains("is-active");
    toneBtn.classList.toggle("is-active", active);
    toneBtn.setAttribute("aria-pressed", String(active));
    updateTimer(timerId, { tone: active });
  });

  vibrateBtn.addEventListener("click", () => {
    const active = !vibrateBtn.classList.contains("is-active");
    vibrateBtn.classList.toggle("is-active", active);
    vibrateBtn.setAttribute("aria-pressed", String(active));
    updateTimer(timerId, { vibrate: active });
  });

  function selectPulse(mode) {
    pulseSingleBtn.classList.toggle("is-selected", mode === "single");
    pulseSingleBtn.setAttribute("aria-pressed", String(mode === "single"));
    pulseTripleBtn.classList.toggle("is-selected", mode === "triple");
    pulseTripleBtn.setAttribute("aria-pressed", String(mode === "triple"));
    updateTimer(timerId, { pulseMode: mode });
  }
  pulseSingleBtn.addEventListener("click", () => selectPulse("single"));
  pulseTripleBtn.addEventListener("click", () => selectPulse("triple"));

  upBtn.addEventListener("click", () => moveTimer(timerId, -1));
  downBtn.addEventListener("click", () => moveTimer(timerId, 1));
  delBtn.addEventListener("click", () => deleteTimer(timerId));
}
