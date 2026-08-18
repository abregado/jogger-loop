package com.abregado.joggerloop

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.abregado.joggerloop.service.TimerService
import com.abregado.joggerloop.ui.MainScreen
import com.abregado.joggerloop.ui.NotificationPermissionRationaleDialog
import com.abregado.joggerloop.ui.theme.AppColorScheme
import com.abregado.joggerloop.ui.theme.JoggerloopTheme

class MainActivity : ComponentActivity() {

    private var timerService by mutableStateOf<TimerService?>(null)
    private var notificationsPermitted by mutableStateOf(true)
    private var showPermissionRationale by mutableStateOf(false)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            notificationsPermitted = granted
        }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            timerService = (binder as TimerService.LocalBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notificationsPermitted = hasNotificationPermission()
        // Only offered once, on first launch - re-showing it on every return to the app would
        // be noisy for a user who already made their choice. A later start attempt while still
        // denied is handled separately by MainScreen's NotificationsRequiredDialog.
        showPermissionRationale = requiresNotificationPermission() && !notificationsPermitted

        setContent {
            // Collected here (separately from MainScreen's own collection of the same
            // StateFlow) so the theme wrapping the whole screen - including this file's own
            // dialogs - reacts to a scheme change immediately, not just the content inside
            // MainScreen.
            var colorScheme by remember { mutableStateOf(AppColorScheme.Default) }
            LaunchedEffect(timerService) {
                timerService?.state?.collect { colorScheme = AppColorScheme.fromName(it.colorScheme) }
            }

            JoggerloopTheme(colorScheme = colorScheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        service = timerService,
                        notificationsPermitted = notificationsPermitted,
                        onOpenNotificationSettings = ::openNotificationSettings,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                if (showPermissionRationale) {
                    NotificationPermissionRationaleDialog(
                        onContinue = {
                            showPermissionRationale = false
                            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        onDismiss = { showPermissionRationale = false },
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, TimerService::class.java), connection, Context.BIND_AUTO_CREATE)
        // Picks up a grant made from the system Settings screen while the app was backgrounded.
        notificationsPermitted = hasNotificationPermission()
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }

    private fun requiresNotificationPermission() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private fun hasNotificationPermission(): Boolean {
        if (!requiresNotificationPermission()) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }
}
