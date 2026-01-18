package io.github.pndhd1.sleeptimer.ui.screens.bottomnav

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import io.github.pndhd1.sleeptimer.ui.screens.settings.SettingsComponent
import io.github.pndhd1.sleeptimer.ui.screens.timer.TimerComponent
import kotlinx.coroutines.flow.StateFlow

interface BottomNavComponent : BackHandlerOwner {

    val stack: StateFlow<ChildStack<*, Child>>

    fun onBackClicked()
    fun onTimerTabClick()
    fun onSettingsTabClick()

    sealed interface Child {
        data class Timer(val component: TimerComponent) : Child
        data class Settings(val component: SettingsComponent) : Child
    }
}
