package io.github.pndhd1.sleeptimer.ui.screens.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import io.github.pndhd1.sleeptimer.ui.screens.about.AboutComponent
import io.github.pndhd1.sleeptimer.ui.screens.bottomnav.BottomNavComponent
import kotlinx.coroutines.flow.StateFlow

interface RootComponent : BackHandlerOwner {

    val stack: StateFlow<ChildStack<*, Child>>

    fun onBackClicked()

    sealed interface Child {
        data class BottomNav(val component: BottomNavComponent) : Child
        data class About(val component: AboutComponent) : Child
    }
}
