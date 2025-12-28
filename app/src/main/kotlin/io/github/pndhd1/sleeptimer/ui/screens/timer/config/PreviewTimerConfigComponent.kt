package io.github.pndhd1.sleeptimer.ui.screens.timer.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

class PreviewTimerConfigComponent(
    initialState: TimerConfigState,
) : TimerConfigComponent {
    override val state: StateFlow<TimerConfigState> = MutableStateFlow(initialState)
    override fun onHoursChanged(hours: Long) = Unit
    override fun onMinutesChanged(minutes: Long) = Unit
    override fun onSecondsChanged(seconds: Long) = Unit
    override fun onPresetSelected(duration: Duration) = Unit
    override fun onStartClick() = Unit
}
