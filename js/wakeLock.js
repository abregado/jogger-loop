let wakeLock = null;
let wantWakeLock = false;

export async function acquireWakeLock() {
  wantWakeLock = true;
  if (!("wakeLock" in navigator) || wakeLock) return;
  try {
    wakeLock = await navigator.wakeLock.request("screen");
    wakeLock.addEventListener("release", () => {
      wakeLock = null;
    });
  } catch {
    // Denied (e.g. low battery) or unsupported - fail silently.
  }
}

export async function releaseWakeLock() {
  wantWakeLock = false;
  if (wakeLock) {
    try {
      await wakeLock.release();
    } catch {
      // ignore
    }
    wakeLock = null;
  }
}

// The Wake Lock API auto-releases when the document is hidden; reacquire
// it once the app becomes visible again if a countdown is still running.
document.addEventListener("visibilitychange", () => {
  if (wantWakeLock && document.visibilityState === "visible") {
    acquireWakeLock();
  }
});
