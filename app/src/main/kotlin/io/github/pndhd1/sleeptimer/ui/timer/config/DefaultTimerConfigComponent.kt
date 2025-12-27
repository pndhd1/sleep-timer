package io.github.pndhd1.sleeptimer.ui.timer.config

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DefaultTimerConfigComponent(
    componentContext: ComponentContext,
    params: TimerConfigParams,
    private val onStartTimer: (duration: Duration) -> Unit,
) : TimerConfigComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(
        TimerConfigState(
            duration = params.duration,
            presets = params.presets,
        )
    )
    override val state: StateFlow<TimerConfigState> = _state.asStateFlow()

    override fun onHoursChanged(hours: Long) {
        _state.update { state ->
            val newDuration =
                hours.coerceIn(0, 23).hours + state.minutes.minutes + state.seconds.seconds
            state.copy(duration = newDuration)
        }
    }

    override fun onMinutesChanged(minutes: Long) {
        _state.update { state ->
            val newDuration =
                state.hours.hours + minutes.coerceIn(0, 59).minutes + state.seconds.seconds
            state.copy(duration = newDuration)
        }
    }

    override fun onSecondsChanged(seconds: Long) {
        _state.update { state ->
            val newDuration =
                state.hours.hours + state.minutes.minutes + seconds.coerceIn(0, 59).seconds
            state.copy(duration = newDuration)
        }
    }

    override fun onPresetSelected(duration: Duration) {
        _state.update { it.copy(duration = duration) }
    }

    override fun onStartClick() {
        val currentState = _state.value
        if (currentState.hasTime) {
            onStartTimer(currentState.duration)
        }
    }
}
