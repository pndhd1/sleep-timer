package io.github.pndhd1.sleeptimer.ui.screen.timer.active

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class PreviewActiveTimerComponent(
    remainingDuration: Duration,
    extendDuration: Duration = 5.minutes,
) : ActiveTimerComponent {
    override val state: StateFlow<ActiveTimerState> = MutableStateFlow(
        ActiveTimerState(
            targetTime = Clock.System.now() + remainingDuration,
            extendDuration = extendDuration,
        )
    )
    override fun onExtendClick() = Unit
    override fun onStopClick() = Unit
}
