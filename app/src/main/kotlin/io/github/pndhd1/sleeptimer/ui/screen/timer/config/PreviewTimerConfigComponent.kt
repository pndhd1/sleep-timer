package io.github.pndhd1.sleeptimer.ui.screen.timer.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

class PreviewTimerConfigComponent(
    initialState: TimerConfigState,
) : TimerConfigComponent {
    override val state: StateFlow<TimerConfigState> = MutableStateFlow(initialState)
    override fun onDurationChanged(duration: Duration) = Unit
    override fun onPresetSelected(duration: Duration) = Unit
    override fun onStartClick() = Unit
}
