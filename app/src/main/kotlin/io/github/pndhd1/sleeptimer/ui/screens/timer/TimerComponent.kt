package io.github.pndhd1.sleeptimer.ui.screens.timer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import io.github.pndhd1.sleeptimer.ui.screens.timer.active.ActiveTimerComponent
import io.github.pndhd1.sleeptimer.ui.screens.timer.config.TimerConfigComponent
import kotlinx.coroutines.flow.StateFlow

interface TimerComponent {

    val slot: StateFlow<ChildSlot<*, Child>?>

    fun interface Factory {
        fun create(componentContext: ComponentContext): TimerComponent
    }

    sealed interface Child {

        data class Config(val component: TimerConfigComponent) : Child

        data class Active(val component: ActiveTimerComponent) : Child
    }
}

