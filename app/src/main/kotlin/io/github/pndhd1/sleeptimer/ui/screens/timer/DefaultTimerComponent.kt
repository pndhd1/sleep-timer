package io.github.pndhd1.sleeptimer.ui.screens.timer

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pndhd1.sleeptimer.domain.model.ActiveTimerData
import io.github.pndhd1.sleeptimer.domain.model.TimerSettings
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import io.github.pndhd1.sleeptimer.domain.repository.DeviceAdminRepository
import io.github.pndhd1.sleeptimer.domain.repository.SettingsRepository
import io.github.pndhd1.sleeptimer.domain.usecase.StartTimerUseCase
import io.github.pndhd1.sleeptimer.domain.usecase.StopTimerUseCase
import io.github.pndhd1.sleeptimer.ui.screens.timer.TimerComponent.Child
import io.github.pndhd1.sleeptimer.ui.screens.timer.active.DefaultActiveTimerComponent
import io.github.pndhd1.sleeptimer.ui.screens.timer.config.DefaultTimerConfigComponent
import io.github.pndhd1.sleeptimer.ui.screens.timer.permission.DefaultPermissionComponent
import io.github.pndhd1.sleeptimer.ui.screens.timer.permission.PermissionType
import io.github.pndhd1.sleeptimer.ui.services.TimerNotificationService
import io.github.pndhd1.sleeptimer.utils.componentScope
import io.github.pndhd1.sleeptimer.utils.flowWithLifecycle
import io.github.pndhd1.sleeptimer.utils.runCatchingSuspend
import io.github.pndhd1.sleeptimer.utils.toStateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

@AssistedInject
class DefaultTimerComponent(
    @Assisted componentContext: ComponentContext,
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    deviceAdminRepository: DeviceAdminRepository,
    private val activeTimerRepository: ActiveTimerRepository,
    private val startTimerUseCase: StartTimerUseCase,
    private val stopTimerUseCase: StopTimerUseCase,
    private val permissionComponentFactory: DefaultPermissionComponent.Factory,
) : TimerComponent, ComponentContext by componentContext {

    @AssistedFactory
    fun interface Factory {
        fun create(componentContext: ComponentContext): DefaultTimerComponent
    }

    private val scope = componentScope()

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
            deviceAdminRepository.isAdminActive,
            deviceAdminRepository.canScheduleExactAlarms,
            transform = ::createSlotConfig,
        )
            .catch { handleError() }
            .onEach { nav.activate(it) }
            .flowWithLifecycle(lifecycle)
            .launchIn(scope)
    }

    private fun createChild(
        config: SlotConfig,
        componentContext: ComponentContext,
    ): Child = when (config) {
        is SlotConfig.Permission -> Child.Permission(
            component = permissionComponentFactory.create(
                componentContext = componentContext,
                permissionType = config.type,
            ),
        )

        is SlotConfig.Config -> Child.Config(
            component = DefaultTimerConfigComponent(
                componentContext = componentContext,
                params = DefaultTimerConfigComponent.Params(
                    duration = config.duration,
                    presets = config.presets,
                ),
                onStartTimer = ::onStartTimer,
            ),
        )

        is SlotConfig.Active -> Child.Active(
            component = DefaultActiveTimerComponent(
                componentContext = componentContext,
                targetTime = config.targetTime,
                onStop = ::onStopTimer,
            ),
        )

        is SlotConfig.Error -> Child.Error
    }

    private fun createSlotConfig(
        settings: TimerSettings,
        activeTimer: ActiveTimerData?,
        isAdminActive: Boolean,
        canScheduleExactAlarms: Boolean,
    ): SlotConfig = when {
        !isAdminActive -> SlotConfig.Permission(PermissionType.DeviceAdmin)
        !canScheduleExactAlarms -> SlotConfig.Permission(PermissionType.ExactAlarm)

        activeTimer != null && activeTimer.targetTime >= Clock.System.now() ->
            activeTimer.toActiveTimerSlot()

        else -> settings.toTimerConfigSlot()
    }

    private fun onStartTimer(targetTime: Instant, duration: Duration) {
        scope.launch {
            // Show error if starting the timer fails
            runCatchingSuspend {
                startTimerUseCase(targetTime, duration)
            }.onFailure {
                handleError()
                return@launch
            }

            // Ignore errors when starting the notification
            val showNotification = runCatchingSuspend {
                settingsRepository.timerSettings.first().showNotification
            }.fold(
                onSuccess = { it },
                onFailure = { false }
            )
            if (showNotification) runCatchingSuspend { TimerNotificationService.start(context) }
        }
    }

    private fun onStopTimer() {
        scope.launch {
            // Ignore errors when stopping the notification
            runCatchingSuspend { TimerNotificationService.stop(context) }

            // Show error if stopping the timer fails
            runCatchingSuspend { stopTimerUseCase() }
                .onFailure { handleError() }
        }
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
    data class Permission(val type: PermissionType) : SlotConfig

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
