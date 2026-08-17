# Phase 1 — Data Model & Persistence

## Goal

Kotlin data classes mirroring the PWA's `state.js` shape, persisted as plain JSON via `org.json` (built into Android — no extra dependency), with a `schemaVersion` field baked in from day one so future changes don't require a rewrite.

## Why plain JSON instead of Room

The PWA already proved this data model is simple: a short list of timers plus a handful of settings. Room (SQLite) is built for querying and relational data — we have neither. A single JSON file read/written wholesale on every change is simpler, has zero query/schema-migration ceremony, and directly mirrors what `storage.js` already does with `localStorage`. If the data model ever grows enough to need real querying, Room can be introduced later — but starting there would be premature complexity for 3 timers and a settings object.

## Tasks

1. **Data classes** (mirroring `state.js`'s timer object and `DEFAULT_SETTINGS`):

   ```kotlin
   enum class PulseMode { SINGLE, TRIPLE }

   data class TimerConfig(
       val id: String,
       val label: String,
       val durationMs: Long,
       val tone: Boolean,
       val vibrate: Boolean,
       val pulseMode: PulseMode,
   )

   data class AppSettings(
       val loopCount: Int,
       val finishTone: Boolean,
       val finishVibrate: Boolean,
   )

   data class AppState(
       val schemaVersion: Int,
       val timers: List<TimerConfig>,
       val settings: AppSettings,
   )
   ```

   Note: `fullscreenPref`/`installBannerShown` from the PWA's settings don't apply to a native app — drop them entirely rather than porting dead fields.

2. **JSON (de)serialization** using `org.json.JSONObject`/`JSONArray` directly — no Gson/Moshi/kotlinx.serialization needed for a shape this small. Write `AppState.toJson(): JSONObject` and a companion `AppState.fromJson(json: JSONObject): AppState`.

3. **Storage location**: `context.filesDir/app-state.json` (private internal storage — not visible to other apps, survives app updates, cleared on uninstall, matching `localStorage`'s semantics).

4. **`TimerRepository` class** wrapping load/save:
   ```kotlin
   class TimerRepository(private val context: Context) {
       fun load(): AppState { ... }   // reads file, runs migrate(), returns parsed AppState; returns a sensible empty default if the file doesn't exist yet
       fun save(state: AppState) { ... }  // overwrites the file wholesale — same "save on every change" philosophy as the PWA
   }
   ```
   This is the one class both the UI and the foreground service will depend on, so keep its API small and synchronous (file I/O this small doesn't need to be async, but callers on the main thread should still dispatch it to `Dispatchers.IO`).

5. **Migration scaffold**:
   ```kotlin
   fun migrate(json: JSONObject): JSONObject {
       var version = json.optInt("schemaVersion", 1)
       if (version < 2) { /* example: add a new field with a default */ version = 2 }
       // ...future version bumps go here, one `if` block per version step
       json.put("schemaVersion", version)
       return json
   }
   ```
   This directly mirrors what `DEFAULT_SETTINGS` merging already does implicitly in `state.js` — the difference is making version bumps explicit and ordered, since native persistence doesn't get the JS convenience of `{ ...defaults, ...loaded }` silently patching in new fields.

## Acceptance criteria

- A JUnit test that constructs an `AppState`, serializes it, deserializes it back, and asserts equality with the original.
- A JUnit test that feeds a schemaVersion-1 JSON blob through `migrate()` and asserts the result has the expected upgraded shape and `schemaVersion == 2` (even if there's nothing real to migrate yet — prove the mechanism works before it's needed).
- These are plain JUnit tests, no emulator/instrumentation required, since `TimerRepository`'s logic (once file I/O is abstracted behind a small interface, or tested against a temp directory) doesn't depend on the Android framework.

## Notes

- Keep `TimerConfig.id` generation as a simple random string (same approach as `genId()` in `utils.js`) — no need for UUID overkill.
- Resist the temptation to reach for `kotlinx.serialization` here even though it's "the modern way" — it's an extra Gradle plugin + dependency for a shape this small, and `org.json` costs nothing.
