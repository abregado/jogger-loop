package com.abregado.joggerloop.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.abregado.joggerloop.data.AppSettings
import com.abregado.joggerloop.data.PulseMode
import com.abregado.joggerloop.data.TimerConfig
import com.abregado.joggerloop.data.TimerRepository
import com.abregado.joggerloop.engine.RunStatus
import com.abregado.joggerloop.engine.TimerEngine
import com.abregado.joggerloop.engine.TimerEvent
import com.abregado.joggerloop.util.formatDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * The piece that survives the lock screen. Owns a [TimerEngine], drives it with its own
 * coroutine tick loop (independent of any bound Activity), and turns the events it
 * returns into real notification updates, vibration, and tone.
 *
 * Callers can either bind and call [start]/[stop]/[reset] directly, or send an Intent
 * with one of the ACTION_* extras (used later by lock-screen notification actions,
 * which can only reach the service via PendingIntent, not a direct method call).
 */
class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var repository: TimerRepository

    private var engine: TimerEngine? = null
    private var settings: AppSettings = AppSettings()
    private var currentTimers: List<TimerConfig> = emptyList()

    private var tickJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var toneGenerator: ToneGenerator? = null

    private val _state = MutableStateFlow(TimerServiceState())
    val state: StateFlow<TimerServiceState> = _state.asStateFlow()

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        repository = TimerRepository(applicationContext)
        createNotificationChannel()

        // Load eagerly so a freshly-bound UI sees correct timers/loopCount immediately,
        // rather than showing defaults until the user taps Start once. start() still
        // reloads if idle/finished, to pick up edits made since the service was created.
        val appState = repository.load()
        currentTimers = appState.timers
        settings = appState.settings
        publishState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
            ACTION_RESET -> reset()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        tickJob?.cancel()
        releaseWakeLock()
        toneGenerator?.release()
        toneGenerator = null
        scope.cancel()
        super.onDestroy()
    }

    // --- Public control surface -------------------------------------------------------

    fun start() {
        val existing = engine
        if (existing == null || existing.status == RunStatus.IDLE || existing.status == RunStatus.FINISHED) {
            // Fresh session (or the previous one fully finished) - reload in case timers
            // or loop settings were edited since the last run.
            val appState = repository.load()
            if (appState.timers.isEmpty()) return
            currentTimers = appState.timers
            settings = appState.settings
            engine = TimerEngine(appState.timers, appState.settings.loopCount)
        }
        val runningEngine = engine ?: return

        runningEngine.start()
        acquireWakeLock()
        startForeground(NOTIFICATION_ID, buildNotification())
        startTickLoop()
        publishState()
    }

    fun stop() {
        engine?.stop()
        tickJob?.cancel()
        tickJob = null
        releaseWakeLock()
        publishState()
        updateNotification()
    }

    fun reset() {
        engine?.reset()
        tickJob?.cancel()
        tickJob = null
        releaseWakeLock()
        publishState()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun getProgress(index: Int): Float = engine?.getProgress(index) ?: 0f

    fun getRemainingMs(index: Int): Long =
        engine?.getRemainingMs(index) ?: currentTimers.getOrNull(index)?.durationMs ?: 0L

    // --- Tick loop ----------------------------------------------------------------------

    private fun startTickLoop() {
        if (tickJob?.isActive == true) return
        tickJob = scope.launch {
            var lastNotificationUpdate = 0L
            while (isActive) {
                val currentEngine = engine ?: break
                currentEngine.tick().forEach(::handleEvent)
                publishState()

                val now = System.currentTimeMillis()
                if (now - lastNotificationUpdate >= NOTIFICATION_UPDATE_INTERVAL_MS) {
                    updateNotification()
                    lastNotificationUpdate = now
                }

                if (currentEngine.status != RunStatus.RUNNING) {
                    updateNotification() // don't let the notification lag up to a second behind
                    break
                }
                delay(TICK_MS)
            }
        }
    }

    private fun handleEvent(event: TimerEvent) {
        when (event) {
            is TimerEvent.TimerAlert -> {
                if (event.timer.tone) playTone(event.timer.pulseMode)
                if (event.timer.vibrate) vibrate(event.timer.pulseMode)
            }

            TimerEvent.AllLoopsFinished -> {
                if (settings.finishTone) playFinishTone()
                if (settings.finishVibrate) vibrateFinish()
                scope.launch {
                    delay(FINISH_DISPLAY_MS)
                    reset()
                }
            }
        }
    }

    private fun publishState() {
        val currentEngine = engine
        _state.value = TimerServiceState(
            status = currentEngine?.status ?: RunStatus.IDLE,
            currentIndex = currentEngine?.currentIndex ?: 0,
            loopsRemaining = currentEngine?.loopsRemaining ?: 0,
            loopCount = settings.loopCount,
            timers = currentTimers,
            updatedAtMs = System.currentTimeMillis(),
        )
    }

    // --- Notification ---------------------------------------------------------------------

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Timer status", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Shows the current timer and remaining time while a workout is running"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val currentState = _state.value
        val timer = currentState.timers.getOrNull(currentState.currentIndex)

        val contentText = when (currentState.status) {
            RunStatus.IDLE -> "Ready to start"
            RunStatus.FINISHED -> "All loops complete"
            RunStatus.PAUSED -> "Paused"
            RunStatus.RUNNING -> {
                val label = timer?.label?.takeIf { it.isNotBlank() } ?: "Timer ${currentState.currentIndex + 1}"
                "$label — ${formatDuration(getRemainingMs(currentState.currentIndex))}"
            }
        }

        // TODO(polish): swap in a proper notification icon - this is a placeholder system glyph.
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Jogger Loop")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(currentState.status == RunStatus.RUNNING || currentState.status == RunStatus.PAUSED)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification() {
        getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, buildNotification())
    }

    // --- Vibration / tone -------------------------------------------------------------------

    private fun getVibrator(): Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Vibrator::class.java)
    }

    private fun vibrate(pulseMode: PulseMode) {
        val pattern = when (pulseMode) {
            PulseMode.SINGLE -> longArrayOf(550)
            PulseMode.TRIPLE -> longArrayOf(140, 120, 140, 120, 140)
        }
        getVibrator()?.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    private fun vibrateFinish() {
        val pattern = longArrayOf(450, 350, 450, 350, 450, 350, 450, 350, 450)
        getVibrator()?.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    private fun getToneGenerator(): ToneGenerator =
        toneGenerator ?: ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME).also { toneGenerator = it }

    // NOTE: ToneGenerator only offers fixed preset tones, not arbitrary frequencies like the
    // PWA's Web Audio oscillator (880/988/440 Hz). These presets are a placeholder that at
    // least keeps single/triple/finish distinguishable by tone, not just rhythm - exact pitch
    // parity would need bundled short WAV files played via SoundPool, deferred as polish.
    private fun playTone(pulseMode: PulseMode) {
        val generator = getToneGenerator()
        when (pulseMode) {
            PulseMode.SINGLE -> generator.startTone(ToneGenerator.TONE_PROP_BEEP, 550)
            PulseMode.TRIPLE -> {
                generator.startTone(ToneGenerator.TONE_PROP_BEEP2, 140)
                scope.launch { delay(210); generator.startTone(ToneGenerator.TONE_PROP_BEEP2, 140) }
                scope.launch { delay(420); generator.startTone(ToneGenerator.TONE_PROP_BEEP2, 140) }
            }
        }
    }

    private fun playFinishTone() {
        val generator = getToneGenerator()
        val period = 450L + 350L
        repeat(5) { i ->
            scope.launch {
                delay(i * period)
                generator.startTone(ToneGenerator.TONE_PROP_PROMPT, 450)
            }
        }
    }

    // --- Wake lock ----------------------------------------------------------------------------

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val powerManager = getSystemService(PowerManager::class.java) ?: return
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$packageName:TimerWakeLock").apply {
            setReferenceCounted(false)
            acquire(MAX_WAKE_LOCK_MS)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    companion object {
        const val ACTION_START = "com.abregado.joggerloop.action.START"
        const val ACTION_STOP = "com.abregado.joggerloop.action.STOP"
        const val ACTION_RESET = "com.abregado.joggerloop.action.RESET"

        private const val CHANNEL_ID = "timer_channel"
        private const val NOTIFICATION_ID = 1
        private const val TICK_MS = 100L
        private const val NOTIFICATION_UPDATE_INTERVAL_MS = 1000L
        private const val FINISH_DISPLAY_MS = 4000L
        private const val MAX_WAKE_LOCK_MS = 2 * 60 * 60 * 1000L // 2h safety ceiling
    }
}
