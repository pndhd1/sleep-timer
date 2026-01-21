package io.github.pndhd1.sleeptimer.ui.screen.timer.config

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class DefaultTimerConfigComponent(
    componentContext: ComponentContext,
    params: Params,
    private val onStartTimer: (targetTime: Instant, duration: Duration) -> Unit,
) : TimerConfigComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(
        TimerConfigState(
            loading = false,
            duration = params.duration,
            presets = params.presets,
        )
    )
    override val state: StateFlow<TimerConfigState> = _state.asStateFlow()

    override fun onDurationChanged(duration: Duration) {
        _state.update { it.copy(duration = duration) }
    }

    override fun onPresetSelected(duration: Duration) {
        _state.update { it.copy(duration = duration) }
    }

    override fun onStartClick() {
        val currentState = _state.updateAndGet { it.copy(loading = true) }
        val targetTime = Clock.System.now() + currentState.duration
        if (currentState.hasTime) onStartTimer(targetTime, currentState.duration)
    }

    data class Params(
        val duration: Duration,
        val presets: List<Duration>,
    )
}
