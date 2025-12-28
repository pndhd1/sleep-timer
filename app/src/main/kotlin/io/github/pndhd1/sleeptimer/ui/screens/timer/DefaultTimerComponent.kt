package io.github.pndhd1.sleeptimer.ui.screens.timer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.zacsweers.metro.*
import io.github.pndhd1.sleeptimer.domain.model.ActiveTimerData
import io.github.pndhd1.sleeptimer.domain.model.TimerSettings
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import io.github.pndhd1.sleeptimer.domain.repository.SettingsRepository
import io.github.pndhd1.sleeptimer.ui.screens.timer.TimerComponent.Child
import io.github.pndhd1.sleeptimer.ui.screens.timer.active.ActiveTimerParams
import io.github.pndhd1.sleeptimer.ui.screens.timer.active.DefaultActiveTimerComponent
import io.github.pndhd1.sleeptimer.ui.screens.timer.config.DefaultTimerConfigComponent
import io.github.pndhd1.sleeptimer.ui.screens.timer.config.TimerConfigParams
import io.github.pndhd1.sleeptimer.utils.flowWithLifecycle
import io.github.pndhd1.sleeptimer.utils.runCatchingSuspend
import io.github.pndhd1.sleeptimer.utils.toStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

@AssistedInject
class DefaultTimerComponent(
    @Assisted componentContext: ComponentContext,
    settingsRepository: SettingsRepository,
    private val activeTimerRepository: ActiveTimerRepository,
) : TimerComponent, ComponentContext by componentContext {

    @AssistedFactory
    @ContributesBinding(AppScope::class)
    fun interface Factory : TimerComponent.Factory {
        override fun create(componentContext: ComponentContext): DefaultTimerComponent
    }

    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val nav = SlotNavigation<SlotConfig>()
    private val _slot: StateFlow<ChildSlot<SlotConfig, Child>> = childSlot(
        source = nav,
        serializer = SlotConfig.serializer(),
        childFactory = ::createChild,
    ).toStateFlow()
    override val slot: StateFlow<ChildSlot<*, Child>?> get() = _slot

    init {
        combine(
            settingsRepository.timerSettings,
            activeTimerRepository.activeTimer,
            transform = ::handleTimerChange,
        )
            .catch { handleError() }
            .flowWithLifecycle(lifecycle)
            .launchIn(scope)
    }

    private fun createChild(
        config: SlotConfig,
        componentContext: ComponentContext,
    ): Child = when (config) {
        is SlotConfig.Config -> Child.Config(
            component = DefaultTimerConfigComponent(
                componentContext = componentContext,
                params = TimerConfigParams(
                    duration = config.duration,
                    presets = config.presets,
                ),
                onStartTimer = ::onStartTimer,
            ),
        )

        is SlotConfig.Active -> Child.Active(
            component = DefaultActiveTimerComponent(
                componentContext = componentContext,
                params = ActiveTimerParams(targetTime = config.targetTime),
                onStop = ::onStopTimer,
            ),
        )

        is SlotConfig.Error -> Child.Error
    }

    private fun onStartTimer(targetTime: Instant) {
        scope.launch {
            runCatchingSuspend { activeTimerRepository.startTimer(targetTime) }
                .onFailure { handleError() }
        }
    }

    private fun onStopTimer() {
        scope.launch {
            runCatchingSuspend { activeTimerRepository.clearTimer() }
                .onFailure { handleError() }
        }
    }

    private fun handleTimerChange(
        settings: TimerSettings,
        activeTimer: ActiveTimerData?,
    ) {
        val targetConfig = if (
            activeTimer != null &&
            activeTimer.targetTime >= Clock.System.now()
        ) {
            activeTimer.toActiveTimerSlot()
        } else {
            settings.toTimerConfigSlot()
        }
        nav.activate(targetConfig)
    }

    private fun handleError() {
        nav.activate(SlotConfig.Error)
    }
}

@Serializable
private sealed interface SlotConfig {

    @Serializable
    data object Error : SlotConfig

    @Serializable
    data class Config(
        val duration: Duration,
        val presets: List<Duration>,
    ) : SlotConfig

    @Serializable
    data class Active(val targetTime: Instant) : SlotConfig
}

private fun TimerSettings.toTimerConfigSlot() = SlotConfig.Config(
    duration = defaultDuration,
    presets = presets,
)

private fun ActiveTimerData.toActiveTimerSlot() = SlotConfig.Active(
    targetTime = targetTime,
)
