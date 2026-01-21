package io.github.pndhd1.sleeptimer.ui.screen.timer.config

import androidx.compose.animation.*
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.ui.widgets.DurationSlider
import io.github.pndhd1.sleeptimer.ui.widgets.PresetChip
import io.github.pndhd1.sleeptimer.utils.Defaults
import io.github.pndhd1.sleeptimer.utils.isPortrait
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

// Add little delay to make to avoid jumpy transition
private const val StartButtonTransitionDelayMillis = 400

private val TimerSliderMin = 5.seconds
private val TimerSliderMax = 90.minutes
private val TimerSliderStep = 1.minutes

@Composable
fun TimerConfigContent(
    component: TimerConfigComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsStateWithLifecycle()
    if (isPortrait()) {
        PortraitLayout(
            state = state,
            onDurationChanged = component::onDurationChanged,
            onPresetSelected = component::onPresetSelected,
            onStartClick = component::onStartClick,
            modifier = modifier,
        )
    } else {
        LandscapeLayout(
            state = state,
            onDurationChanged = component::onDurationChanged,
            onPresetSelected = component::onPresetSelected,
            onStartClick = component::onStartClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun PortraitLayout(
    state: TimerConfigState,
    onDurationChanged: (Duration) -> Unit,
    onPresetSelected: (Duration) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        DurationSlider(
            duration = state.duration,
            onDurationChanged = onDurationChanged,
            minDuration = TimerSliderMin,
            maxDuration = TimerSliderMax,
            step = TimerSliderStep,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        )

        Spacer(Modifier.height(24.dp))

        PresetButtons(
            presets = state.presets,
            onPresetSelected = onPresetSelected,
        )

        Spacer(modifier = Modifier.height(24.dp))

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
    onDurationChanged: (Duration) -> Unit,
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
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
            ) {
                DurationSlider(
                    duration = state.duration,
                    onDurationChanged = onDurationChanged,
                    minDuration = TimerSliderMin,
                    maxDuration = TimerSliderMax,
                    step = TimerSliderStep,
                    modifier = Modifier.fillMaxWidth(),
                )

                PresetButtons(
                    presets = state.presets,
                    onPresetSelected = onPresetSelected,
                )
            }

            StartButton(
                enabled = state.hasTime,
                loading = state.loading,
                onClick = onStartClick,
            )
        }
    }
}

@Composable
private fun PresetButtons(
    presets: List<Duration>,
    onPresetSelected: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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
