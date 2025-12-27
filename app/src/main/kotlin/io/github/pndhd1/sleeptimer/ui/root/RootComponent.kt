package io.github.pndhd1.sleeptimer.ui.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

interface RootComponent {

    val stack: Value<ChildStack<*, Child>>

    fun onTimerTabClick()
    fun onSettingsTabClick()

    sealed interface Child {

        class TimerChild : Child

        class SettingsChild : Child
    }
}

