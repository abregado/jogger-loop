import { state } from "../state.js";
import { getProgress, getRemainingMs } from "../timerEngine.js";
import { formatMs } from "../utils.js";
import { bindEditControls } from "./editControls.js";
import { activateTimeEdit } from "./keypadInput.js";
import { updateLoopControls } from "./loopControls.js";

const listEl = document.getElementById("timerList");
const emptyStateEl = document.getElementById("emptyState");
const addBtn = document.getElementById("addTimerBtn");
const startStopBtn = document.getElementById("startStopBtn");
const resetBtn = document.getElementById("resetBtn");
const editToggleBtn = document.getElementById("editToggleBtn");

let lastSignature = null;

export function render() {
  const signature = `${state.editMode}|${state.timers.map((t) => t.id).join(",")}`;
  if (signature !== lastSignature) {
    rebuild();
    lastSignature = signature;
  }
  updateProgress();
  updateControls();
  updateLoopControls();
}

function rebuild() {
  listEl.innerHTML = "";
  state.timers.forEach((timer, index) => {
    listEl.appendChild(buildRow(timer, index));
  });
  emptyStateEl.classList.toggle("hidden", state.timers.length > 0 || !state.editMode);
  addBtn.classList.toggle("hidden", !state.editMode);
}

function buildRow(timer, index) {
  const li = document.createElement("li");
  li.className = "timer-item";
  li.dataset.id = timer.id;

  const fill = document.createElement("div");
  fill.className = "timer-fill";

  const content = document.createElement("div");
  content.className = "timer-content";

  const main = document.createElement("div");
  main.className = "timer-main";

  const label = document.createElement("span");
  label.className = "timer-label";
  label.dataset.placeholder = `Timer ${index + 1}`;
  label.textContent = timer.label || "";

  const value = document.createElement("button");
  value.type = "button";
  value.className = "timer-value";
  value.textContent = formatMs(timer.durationMs);
  if (state.editMode) {
    value.addEventListener("click", () => activateTimeEdit(timer.id, value));
  } else {
    value.disabled = true;
  }

  main.append(label, value);
  content.append(main);

  if (state.editMode) {
    content.appendChild(buildEditControls(timer, index));
  }

  li.append(fill, content);
  return li;
}

function buildEditControls(timer, index) {
  const wrap = document.createElement("div");
  wrap.className = "timer-edit-controls";

  const labelInput = document.createElement("input");
  labelInput.type = "text";
  labelInput.className = "timer-label-input";
  labelInput.placeholder = `Timer ${index + 1} label (optional)`;
  labelInput.maxLength = 24;
  labelInput.value = timer.label || "";

  const toggleRow = document.createElement("div");
  toggleRow.className = "toggle-row";

  const toneLabel = document.createElement("label");
  const toneInput = document.createElement("input");
  toneInput.type = "checkbox";
  toneInput.className = "toggle-tone";
  toneInput.checked = timer.tone;
  toneLabel.append(toneInput, document.createTextNode(" Tone"));

  const vibLabel = document.createElement("label");
  const vibInput = document.createElement("input");
  vibInput.type = "checkbox";
  vibInput.className = "toggle-vibrate";
  vibInput.checked = timer.vibrate;
  vibLabel.append(vibInput, document.createTextNode(" Vibrate"));

  const pulseToggle = document.createElement("div");
  pulseToggle.className = "pulse-toggle";
  pulseToggle.setAttribute("role", "group");
  pulseToggle.setAttribute("aria-label", "Pulse mode");

  const pulseSingleBtn = document.createElement("button");
  pulseSingleBtn.type = "button";
  pulseSingleBtn.className = "pulse-btn";
  pulseSingleBtn.textContent = "Single";
  pulseSingleBtn.classList.toggle("is-selected", timer.pulseMode === "single");
  pulseSingleBtn.setAttribute("aria-pressed", String(timer.pulseMode === "single"));

  const pulseTripleBtn = document.createElement("button");
  pulseTripleBtn.type = "button";
  pulseTripleBtn.className = "pulse-btn";
  pulseTripleBtn.textContent = "Triple";
  pulseTripleBtn.classList.toggle("is-selected", timer.pulseMode === "triple");
  pulseTripleBtn.setAttribute("aria-pressed", String(timer.pulseMode === "triple"));

  pulseToggle.append(pulseSingleBtn, pulseTripleBtn);

  toggleRow.append(toneLabel, vibLabel, pulseToggle);

  const rowActions = document.createElement("div");
  rowActions.className = "row-actions";

  const upBtn = document.createElement("button");
  upBtn.type = "button";
  upBtn.className = "move-up-btn";
  upBtn.textContent = "↑";
  upBtn.disabled = index === 0;
  upBtn.setAttribute("aria-label", "Move timer up");

  const downBtn = document.createElement("button");
  downBtn.type = "button";
  downBtn.className = "move-down-btn";
  downBtn.textContent = "↓";
  downBtn.disabled = index === state.timers.length - 1;
  downBtn.setAttribute("aria-label", "Move timer down");

  const delBtn = document.createElement("button");
  delBtn.type = "button";
  delBtn.className = "delete-btn";
  delBtn.textContent = "Delete";

  rowActions.append(upBtn, downBtn, delBtn);
  wrap.append(labelInput, toggleRow, rowActions);

  bindEditControls({
    timerId: timer.id,
    labelInput,
    toneInput,
    vibInput,
    pulseSingleBtn,
    pulseTripleBtn,
    upBtn,
    downBtn,
    delBtn,
  });

  return wrap;
}

function updateProgress() {
  state.timers.forEach((timer, index) => {
    const li = listEl.querySelector(`[data-id="${timer.id}"]`);
    if (!li) return;

    const progress = getProgress(index);
    const fill = li.querySelector(".timer-fill");
    fill.style.height = `${(progress * 100).toFixed(2)}%`;

    li.classList.toggle("is-active", state.run.status !== "idle" && index === state.run.currentIndex);
    li.classList.toggle("is-complete", progress >= 1);

    const valueEl = li.querySelector(".timer-value");
    if (valueEl && valueEl.tagName === "BUTTON") {
      valueEl.textContent = formatMs(getRemainingMs(index));
    }

    const labelEl = li.querySelector(".timer-label");
    if (labelEl) {
      labelEl.textContent = timer.label || "";
    }
  });
}

function updateControls() {
  const hasTimers = state.timers.length > 0;
  const running = state.run.status === "running";
  const paused = state.run.status === "paused";

  startStopBtn.disabled = !hasTimers || state.editMode;
  resetBtn.disabled = !hasTimers || state.editMode || state.run.status === "idle";

  let label = running ? "Stop" : "Start";
  const loopsLeft = state.run.loopsRemaining || 0;
  if ((running || paused) && loopsLeft > 0) {
    label += ` (${loopsLeft} loop${loopsLeft === 1 ? "" : "s"} left)`;
  }
  startStopBtn.textContent = label;
  startStopBtn.classList.toggle("is-running", running);

  editToggleBtn.setAttribute("aria-pressed", String(state.editMode));
  editToggleBtn.disabled = running || paused;
}
