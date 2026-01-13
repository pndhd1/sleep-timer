package io.github.pndhd1.sleeptimer.ui.screens.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pndhd1.sleeptimer.domain.model.FadeOutSettings
import io.github.pndhd1.sleeptimer.domain.repository.SettingsRepository
import io.github.pndhd1.sleeptimer.utils.componentScope
import io.github.pndhd1.sleeptimer.utils.runCatchingSuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration

@AssistedInject
class DefaultSettingsComponent(
    @Assisted componentContext: ComponentContext,
    private val settingsRepository: SettingsRepository,
) : SettingsComponent, ComponentContext by componentContext {

    @AssistedFactory
    fun interface Factory {
        fun create(componentContext: ComponentContext): DefaultSettingsComponent
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
                fadeOut = settings.fadeOut,
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

    override fun onResetSettings() {
        _state.value = SettingsState.Loading
        scope.launch {
            runCatchingSuspend { settingsRepository.resetToDefaults() }
            loadSettings()
        }
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
                .onFailure { _state.value = SettingsState.Error }
        }
    }
}
