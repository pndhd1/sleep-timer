package io.github.pndhd1.sleeptimer.ui.screen.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.yandex.mobile.ads.common.MobileAds
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pndhd1.sleeptimer.domain.repository.GdprRepository
import io.github.pndhd1.sleeptimer.ui.screen.about.DefaultAboutComponent
import io.github.pndhd1.sleeptimer.ui.screen.bottomnav.DefaultBottomNavComponent
import io.github.pndhd1.sleeptimer.ui.screen.root.RootComponent.Child
import io.github.pndhd1.sleeptimer.ui.screen.root.RootComponent.State
import io.github.pndhd1.sleeptimer.utils.SplashScreenStateHolder
import io.github.pndhd1.sleeptimer.utils.exceptions.GdprInitializationException
import io.github.pndhd1.sleeptimer.utils.toStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@AssistedInject
class DefaultRootComponent(
    @Assisted componentContext: ComponentContext,
    private val gdprRepository: GdprRepository,
    private val bottomNavComponentFactory: DefaultBottomNavComponent.Factory,
    private val splashScreenStateHolder: SplashScreenStateHolder,
) : RootComponent, ComponentContext by componentContext {

    @AssistedFactory
    fun interface Factory {
        fun create(componentContext: ComponentContext): DefaultRootComponent
    }

    private val scope = coroutineScope()

    private val navigation = StackNavigation<Config>()
    private val _stack: StateFlow<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.BottomNav,
        childFactory = ::createChild,
        handleBackButton = true,
    ).toStateFlow()
    override val stack: StateFlow<ChildStack<*, Child>> get() = _stack

    private val _state = MutableStateFlow<State>(State.Loading)
    override val state: StateFlow<State> get() = _state

    init {
        scope.launch {
            runCatching { checkGdprConsent() }
                .onFailure { onGdprConsentError(it) }
        }
    }

    // https://ads.yandex.com/helpcenter/en/dev/android/gdpr
    // GDPR: Pass user consent to Yandex Ads SDK
    // Must be called on every app launch after applicable user makes his choice
    private suspend fun checkGdprConsent() {
        val gdprState = gdprRepository.state.first()

        if (!gdprState.isApplicable) {
            _state.value = State.Root
            return
        }

        if (gdprState.isConsentGiven || gdprState.dialogShown) {
            MobileAds.setUserConsent(gdprState.isConsentGiven)
            _state.value = State.Root
            return
        }

        _state.value = State.GdprConsent
        splashScreenStateHolder.keepSplashScreen = false
    }

    override fun onGdprConsentResult(accepted: Boolean) {
        scope.launch {
            runCatching {
                gdprRepository.setUserConsent(accepted)
                MobileAds.setUserConsent(accepted)
                _state.value = State.Root
            }.onFailure { onGdprConsentError(it) }
        }
    }

    private fun onGdprConsentError(throwable: Throwable) {
        Firebase.crashlytics.recordException(GdprInitializationException(throwable))
        MobileAds.setUserConsent(false)
        _state.value = State.Root
    }

    override fun onBackClicked() {
        navigation.pop()
    }

    private fun onAboutClick() {
        navigation.pushNew(Config.About)
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
                onBack = ::onBackClicked,
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
