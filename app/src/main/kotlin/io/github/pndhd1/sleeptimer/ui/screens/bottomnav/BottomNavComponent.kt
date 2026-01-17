package io.github.pndhd1.sleeptimer.ui.screens.bottomnav

import com.arkivanov.decompose.router.stack.ChildStack
import io.github.pndhd1.sleeptimer.ui.screens.settings.SettingsComponent
import io.github.pndhd1.sleeptimer.ui.screens.timer.TimerComponent
import kotlinx.coroutines.flow.StateFlow

interface BottomNavComponent {

    val stack: StateFlow<ChildStack<*, Child>>

    fun onTimerTabClick()
    fun onSettingsTabClick()

    sealed interface Child {
        data class Timer(val component: TimerComponent) : Child
        data class Settings(val component: SettingsComponent) : Child
    }
}
