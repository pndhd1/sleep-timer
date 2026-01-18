package io.github.pndhd1.sleeptimer.ui.screens.bottomnav

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pndhd1.sleeptimer.ui.screens.bottomnav.BottomNavComponent.Child
import io.github.pndhd1.sleeptimer.ui.screens.settings.DefaultSettingsComponent
import io.github.pndhd1.sleeptimer.ui.screens.timer.DefaultTimerComponent
import io.github.pndhd1.sleeptimer.utils.toStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@AssistedInject
class DefaultBottomNavComponent(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onNavigateToAbout: () -> Unit,
    private val timerComponentFactory: DefaultTimerComponent.Factory,
    private val settingsComponentFactory: DefaultSettingsComponent.Factory,
) : BottomNavComponent, ComponentContext by componentContext {

    @AssistedFactory
    fun interface Factory {
        fun create(
            componentContext: ComponentContext,
            onNavigateToAbout: () -> Unit,
        ): DefaultBottomNavComponent
    }

    private val navigation = StackNavigation<Config>()
    private val _stack: StateFlow<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Timer,
        childFactory = ::createChild,
        handleBackButton = true,
    ).toStateFlow()
    override val stack: StateFlow<ChildStack<*, Child>> get() = _stack

    override fun onBackClicked() {
        navigation.pop()
    }

    override fun onTimerTabClick() {
        navigation.replaceAll(Config.Timer)
    }

    override fun onSettingsTabClick() {
        navigation.pushToFront(Config.Settings)
    }

    private fun createChild(
        config: Config,
        componentContext: ComponentContext,
    ): Child = when (config) {
        Config.Timer -> Child.Timer(
            component = timerComponentFactory.create(componentContext),
        )

        Config.Settings -> Child.Settings(
            component = settingsComponentFactory.create(
                componentContext = componentContext,
                onNavigateToAbout = onNavigateToAbout,
            ),
        )
    }
}

@Serializable
private sealed interface Config {
    @Serializable
    data object Timer : Config

    @Serializable
    data object Settings : Config
}
