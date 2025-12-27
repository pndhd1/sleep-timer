package io.github.pndhd1.sleeptimer.ui.screens.timer.active

import com.arkivanov.decompose.ComponentContext

class DefaultActiveTimerComponent(
    componentContext: ComponentContext,
    private val onStop: () -> Unit,
) : ActiveTimerComponent, ComponentContext by componentContext {

    override fun onStopClick() {
        onStop()
    }
}
