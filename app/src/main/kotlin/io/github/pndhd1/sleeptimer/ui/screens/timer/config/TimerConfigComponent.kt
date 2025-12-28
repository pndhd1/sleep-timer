package io.github.pndhd1.sleeptimer.ui.screens.timer.config

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface TimerConfigComponent {

    val state: StateFlow<TimerConfigState>

    fun onHoursChanged(hours: Long)
    fun onMinutesChanged(minutes: Long)
    fun onSecondsChanged(seconds: Long)
    fun onPresetSelected(duration: Duration)
    fun onStartClick()
}

data class TimerConfigParams(
    val duration: Duration,
    val presets: List<Duration>,
)

data class TimerConfigState(
    val loading: Boolean,
    val duration: Duration,
    val presets: List<Duration>,
) {
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60
    val seconds = duration.inWholeSeconds % 60
    val hasTime: Boolean get() = duration.isPositive()
}
