package com.abregado.joggerloop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.abregado.joggerloop.ui.theme.OnWarningContainerDark
import com.abregado.joggerloop.ui.theme.OnWarningContainerLight
import com.abregado.joggerloop.ui.theme.WarningContainerDark
import com.abregado.joggerloop.ui.theme.WarningContainerLight

/**
 * Shown proactively on first launch (Android 13+ only), before the system permission dialog,
 * so the ask doesn't come out of nowhere. The timer runs fine either way - a foreground
 * service keeps going without this permission, Android just won't display its notification
 * (confirmed via the Android docs: denying POST_NOTIFICATIONS doesn't stop the service, it
 * only hides the notification from the drawer/lock screen). This dialog exists purely to
 * explain the trade-off, not to pressure a grant.
 */
@Composable
fun NotificationPermissionRationaleDialog(onContinue: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable notifications?") },
        text = {
            Text(
                "Jogger Loop can show your running timer as a notification, including on the " +
                    "lock screen, so you can check progress without unlocking your phone. " +
                    "The timer works either way - without this permission you just won't see " +
                    "it there.",
            )
        },
        confirmButton = { TextButton(onClick = onContinue) { Text("Continue") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Not now") } },
    )
}

/**
 * Small non-blocking banner shown while notifications are off, offering a shortcut to enable
 * them - never gates starting a timer, since the timer runs fine without this permission and
 * claiming otherwise would be a dark pattern. Pill-shaped with a cautionary (not error) amber
 * background, since this is a heads-up about reduced visibility, not something broken.
 */
@Composable
fun NotificationsDisabledBanner(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val containerColor = if (isSystemInDarkTheme()) WarningContainerDark else WarningContainerLight
    val contentColor = if (isSystemInDarkTheme()) OnWarningContainerDark else OnWarningContainerLight

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Notifications are off - the timer will run, but won't show on your lock screen.",
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = onOpenSettings,
            colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
        ) { Text("Enable") }
    }
}
