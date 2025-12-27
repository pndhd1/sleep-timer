package io.github.pndhd1.sleeptimer.ui.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import io.github.pndhd1.sleeptimer.ui.timer.TimerComponent

interface RootComponent {

    val stack: Value<ChildStack<*, Child>>

    fun onTimerTabClick()
    fun onSettingsTabClick()

    sealed interface Child {

        data class TimerChild(val component: TimerComponent) : Child

        data object SettingsChild : Child
    }
}

