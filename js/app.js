import { state, subscribe, setEditMode, addTimer } from "./state.js";
import * as engine from "./timerEngine.js";
import { render } from "./ui/renderList.js";
import { initInstallBanner } from "./ui/installBanner.js";
import { initFullscreenPrompt } from "./ui/fullscreenPrompt.js";
import { initLoopControls } from "./ui/loopControls.js";
import { unlockAudio } from "./audio.js";

const editToggleBtn = document.getElementById("editToggleBtn");
const startStopBtn = document.getElementById("startStopBtn");
const resetBtn = document.getElementById("resetBtn");
const addTimerBtn = document.getElementById("addTimerBtn");

editToggleBtn.addEventListener("click", () => {
  setEditMode(!state.editMode);
});

startStopBtn.addEventListener("click", () => {
  unlockAudio();
  if (state.run.status === "running") {
    engine.stop();
  } else {
    engine.start();
  }
});

resetBtn.addEventListener("click", () => {
  engine.reset();
});

addTimerBtn.addEventListener("click", () => {
  addTimer();
});

initLoopControls();
subscribe(render);
render();

initInstallBanner();
initFullscreenPrompt();

if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("service-worker.js").catch(() => {});
  });
}
