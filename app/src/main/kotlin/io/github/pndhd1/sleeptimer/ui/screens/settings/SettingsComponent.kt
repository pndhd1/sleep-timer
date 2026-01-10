package io.github.pndhd1.sleeptimer.ui.screens.settings

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface SettingsComponent {

    val state: StateFlow<SettingsState>

    fun onDefaultDurationChanged(duration: Duration)
    fun onExtendDurationChanged(duration: Duration)
    fun onPresetAdded(duration: Duration)
    fun onPresetRemoved(duration: Duration)
    fun onResetSettings()
}

sealed interface SettingsState {

    data object Loading : SettingsState

    data object Error : SettingsState

    data class Loaded(
        val defaultDuration: Duration,
        val extendDuration: Duration,
        val presets: List<Duration>,
    ) : SettingsState
}
