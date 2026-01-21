package io.github.pndhd1.sleeptimer.ui.screen.timer.config

import io.github.pndhd1.sleeptimer.utils.Defaults
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface TimerConfigComponent {

    val state: StateFlow<TimerConfigState>

    fun onDurationChanged(duration: Duration)
    fun onPresetSelected(duration: Duration)
    fun onStartClick()
}

data class TimerConfigState(
    val loading: Boolean,
    val duration: Duration,
    val presets: List<Duration>,
) {
    val hasTime: Boolean get() = duration >= Defaults.MinTimerDuration && duration <= Defaults.MaxTimerDuration
}
