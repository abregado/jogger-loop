import { updateTimer, deleteTimer, moveTimer } from "../state.js";

export function bindEditControls({
  timerId,
  labelInput,
  toneInput,
  vibInput,
  pulseSingleBtn,
  pulseTripleBtn,
  upBtn,
  downBtn,
  delBtn,
}) {
  labelInput.addEventListener("input", () => {
    updateTimer(timerId, { label: labelInput.value });
  });
  toneInput.addEventListener("change", () => {
    updateTimer(timerId, { tone: toneInput.checked });
  });
  vibInput.addEventListener("change", () => {
    updateTimer(timerId, { vibrate: vibInput.checked });
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
