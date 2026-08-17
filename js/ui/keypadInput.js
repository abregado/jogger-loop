import { state, updateTimer } from "../state.js";
import { formatMs, digitsToMs, msToDigits } from "../utils.js";

function buildValueButton(timer) {
  const btn = document.createElement("button");
  btn.type = "button";
  btn.className = "timer-value";
  btn.textContent = formatMs(timer.durationMs);
  btn.addEventListener("click", () => activateTimeEdit(timer.id, btn));
  return btn;
}

export function activateTimeEdit(timerId, buttonEl) {
  const timer = state.timers.find((t) => t.id === timerId);
  if (!timer) return;

  const input = document.createElement("input");
  input.type = "tel";
  input.inputMode = "numeric";
  input.autocomplete = "off";
  input.className = "timer-value is-editing";
  input.maxLength = 5;

  let digits = msToDigits(timer.durationMs).replace(/^0+(?=\d)/, "");

  function paint() {
    const padded = digits.padStart(4, "0").slice(-4);
    input.value = `${padded.slice(0, 2)}:${padded.slice(2)}`;
  }
  paint();

  buttonEl.replaceWith(input);
  input.focus();
  requestAnimationFrame(() => input.setSelectionRange(input.value.length, input.value.length));

  let committed = false;
  function commit() {
    if (committed) return;
    committed = true;
    const ms = digitsToMs(digits) || 1000;
    updateTimer(timerId, { durationMs: ms });
    const freshTimer = state.timers.find((t) => t.id === timerId) || { ...timer, durationMs: ms };
    input.replaceWith(buildValueButton(freshTimer));
  }

  input.addEventListener("keydown", (e) => {
    if (e.key >= "0" && e.key <= "9") {
      e.preventDefault();
      digits = (digits + e.key).replace(/^0+(?=\d)/, "").slice(-4);
      paint();
    } else if (e.key === "Backspace") {
      e.preventDefault();
      digits = digits.slice(0, -1);
      paint();
    } else if (e.key === "Enter") {
      e.preventDefault();
      input.blur();
    }
  });

  // Mobile numeric keyboards are more reliable via 'input' than keydown.
  input.addEventListener("input", () => {
    const raw = input.value.replace(/\D/g, "");
    digits = raw.replace(/^0+(?=\d)/, "").slice(-4);
    paint();
  });

  input.addEventListener("blur", commit, { once: true });
}
