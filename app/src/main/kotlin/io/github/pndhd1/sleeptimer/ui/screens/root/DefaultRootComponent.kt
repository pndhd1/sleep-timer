package io.github.pndhd1.sleeptimer.ui.screens.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import dev.zacsweers.metro.*
import io.github.pndhd1.sleeptimer.ui.screens.root.RootComponent.Child
import io.github.pndhd1.sleeptimer.ui.screens.timer.TimerComponent
import io.github.pndhd1.sleeptimer.utils.toStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@AssistedInject
class DefaultRootComponent(
    @Assisted componentContext: ComponentContext,
    private val timerComponentFactory: TimerComponent.Factory,
) : RootComponent, ComponentContext by componentContext {

    @AssistedFactory
    @ContributesBinding(AppScope::class)
    fun interface Factory : RootComponent.Factory {
        override fun create(componentContext: ComponentContext): DefaultRootComponent
    }

    private val nav = StackNavigation<StackConfig>()
    private val _stack: StateFlow<ChildStack<StackConfig, Child>> = childStack(
        source = nav,
        serializer = StackConfig.serializer(),
        initialConfiguration = StackConfig.TimerConfig,
        childFactory = ::createChild,
    ).toStateFlow()
    override val stack: StateFlow<ChildStack<*, Child>> get() = _stack

    override fun onTimerTabClick() {
        nav.bringToFront(StackConfig.TimerConfig)
    }

    override fun onSettingsTabClick() {
        nav.bringToFront(StackConfig.SettingsConfig)
    }

    private fun createChild(
        config: StackConfig,
        componentContext: ComponentContext,
    ): Child = when (config) {
        StackConfig.TimerConfig -> Child.TimerChild(
            component = timerComponentFactory.create(componentContext),
        )

        StackConfig.SettingsConfig -> Child.SettingsChild
    }
}

@Serializable
private sealed interface StackConfig {

    data object TimerConfig : StackConfig

    data object SettingsConfig : StackConfig
}
