package io.github.pndhd1.sleeptimer.ui.screen.settings

import com.arkivanov.decompose.ComponentContext
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pndhd1.sleeptimer.domain.model.FabAlignment
import io.github.pndhd1.sleeptimer.domain.notification.NotificationChannelManager
import io.github.pndhd1.sleeptimer.domain.repository.SettingsRepository
import io.github.pndhd1.sleeptimer.domain.repository.SystemRepository
import io.github.pndhd1.sleeptimer.utils.componentScope
import io.github.pndhd1.sleeptimer.utils.exceptions.FatalException
import io.github.pndhd1.sleeptimer.utils.runCatchingSuspend
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration

@AssistedInject
class DefaultSettingsComponent(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onNavigateToAbout: () -> Unit,
    private val settingsRepository: SettingsRepository,
    private val systemRepository: SystemRepository,
    private val notificationChannelManager: NotificationChannelManager,
) : SettingsComponent, ComponentContext by componentContext {

    @AssistedFactory
    fun interface Factory {
        fun create(
            componentContext: ComponentContext,
            onNavigateToAbout: () -> Unit,
        ): DefaultSettingsComponent
    }

    private val scope = componentScope()

    private val _state = MutableStateFlow<SettingsState>(SettingsState.Loading)
    override val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        launchWithErrorHandling {
            val settings = settingsRepository.timerSettings.first()
            _state.value = SettingsState.Loaded(
                defaultDuration = settings.defaultDuration,
                extendDuration = settings.extendDuration,
                presets = settings.presets,
                showNotification = settings.showNotification,
                hasNotificationPermission = systemRepository.canSendNotifications.value,
                fadeOut = settings.fadeOut,
                goHomeOnExpire = settings.goHomeOnExpire,
                hasFullScreenIntentPermission = systemRepository.canUseFullScreenIntent.value,
                isActionsChannelEnabled = notificationChannelManager.isActionsChannelEnabled(),
                stopMediaOnExpire = settings.stopMediaOnExpire,
                fabAlignment = settings.fabAlignment,
            )
        }
    }

    override fun onDefaultDurationChanged(duration: Duration) {
        updateLoadedState { it.copy(defaultDuration = duration) }
        launchWithErrorHandling {
            settingsRepository.updateTimerDefaultDuration(duration)
        }
    }

    override fun onExtendDurationChanged(duration: Duration) {
        updateLoadedState { it.copy(extendDuration = duration) }
        launchWithErrorHandling {
            settingsRepository.updateExtendDuration(duration)
        }
    }

    override fun onPresetAdded(duration: Duration) {
        val currentState = _state.value as? SettingsState.Loaded ?: return
        if (duration in currentState.presets) return

        val newPresets = (currentState.presets + duration).sortedBy { it.inWholeSeconds }
        updateLoadedState { it.copy(presets = newPresets) }
        launchWithErrorHandling {
            settingsRepository.updateTimerPresets(newPresets)
        }
    }

    override fun onPresetRemoved(duration: Duration) {
        val currentState = _state.value as? SettingsState.Loaded ?: return
        val newPresets = currentState.presets - duration
        updateLoadedState { it.copy(presets = newPresets) }
        launchWithErrorHandling {
            settingsRepository.updateTimerPresets(newPresets)
        }
    }

    override fun onShowNotificationChanged(show: Boolean) {
        updateLoadedState { it.copy(showNotification = show) }
        launchWithErrorHandling {
            settingsRepository.updateShowNotification(show)
        }
    }

    override fun onNotificationPermissionResult(granted: Boolean) {
        systemRepository.refreshNotificationPermissionState()
        updateLoadedState {
            it.copy(
                hasNotificationPermission = granted,
                showNotification = granted,
            )
        }
        if (granted) launchWithErrorHandling {
            settingsRepository.updateShowNotification(true)
        }
    }

    override fun getNotificationPermission(): String? = systemRepository.getNotificationPermission()

    override fun onFadeOutEnabledChanged(enabled: Boolean) {
        updateLoadedState { it.copy(fadeOut = it.fadeOut.copy(enabled = enabled)) }
        launchWithErrorHandling {
            settingsRepository.updateFadeOutEnabled(enabled)
        }
    }

    override fun onFadeOutStartBeforeChanged(duration: Duration) {
        updateLoadedState { it.copy(fadeOut = it.fadeOut.copy(startBefore = duration)) }
        launchWithErrorHandling {
            settingsRepository.updateFadeStartBefore(duration)
        }
    }

    override fun onFadeOutDurationChanged(duration: Duration) {
        updateLoadedState { it.copy(fadeOut = it.fadeOut.copy(duration = duration)) }
        launchWithErrorHandling {
            settingsRepository.updateFadeOutDuration(duration)
        }
    }

    override fun onFadeTargetVolumePercentChanged(percent: Int) {
        updateLoadedState { it.copy(fadeOut = it.fadeOut.copy(targetVolumePercent = percent)) }
        launchWithErrorHandling {
            settingsRepository.updateFadeTargetVolumePercent(percent)
        }
    }

    override fun onGoHomeOnExpireChanged(enabled: Boolean) {
        updateLoadedState { it.copy(goHomeOnExpire = enabled) }
        launchWithErrorHandling {
            settingsRepository.updateGoHomeOnExpire(enabled)
        }
    }

    override fun onFullScreenIntentPermissionResult() {
        systemRepository.refreshFullScreenIntentPermissionState()
        updateLoadedState {
            it.copy(
                hasFullScreenIntentPermission = systemRepository.canUseFullScreenIntent.value,
                isActionsChannelEnabled = notificationChannelManager.isActionsChannelEnabled(),
                goHomeOnExpire = systemRepository.canUseFullScreenIntent.value,
            )
        }
    }

    override fun getFullScreenIntentSettingsIntent() =
        systemRepository.getFullScreenIntentSettingsIntent()

    override fun getNotificationChannelSettingsIntent() =
        notificationChannelManager.getActionsChannelSettingsIntent()

    override fun onStopMediaOnExpireChanged(enabled: Boolean) {
        updateLoadedState { it.copy(stopMediaOnExpire = enabled) }
        launchWithErrorHandling {
            settingsRepository.updateStopMediaOnExpire(enabled)
        }
    }

    override fun onFabAlignmentChanged(alignment: FabAlignment) {
        updateLoadedState { it.copy(fabAlignment = alignment) }
        launchWithErrorHandling {
            settingsRepository.updateFabAlignment(alignment)
        }
    }

    override fun onAboutClick() {
        onNavigateToAbout()
    }

    private inline fun updateLoadedState(transform: (SettingsState.Loaded) -> SettingsState.Loaded) {
        _state.update { current ->
            when (current) {
                is SettingsState.Loaded -> transform(current)
                else -> current
            }
        }
    }

    private inline fun launchWithErrorHandling(crossinline block: suspend () -> Unit) {
        scope.launch {
            runCatchingSuspend { block() }
                .onFailure {
                    Firebase.crashlytics.recordException(FatalException("Settings error", it))
                    _state.value = SettingsState.Error
                }
        }
    }
}
