package io.github.pndhd1.sleeptimer.ui.screens.timer.config

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.ui.widgets.PresetChip
import io.github.pndhd1.sleeptimer.utils.Defaults
import io.github.pndhd1.sleeptimer.utils.Formatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

// Add little delay to make to avoid jumpy transition
private const val StartButtonTransitionDelayMillis = 400

@Composable
fun TimerConfigContent(
    component: TimerConfigComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeLayout(
            state = state,
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
    onHoursChanged: (Long) -> Unit,
    onMinutesChanged: (Long) -> Unit,
    onSecondsChanged: (Long) -> Unit,
    onPresetSelected: (Duration) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TimeDisplay(state = state)

        Spacer(modifier = Modifier.height(32.dp))

        TimeInputSection(
            hours = state.hours,
            minutes = state.minutes,
            seconds = state.seconds,
            onHoursChanged = onHoursChanged,
            onMinutesChanged = onMinutesChanged,
            onSecondsChanged = onSecondsChanged,
        )

        Spacer(Modifier.height(16.dp))

        PresetButtons(
            presets = state.presets,
            onPresetSelected = onPresetSelected,
        )

        Spacer(modifier = Modifier.height(48.dp))

        StartButton(
            enabled = state.hasTime,
            loading = state.loading,
            onClick = onStartClick,
        )
    }
}

@Composable
private fun LandscapeLayout(
    state: TimerConfigState,
    onHoursChanged: (Long) -> Unit,
    onMinutesChanged: (Long) -> Unit,
    onSecondsChanged: (Long) -> Unit,
    onPresetSelected: (Duration) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                TimeDisplay(state = state)

                StartButton(
                    enabled = state.hasTime,
                    loading = state.loading,
                    onClick = onStartClick,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
}

@Composable
private fun TimeDisplay(
    state: TimerConfigState,
    modifier: Modifier = Modifier,
) {
    Text(
        text = Formatter.formatTime(state.duration),
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
private fun StartButton(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        loading,
        transitionSpec = {
            val spec = tween<Float>(delayMillis = StartButtonTransitionDelayMillis)
            fadeIn(spec)
                .togetherWith(fadeOut(spec))
                .using(SizeTransform(clip = false) { _, _ -> snap() })
        },
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) { curLoading ->
        Box(
            modifier = Modifier.requiredHeight(56.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (curLoading) {
                CircularProgressIndicator()
                return@AnimatedContent
            }

            Button(
                onClick = { if (!loading) onClick() },
                enabled = enabled,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = stringResource(R.string.button_start),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

// region Preview

private class TimerConfigStateProvider : PreviewParameterProvider<TimerConfigState> {
    override val values = sequenceOf(
        TimerConfigState(
            loading = false,
            duration = Duration.ZERO,
            presets = Defaults.DefaultPresets,
        ),
        TimerConfigState(
            loading = false,
            duration = 1.hours + 30.minutes,
            presets = Defaults.DefaultPresets,
        ),
        TimerConfigState(
            loading = true,
            duration = 15.minutes,
            presets = Defaults.DefaultPresets,
        ),
    )
}

@Preview(showBackground = true)
@PreviewScreenSizes
@Composable
private fun TimerConfigContentPreview(
    @PreviewParameter(TimerConfigStateProvider::class) state: TimerConfigState,
) {
    SleepTimerTheme {
        TimerConfigContent(
            component = PreviewTimerConfigComponent(state),
        )
    }
}

// endregion
