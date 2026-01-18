package io.github.pndhd1.sleeptimer.ui.screen.timer.active

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import kotlin.time.Duration

class PreviewActiveTimerComponent(
    remainingDuration: Duration,
) : ActiveTimerComponent {
    override val state: StateFlow<ActiveTimerState> = MutableStateFlow(
        ActiveTimerState(targetTime = Clock.System.now() + remainingDuration)
    )
    override fun onStopClick() = Unit
}
