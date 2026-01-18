package io.github.pndhd1.sleeptimer.ui.screen.timer.active

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Instant

interface ActiveTimerComponent {

    val state: StateFlow<ActiveTimerState>

    fun onStopClick()
}

data class ActiveTimerState(
    val targetTime: Instant,
)
