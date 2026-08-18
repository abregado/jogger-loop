package com.abregado.joggerloop

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.abregado.joggerloop.service.TimerService
import com.abregado.joggerloop.ui.theme.JoggerloopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JoggerloopTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // TEMPORARY Phase 3 smoke-test controls, replaced by the real UI in Phase 4/5.
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Button(onClick = { sendServiceAction(TimerService.ACTION_START) }) {
                            Text("Start")
                        }
                        Button(onClick = { sendServiceAction(TimerService.ACTION_STOP) }) {
                            Text("Stop")
                        }
                        Button(onClick = { sendServiceAction(TimerService.ACTION_RESET) }) {
                            Text("Reset")
                        }
                    }
                }
            }
        }
    }

    private fun sendServiceAction(action: String) {
        val intent = Intent(this, TimerService::class.java).setAction(action)
        ContextCompat.startForegroundService(this, intent)
    }
}
