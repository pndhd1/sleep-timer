package io.github.pndhd1.sleeptimer.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pndhd1.sleeptimer.utils.Defaults
import io.github.pndhd1.sleeptimer.utils.Formatter
import kotlin.math.round
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun DurationSlider(
    duration: Duration,
    onDurationChanged: (Duration) -> Unit,
    minDuration: Duration,
    maxDuration: Duration,
    modifier: Modifier = Modifier,
    step: Duration? = null,
    dialogMaxDuration: Duration = Defaults.MaxTimerDuration,
    dialogMinDuration: Duration = minDuration,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val minSeconds = minDuration.inWholeSeconds.toFloat()
    val maxSeconds = maxDuration.inWholeSeconds.toFloat()
    val stepSeconds = step?.inWholeSeconds?.toInt() ?: 1

    var sliderValue by remember(duration) {
        mutableFloatStateOf(duration.inWholeSeconds.toFloat())
    }

    // Show slider value during drag, otherwise show actual duration
    val sliderSeconds = sliderValue.toInt()
    val durationSeconds = duration.inWholeSeconds.toInt()
    val displayDuration = if (sliderSeconds != durationSeconds) {
        sliderSeconds.seconds
    } else {
        duration
    }

    // Calculate steps so values are always multiples of step (0, step, 2*step, ...)
    val steps = if (stepSeconds > 0) {
        (maxSeconds / stepSeconds).toInt() - 1
    } else {
        0
    }


    // Round value to nearest step multiple
    fun roundToStep(value: Float): Float {
        if (stepSeconds <= 0) return value
        return (round(value / stepSeconds) * stepSeconds).coerceIn(minSeconds, maxSeconds)
    }

    Column(modifier = modifier) {
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(
                text = Formatter.formatTime(displayDuration),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = sliderValue.coerceIn(0f, maxSeconds),
            onValueChange = { sliderValue = roundToStep(it) },
            onValueChangeFinished = { onDurationChanged(sliderValue.toInt().seconds) },
            valueRange = 0f..maxSeconds,
            steps = steps,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = Formatter.formatTimeWithUnits(minDuration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = Formatter.formatTimeWithUnits(maxDuration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showDialog) {
        DurationEditDialog(
            currentDuration = duration,
            maxDuration = dialogMaxDuration,
            minDuration = dialogMinDuration,
            onConfirm = { newDuration ->
                onDurationChanged(newDuration)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }
}
