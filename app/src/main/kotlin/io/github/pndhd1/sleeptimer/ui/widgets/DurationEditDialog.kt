package io.github.pndhd1.sleeptimer.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.utils.Defaults
import io.github.pndhd1.sleeptimer.utils.Formatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun DurationEditDialog(
    currentDuration: Duration,
    onConfirm: (Duration) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.settings_edit_duration_title),
    maxDuration: Duration = Defaults.MaxTimerDuration,
    minDuration: Duration = Defaults.MinTimerDuration,
) {
    var hours by rememberSaveable { mutableStateOf(currentDuration.inWholeHours.toString()) }
    var minutes by rememberSaveable { mutableStateOf((currentDuration.inWholeMinutes % 60).toString()) }
    var seconds by rememberSaveable { mutableStateOf((currentDuration.inWholeSeconds % 60).toString()) }

    val parsedHours = hours.toIntOrNull() ?: 0
    val parsedMinutes = minutes.toIntOrNull() ?: 0
    val parsedSeconds = seconds.toIntOrNull() ?: 0
    val totalDuration = parsedHours.hours + parsedMinutes.minutes + parsedSeconds.seconds
    val isTooShort = totalDuration < minDuration
    val isTooLong = totalDuration > maxDuration
    val isValid = !isTooShort && !isTooLong

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { newValue ->
                            hours = newValue.filter { it.isDigit() }.take(2)
                        },
                        label = { Text(stringResource(R.string.label_hours_short), maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(72.dp),
                    )

                    Text(":", style = MaterialTheme.typography.headlineMedium)

                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { newValue ->
                            minutes = newValue.filter { it.isDigit() }.take(2)
                        },
                        label = {
                            Text(
                                stringResource(R.string.label_minutes_short),
                                maxLines = 1
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(72.dp),
                    )

                    Text(":", style = MaterialTheme.typography.headlineMedium)

                    OutlinedTextField(
                        value = seconds,
                        onValueChange = { newValue ->
                            seconds = newValue.filter { it.isDigit() }.take(2)
                        },
                        label = {
                            Text(
                                stringResource(R.string.label_seconds_short),
                                maxLines = 1
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(72.dp),
                    )
                }

                if (!isValid) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            isTooShort -> stringResource(
                                R.string.settings_min_duration_error,
                                Formatter.formatTime(minDuration),
                            )

                            else -> stringResource(
                                R.string.settings_max_duration_error,
                                Formatter.formatTime(maxDuration),
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(totalDuration) },
                enabled = isValid,
            ) {
                Text(stringResource(R.string.settings_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    )
}
