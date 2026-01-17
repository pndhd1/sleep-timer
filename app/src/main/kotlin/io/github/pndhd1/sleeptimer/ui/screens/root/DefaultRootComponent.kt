package io.github.pndhd1.sleeptimer.ui.screens.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pndhd1.sleeptimer.ui.screens.about.DefaultAboutComponent
import io.github.pndhd1.sleeptimer.ui.screens.bottomnav.DefaultBottomNavComponent
import io.github.pndhd1.sleeptimer.ui.screens.root.RootComponent.Child
import io.github.pndhd1.sleeptimer.utils.toStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@AssistedInject
class DefaultRootComponent(
    @Assisted componentContext: ComponentContext,
    private val bottomNavComponentFactory: DefaultBottomNavComponent.Factory,
) : RootComponent, ComponentContext by componentContext {

    @AssistedFactory
    fun interface Factory {
        fun create(componentContext: ComponentContext): DefaultRootComponent
    }

    private val navigation = StackNavigation<Config>()
    private val _stack: StateFlow<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.BottomNav,
        childFactory = ::createChild,
        handleBackButton = true,
    ).toStateFlow()
    override val stack: StateFlow<ChildStack<*, Child>> get() = _stack

    private fun onAboutClick() {
        navigation.pushNew(Config.About)
    }

    private fun onBack() {
        navigation.pop()
    }

    private fun createChild(
        config: Config,
        componentContext: ComponentContext,
    ): Child = when (config) {
        Config.BottomNav -> Child.BottomNav(
            component = bottomNavComponentFactory.create(
                componentContext = componentContext,
                onNavigateToAbout = ::onAboutClick,
            ),
        )

        Config.About -> Child.About(
            component = DefaultAboutComponent(
                componentContext = componentContext,
                onBack = ::onBack,
            ),
        )
    }
}

@Serializable
private sealed interface Config {
    @Serializable
    data object BottomNav : Config

    @Serializable
    data object About : Config
}
