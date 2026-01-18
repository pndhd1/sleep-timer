package io.github.pndhd1.sleeptimer.ui.screen.timer.active

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Instant

class DefaultActiveTimerComponent(
    componentContext: ComponentContext,
    targetTime: Instant,
    private val onStop: () -> Unit,
) : ActiveTimerComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(ActiveTimerState(targetTime = targetTime))
    override val state: StateFlow<ActiveTimerState> = _state.asStateFlow()

    override fun onStopClick() {
        onStop()
    }
}
