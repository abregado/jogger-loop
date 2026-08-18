package com.abregado.joggerloop.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abregado.joggerloop.data.PulseMode
import com.abregado.joggerloop.data.TimerConfig
import com.abregado.joggerloop.util.digitsToMs
import com.abregado.joggerloop.util.formatDigitsAsClock
import com.abregado.joggerloop.util.formatDuration
import com.abregado.joggerloop.util.msToDigits

/** One timer in the list - bottom-up progress fill mirrors the PWA's .timer-fill treatment. */
@Composable
fun TimerRow(
    timer: TimerConfig,
    index: Int,
    editMode: Boolean,
    remainingMs: Long,
    progress: Float,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onLabelChange: (String) -> Unit,
    onDurationChange: (Long) -> Unit,
    onToggleTone: () -> Unit,
    onToggleVibrate: () -> Unit,
    onSetPulseMode: (PulseMode) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), label = "timerProgress")
    var isRenaming by remember(timer.id) { mutableStateOf(false) }
    // isRenaming is local state, so exiting edit mode from the control panel (not this row)
    // wouldn't otherwise touch it, leaving the field open and inconsistent with the rest
    // of the UI having exited edit mode.
    LaunchedEffect(editMode) {
        if (!editMode) isRenaming = false
    }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            // Taller in edit mode: OutlinedTextField needs ~56dp minimum, more than the plain
            // Text rows needed. Progress is always 0 while editing (only possible when idle),
            // so the fill itself is invisible either way - safe to vary this without affecting it.
            Box(modifier = Modifier.fillMaxWidth().height(if (editMode) 96.dp else 72.dp)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .fillMaxHeight(animatedProgress)
                        .background(MaterialTheme.colorScheme.primary),
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isRenaming) {
                        // Local, not bound to `timer.label` directly: the field must not fight
                        // with the round-tripped value coming back through the service's
                        // StateFlow on every keystroke, which caused it to lose focus.
                        //
                        // Exits on the keyboard's Done action rather than onFocusChanged: a
                        // transient unfocused state during layout/keyboard-animation was
                        // enough to trip a focus listener and bounce back out before the user
                        // could type, even with nothing the user did causing it. Done is
                        // explicit and unambiguous, and unlocks safely re-adding auto-focus
                        // below since there's no listener left for it to race against.
                        var localLabel by remember(timer.id) { mutableStateOf(timer.label) }
                        val focusRequester = remember { FocusRequester() }
                        OutlinedTextField(
                            value = localLabel,
                            onValueChange = {
                                localLabel = it
                                onLabelChange(it)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { isRenaming = false }),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                        )
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    } else {
                        // Tapping the label itself opens the rename field - replaces a
                        // separate pencil button, which was one more small target crowding
                        // the edit-mode toolbar for no benefit over the label already being
                        // right there. Only active in edit mode, matching where the pencil
                        // button used to be shown.
                        Text(
                            text = timer.label.ifBlank { "Timer ${index + 1}" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .then(if (editMode) Modifier.clickable { isRenaming = true } else Modifier),
                        )
                    }

                    if (editMode) {
                        DurationField(durationMs = timer.durationMs, onDurationChange = onDurationChange)
                    } else {
                        Text(
                            text = formatDuration(remainingMs),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (editMode) {
                TimerEditControls(
                    tone = timer.tone,
                    vibrate = timer.vibrate,
                    pulseMode = timer.pulseMode,
                    onToggleTone = onToggleTone,
                    onToggleVibrate = onToggleVibrate,
                    onSetPulseMode = onSetPulseMode,
                    onDelete = onDelete,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }
        }

        if (editMode) {
            ReorderControls(
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun DurationField(
    durationMs: Long,
    onDurationChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var digits by remember { mutableStateOf(msToDigits(durationMs)) }
    var fieldValue by remember {
        val formatted = formatDigitsAsClock(digits)
        mutableStateOf(TextFieldValue(formatted, selection = TextRange(formatted.length)))
    }

    OutlinedTextField(
        value = fieldValue,
        onValueChange = { newValue ->
            val rawDigits = newValue.text.filter(Char::isDigit)
            digits = rawDigits.takeLast(4).trimStart('0').ifEmpty { "0" }
            val formatted = formatDigitsAsClock(digits)
            fieldValue = TextFieldValue(formatted, selection = TextRange(formatted.length))
            onDurationChange(digitsToMs(digits))
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.width(100.dp),
    )
}
