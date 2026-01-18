package io.github.pndhd1.sleeptimer.ui.screen.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import io.github.pndhd1.sleeptimer.ui.screen.about.AboutComponent
import io.github.pndhd1.sleeptimer.ui.screen.bottomnav.BottomNavComponent
import kotlinx.coroutines.flow.StateFlow

interface RootComponent : BackHandlerOwner {

    val stack: StateFlow<ChildStack<*, Child>>

    val state: StateFlow<State>

    fun onBackClicked()

    fun onGdprConsentResult(accepted: Boolean)

    sealed interface State {
        data class Root(val showGdprDialog: Boolean) : State
        data object Error : State
    }

    sealed interface Child {
        data class BottomNav(val component: BottomNavComponent) : Child
        data class About(val component: AboutComponent) : Child
    }
}
