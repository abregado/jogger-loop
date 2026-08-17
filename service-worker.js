const CACHE_VERSION = "v4";
const CACHE_NAME = `jogger-loop-${CACHE_VERSION}`;

const APP_SHELL = [
  "./",
  "index.html",
  "manifest.webmanifest",
  "css/styles.css",
  "js/app.js",
  "js/state.js",
  "js/storage.js",
  "js/utils.js",
  "js/audio.js",
  "js/vibration.js",
  "js/wakeLock.js",
  "js/timerEngine.js",
  "js/ui/renderList.js",
  "js/ui/editControls.js",
  "js/ui/keypadInput.js",
  "js/ui/installBanner.js",
  "js/ui/fullscreenPrompt.js",
  "js/ui/loopControls.js",
  "icons/icon-192.png",
  "icons/icon-512.png",
  "icons/icon-512-maskable.png",
  "icons/apple-touch-icon.png",
  "icons/favicon-32.png",
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches
      .open(CACHE_NAME)
      .then((cache) => cache.addAll(APP_SHELL))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) => Promise.all(keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET") return;

  event.respondWith(
    caches.match(event.request).then((cached) => {
      if (cached) return cached;

      return fetch(event.request)
        .then((response) => {
          if (response.ok && response.type === "basic") {
            const clone = response.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(event.request, clone));
          }
          return response;
        })
        .catch(() => {
          if (event.request.mode === "navigate") {
            return caches.match("index.html");
          }
          return undefined;
        });
    })
  );
});
