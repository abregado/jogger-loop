import { state, persistSettings } from "../state.js";

const modal = document.getElementById("fullscreenModal");
const yesBtn = document.getElementById("fullscreenYesBtn");
const noBtn = document.getElementById("fullscreenNoBtn");

function isStandalone() {
  return window.matchMedia("(display-mode: standalone)").matches || window.navigator.standalone === true;
}

function fullscreenSupported() {
  return typeof document.documentElement.requestFullscreen === "function";
}

function requestFullscreenSafe() {
  const el = document.documentElement;
  if (el.requestFullscreen) {
    el.requestFullscreen().catch(() => {});
  }
}

// Fullscreen can only be entered from a user gesture, so a saved "yes"
// preference is applied on the next tap/click rather than automatically on load.
function armAutoFullscreen() {
  const handler = () => {
    requestFullscreenSafe();
    document.removeEventListener("pointerdown", handler);
  };
  document.addEventListener("pointerdown", handler, { once: true });
}

export function initFullscreenPrompt() {
  if (!isStandalone() || !fullscreenSupported()) return;

  if (state.settings.fullscreenPref === true) {
    armAutoFullscreen();
    return;
  }

  modal.classList.remove("hidden");

  yesBtn.addEventListener("click", () => {
    modal.classList.add("hidden");
    state.settings.fullscreenPref = true;
    persistSettings();
    requestFullscreenSafe();
  });

  noBtn.addEventListener("click", () => {
    modal.classList.add("hidden");
    // Intentionally not persisted, so the prompt reappears next launch.
  });
}
