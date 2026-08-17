import { state, updateTimer } from "../state.js";

function buildLabelSpan(timerId) {
  const timer = state.timers.find((t) => t.id === timerId);
  const index = state.timers.findIndex((t) => t.id === timerId);
  const span = document.createElement("span");
  span.className = "timer-label";
  span.dataset.placeholder = `Timer ${index + 1}`;
  span.textContent = timer?.label || "";
  return span;
}

export function activateLabelEdit(timerId, labelEl) {
  const timer = state.timers.find((t) => t.id === timerId);
  if (!timer) return;

  const input = document.createElement("input");
  input.type = "text";
  input.className = "timer-label is-editing";
  input.maxLength = 24;
  input.value = timer.label || "";
  input.placeholder = labelEl.dataset.placeholder || "";

  labelEl.replaceWith(input);
  input.focus();
  input.select();

  let committed = false;
  function commit() {
    if (committed) return;
    committed = true;
    updateTimer(timerId, { label: input.value.trim() });
    input.replaceWith(buildLabelSpan(timerId));
  }

  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      input.blur();
    }
  });

  input.addEventListener("blur", commit, { once: true });
}
