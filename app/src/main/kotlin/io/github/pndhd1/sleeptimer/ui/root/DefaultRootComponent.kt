package io.github.pndhd1.sleeptimer.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import io.github.pndhd1.sleeptimer.ui.root.RootComponent.Child
import kotlinx.serialization.Serializable

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {

    private val nav = StackNavigation<Config>()

    private val _stack: Value<ChildStack<Config, Child>> = childStack(
        source = nav,
        serializer = Config.serializer(),
        initialConfiguration = Config.TimerConfig,
        childFactory = ::createChild,
    )
    override val stack: Value<ChildStack<*, Child>> get() = _stack

    override fun onTimerTabClick() {
        nav.bringToFront(Config.TimerConfig)
    }

    override fun onSettingsTabClick() {
        nav.bringToFront(Config.SettingsConfig)
    }

    private fun createChild(config: Config, componentContext: ComponentContext): Child =
        when (config) {
            Config.TimerConfig -> Child.TimerChild()
            Config.SettingsConfig -> Child.SettingsChild()
        }


    @Serializable
    private sealed interface Config {

        data object TimerConfig : Config

        data object SettingsConfig : Config
    }
}
