import { state, persistSettings } from "../state.js";

const banner = document.getElementById("installBanner");
const textEl = banner.querySelector(".banner-text");
const installBtn = document.getElementById("installActionBtn");
const dismissBtn = document.getElementById("installDismissBtn");

let deferredPrompt = null;

function isStandalone() {
  return window.matchMedia("(display-mode: standalone)").matches || window.navigator.standalone === true;
}

function isIOS() {
  return /iphone|ipad|ipod/i.test(navigator.userAgent);
}

function dismiss() {
  banner.classList.add("hidden");
  state.settings.installBannerShown = true;
  persistSettings();
}

export function initInstallBanner() {
  if (isStandalone() || state.settings.installBannerShown) return;

  window.addEventListener("beforeinstallprompt", (e) => {
    e.preventDefault();
    deferredPrompt = e;
    installBtn.classList.remove("hidden");
  });

  textEl.textContent = isIOS()
    ? 'Install Jogger Loop: tap the Share icon, then "Add to Home Screen".'
    : "Install Jogger Loop to your home screen for quick, full-screen access.";

  banner.classList.remove("hidden");

  installBtn.addEventListener("click", async () => {
    if (!deferredPrompt) return;
    deferredPrompt.prompt();
    await deferredPrompt.userChoice;
    deferredPrompt = null;
    dismiss();
  });

  dismissBtn.addEventListener("click", dismiss);
}
