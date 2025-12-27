package io.github.pndhd1.sleeptimer.ui.timer

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import io.github.pndhd1.sleeptimer.ui.timer.active.ActiveTimerComponent
import io.github.pndhd1.sleeptimer.ui.timer.config.TimerConfigComponent

interface TimerComponent {

    val state: Value<TimerState>

    sealed interface TimerState {

        data object Loading : TimerState

        data class Content(
            val childSlot: ChildSlot<*, Child>,
        ) : TimerState
    }

    sealed interface Child {

        data class Config(val component: TimerConfigComponent) : Child

        data class Active(val component: ActiveTimerComponent) : Child
    }
}

