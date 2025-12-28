package io.github.pndhd1.sleeptimer.ui.screens.timer.active

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultActiveTimerComponent(
    componentContext: ComponentContext,
    params: ActiveTimerParams,
    private val onStop: () -> Unit,
) : ActiveTimerComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(ActiveTimerState(targetTime = params.targetTime))
    override val state: StateFlow<ActiveTimerState> = _state.asStateFlow()

    override fun onStopClick() {
        onStop()
    }
}
