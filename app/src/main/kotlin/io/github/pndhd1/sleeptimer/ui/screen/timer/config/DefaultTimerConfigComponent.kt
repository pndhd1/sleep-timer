package io.github.pndhd1.sleeptimer.ui.screen.timer.config

import com.arkivanov.decompose.ComponentContext
import io.github.pndhd1.sleeptimer.domain.model.FabAlignment
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
            duration = params.defaultDuration,
            defaultDuration = params.defaultDuration,
            presets = params.presets,
            fabAlignment = params.fabAlignment,
            isCustomExpanded = false,
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

    override fun onCustomExpandedChanged(expanded: Boolean) {
        _state.update { it.copy(isCustomExpanded = expanded) }
    }

    data class Params(
        val defaultDuration: Duration,
        val presets: List<Duration>,
        val fabAlignment: FabAlignment,
    )
}
