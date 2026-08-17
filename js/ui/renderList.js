import { state } from "../state.js";
import { getProgress, getRemainingMs } from "../timerEngine.js";
import { formatMs, computeLoopLengths } from "../utils.js";
import { bindEditControls } from "./editControls.js";
import { activateTimeEdit } from "./keypadInput.js";
import { updateLoopControls } from "./loopControls.js";
import { createIconButton, ICON_PATHS } from "./icons.js";

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
    content.appendChild(buildEditControls(timer, index, label));
  }

  li.append(fill, content);
  return li;
}

function buildEditControls(timer, index, labelEl) {
  const wrap = document.createElement("div");
  wrap.className = "timer-edit-controls";

  const main = document.createElement("div");
  main.className = "edit-controls-main";

  const pencilBtn = createIconButton("pencil", "Rename timer");

  const toneBtn = createIconButton("tone", "Tone", "toggle-tone");
  toneBtn.classList.toggle("is-active", timer.tone);
  toneBtn.setAttribute("aria-pressed", String(timer.tone));

  const vibrateBtn = createIconButton("vibrate", "Vibrate", "toggle-vibrate");
  vibrateBtn.classList.toggle("is-active", timer.vibrate);
  vibrateBtn.setAttribute("aria-pressed", String(timer.vibrate));

  const pulseToggle = document.createElement("div");
  pulseToggle.className = "pulse-toggle";
  pulseToggle.setAttribute("role", "group");
  pulseToggle.setAttribute("aria-label", "Pulse pattern");

  const pulseSingleBtn = document.createElement("button");
  pulseSingleBtn.type = "button";
  pulseSingleBtn.className = "pulse-btn";
  pulseSingleBtn.innerHTML = '<span class="pulse-dots">●</span>';
  pulseSingleBtn.setAttribute("aria-label", "Single pulse");
  pulseSingleBtn.classList.toggle("is-selected", timer.pulseMode === "single");
  pulseSingleBtn.setAttribute("aria-pressed", String(timer.pulseMode === "single"));

  const pulseTripleBtn = document.createElement("button");
  pulseTripleBtn.type = "button";
  pulseTripleBtn.className = "pulse-btn";
  pulseTripleBtn.innerHTML = '<span class="pulse-dots">● ● ●</span>';
  pulseTripleBtn.setAttribute("aria-label", "Triple pulse");
  pulseTripleBtn.classList.toggle("is-selected", timer.pulseMode === "triple");
  pulseTripleBtn.setAttribute("aria-pressed", String(timer.pulseMode === "triple"));

  pulseToggle.append(pulseSingleBtn, pulseTripleBtn);

  const delBtn = createIconButton("trash", "Delete timer", "delete-icon-btn");

  main.append(pencilBtn, toneBtn, vibrateBtn, pulseToggle, delBtn);

  const moveControls = document.createElement("div");
  moveControls.className = "move-controls";

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

  moveControls.append(upBtn, downBtn);

  wrap.append(main, moveControls);

  bindEditControls({
    timerId: timer.id,
    labelEl,
    pencilBtn,
    toneBtn,
    vibrateBtn,
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
    if (labelEl && labelEl.tagName === "SPAN") {
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

  const icon = running ? ICON_PATHS.pause : ICON_PATHS.play;
  let timeLabel = "";
  if (!running) {
    const { totalLengthMs } = computeLoopLengths(state.timers, state.settings.loopCount);
    timeLabel = `<span class="btn-time-label">${formatMs(totalLengthMs)}</span>`;
  }
  startStopBtn.innerHTML = `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="${icon}"/></svg>${timeLabel}`;
  startStopBtn.classList.toggle("is-running", running);

  editToggleBtn.setAttribute("aria-pressed", String(state.editMode));
  editToggleBtn.disabled = running || paused;
}
