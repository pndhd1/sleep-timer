package io.github.pndhd1.sleeptimer.ui.screen.timer

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.hoc081098.flowext.combine
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pndhd1.sleeptimer.domain.model.ActiveTimerData
import io.github.pndhd1.sleeptimer.domain.model.TimerSettings
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import io.github.pndhd1.sleeptimer.domain.repository.SettingsRepository
import io.github.pndhd1.sleeptimer.domain.repository.SystemRepository
import io.github.pndhd1.sleeptimer.domain.usecase.ExtendTimerUseCase
import io.github.pndhd1.sleeptimer.domain.usecase.StartTimerUseCase
import io.github.pndhd1.sleeptimer.domain.usecase.StopTimerUseCase
import io.github.pndhd1.sleeptimer.ui.screen.timer.TimerComponent.Child
import io.github.pndhd1.sleeptimer.ui.screen.timer.active.DefaultActiveTimerComponent
import io.github.pndhd1.sleeptimer.ui.screen.timer.config.DefaultTimerConfigComponent
import io.github.pndhd1.sleeptimer.ui.screen.timer.permission.DefaultPermissionComponent
import io.github.pndhd1.sleeptimer.ui.screen.timer.permission.PermissionType
import io.github.pndhd1.sleeptimer.ui.service.TimerNotificationService
import io.github.pndhd1.sleeptimer.utils.*
import io.github.pndhd1.sleeptimer.utils.exceptions.FatalException
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
    systemRepository: SystemRepository,
    activeTimerRepository: ActiveTimerRepository,
    private val extendTimerUseCase: ExtendTimerUseCase,
    private val startTimerUseCase: StartTimerUseCase,
    private val stopTimerUseCase: StopTimerUseCase,
    private val permissionComponentFactory: DefaultPermissionComponent.Factory,
    private val splashScreenStateHolder: SplashScreenStateHolder,
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
            systemRepository.isAdminActive,
            systemRepository.canScheduleExactAlarms,
            systemRepository.canSendNotifications,
            systemRepository.wasNotificationPermissionRequested,
            transform = ::createSlotConfig,
        )
            .catch { handleError(it) }
            .onEach {
                nav.activate(it)
                // Hide the splash screen after the first successful load
                splashScreenStateHolder.keepSplashScreen = false
            }
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
                permissionType = config.permissionType,
            ),
        )

        is SlotConfig.Config -> Child.Config(
            component = DefaultTimerConfigComponent(
                componentContext = componentContext,
                params = DefaultTimerConfigComponent.Params(
                    defaultDuration = config.defaultDuration,
                    presets = config.presets,
                ),
                onStartTimer = ::onStartTimer,
            ),
        )

        is SlotConfig.Active -> Child.Active(
            component = DefaultActiveTimerComponent(
                componentContext = componentContext,
                targetTime = config.targetTime,
                extendDuration = config.extendDuration,
                onExtend = ::onExtendTimer,
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
        canSendNotifications: Boolean,
        wasNotificationPermissionRequested: Boolean,
    ): SlotConfig = when {
        !isAdminActive -> SlotConfig.Permission(PermissionType.DeviceAdmin)
        !canScheduleExactAlarms -> SlotConfig.Permission(PermissionType.ExactAlarm)
        !wasNotificationPermissionRequested && !canSendNotifications ->
            SlotConfig.Permission(PermissionType.Notification)

        activeTimer != null && activeTimer.targetTime >= Clock.System.now() ->
            activeTimer.toActiveTimerSlot(settings.extendDuration)

        else -> settings.toTimerConfigSlot()
    }

    private fun onStartTimer(targetTime: Instant, duration: Duration) {
        scope.launch {
            // Show error if starting the timer fails
            runCatchingSuspend {
                startTimerUseCase(targetTime, duration)
            }.onFailure {
                handleError(it)
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

    private fun onExtendTimer() {
        scope.launch {
            runCatchingSuspend { extendTimerUseCase() }.onFailure(::handleError)
        }
    }

    private fun onStopTimer() {
        scope.launch {
            // Ignore errors when stopping the notification
            runCatchingSuspend { TimerNotificationService.stop(context) }

            // Show error if stopping the timer fails
            runCatchingSuspend { stopTimerUseCase() }.onFailure(::handleError)
        }
    }

    private fun handleError(throwable: Throwable) {
        Firebase.crashlytics.recordException(FatalException("Timer error", throwable))
        nav.activate(SlotConfig.Error)
    }
}

@Serializable
private sealed interface SlotConfig {

    @Serializable
    data object Error : SlotConfig

    @Serializable
    data class Permission(val permissionType: PermissionType) : SlotConfig

    @Serializable
    data class Config(
        val defaultDuration: Duration,
        val presets: List<Duration>,
    ) : SlotConfig

    @Serializable
    data class Active(
        val targetTime: Instant,
        val extendDuration: Duration,
    ) : SlotConfig
}

private fun TimerSettings.toTimerConfigSlot() = SlotConfig.Config(
    defaultDuration = defaultDuration,
    presets = presets,
)

private fun ActiveTimerData.toActiveTimerSlot(extendDuration: Duration) = SlotConfig.Active(
    targetTime = targetTime,
    extendDuration = extendDuration,
)
