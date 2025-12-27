package io.github.pndhd1.sleeptimer.ui.timer.config

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.pndhd1.sleeptimer.R
import java.util.Locale
import kotlin.time.Duration

private const val TimeFormat = "%d:%02d:%02d"

@Composable
fun TimerConfigContent(
    component: TimerConfigComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeLayout(
            state = state,
            locale = locale,
            onHoursChanged = component::onHoursChanged,
            onMinutesChanged = component::onMinutesChanged,
            onSecondsChanged = component::onSecondsChanged,
            onPresetSelected = component::onPresetSelected,
            onStartClick = component::onStartClick,
            modifier = modifier,
        )
    } else {
        PortraitLayout(
            state = state,
            locale = locale,
            onHoursChanged = component::onHoursChanged,
            onMinutesChanged = component::onMinutesChanged,
            onSecondsChanged = component::onSecondsChanged,
            onPresetSelected = component::onPresetSelected,
            onStartClick = component::onStartClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun PortraitLayout(
    state: TimerConfigState,
    locale: Locale,
    onHoursChanged: (Long) -> Unit,
    onMinutesChanged: (Long) -> Unit,
    onSecondsChanged: (Long) -> Unit,
    onPresetSelected: (Duration) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        TimeDisplay(
            state = state,
            locale = locale,
        )

        TimeInputSection(
            hours = state.hours,
            minutes = state.minutes,
            seconds = state.seconds,
            onHoursChanged = onHoursChanged,
            onMinutesChanged = onMinutesChanged,
            onSecondsChanged = onSecondsChanged,
        )

        PresetButtons(
            presets = state.presets,
            onPresetSelected = onPresetSelected,
        )

        StartButton(
            enabled = state.hasTime,
            onClick = onStartClick,
        )
    }
}

@Composable
private fun LandscapeLayout(
    state: TimerConfigState,
    locale: Locale,
    onHoursChanged: (Long) -> Unit,
    onMinutesChanged: (Long) -> Unit,
    onSecondsChanged: (Long) -> Unit,
    onPresetSelected: (Duration) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TimeDisplay(
                state = state,
                locale = locale,
            )

            StartButton(
                enabled = state.hasTime,
                onClick = onStartClick,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TimeInputSection(
                hours = state.hours,
                minutes = state.minutes,
                seconds = state.seconds,
                onHoursChanged = onHoursChanged,
                onMinutesChanged = onMinutesChanged,
                onSecondsChanged = onSecondsChanged,
            )

            PresetButtons(
                presets = state.presets,
                onPresetSelected = onPresetSelected,
            )
        }
    }
}

@Composable
private fun TimeDisplay(
    state: TimerConfigState,
    locale: Locale,
    modifier: Modifier = Modifier,
) {
    val displayTime = remember(state.duration, locale) {
        String.format(locale, TimeFormat, state.hours, state.minutes, state.seconds)
    }

    Text(
        text = displayTime,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
private fun TimeInputSection(
    hours: Long,
    minutes: Long,
    seconds: Long,
    onHoursChanged: (Long) -> Unit,
    onMinutesChanged: (Long) -> Unit,
    onSecondsChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimeInputField(
            value = hours,
            onValueChanged = onHoursChanged,
            label = stringResource(R.string.label_hours),
            maxValue = 23,
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
        )

        TimeInputField(
            value = minutes,
            onValueChanged = onMinutesChanged,
            label = stringResource(R.string.label_minutes),
            maxValue = 59,
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
        )

        TimeInputField(
            value = seconds,
            onValueChanged = onSecondsChanged,
            label = stringResource(R.string.label_seconds),
            maxValue = 59,
        )
    }
}

@Composable
private fun TimeInputField(
    value: Long,
    onValueChanged: (Long) -> Unit,
    label: String,
    maxValue: Long,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() }.take(2)
            val longValue = filtered.toLongOrNull() ?: 0
            onValueChanged(longValue.coerceIn(0, maxValue))
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            textAlign = TextAlign.Center,
        ),
        modifier = modifier.width(100.dp),
    )
}

@Composable
private fun PresetButtons(
    presets: List<Duration>,
    onPresetSelected: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        presets.forEach { duration ->
            PresetChip(
                duration = duration,
                onClick = { onPresetSelected(duration) },
            )
        }
    }
}

@Composable
private fun PresetChip(
    duration: Duration,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = if (duration.inWholeHours > 0) {
        stringResource(R.string.preset_hours, duration.inWholeHours.toInt())
    } else {
        stringResource(R.string.preset_minutes, duration.inWholeMinutes.toInt())
    }

    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
    )
}

@Composable
private fun StartButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
    ) {
        Text(
            text = stringResource(R.string.button_start),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
