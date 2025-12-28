package io.github.pndhd1.sleeptimer.ui.screens.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import io.github.pndhd1.sleeptimer.ui.screens.timer.TimerComponent
import kotlinx.coroutines.flow.StateFlow

interface RootComponent {

    val stack: StateFlow<ChildStack<*, Child>>

    fun onTimerTabClick()
    fun onSettingsTabClick()

    fun interface Factory {
        fun create(componentContext: ComponentContext): RootComponent
    }

    sealed interface Child {

        data class TimerChild(val component: TimerComponent) : Child

        data object SettingsChild : Child
    }
}

