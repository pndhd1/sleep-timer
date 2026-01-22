package io.github.pndhd1.sleeptimer.ui.screen.settings

import android.content.Intent
import io.github.pndhd1.sleeptimer.domain.model.FadeOutSettings
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface SettingsComponent {

    val state: StateFlow<SettingsState>

    fun onDefaultDurationChanged(duration: Duration)
    fun onExtendDurationChanged(duration: Duration)
    fun onPresetAdded(duration: Duration)
    fun onPresetRemoved(duration: Duration)
    fun onShowNotificationChanged(show: Boolean)
    fun onNotificationPermissionResult(granted: Boolean)
    fun getNotificationPermission(): String?
    fun onFadeOutEnabledChanged(enabled: Boolean)
    fun onFadeOutStartBeforeChanged(duration: Duration)
    fun onFadeOutDurationChanged(duration: Duration)
    fun onFadeTargetVolumePercentChanged(percent: Int)
    fun onGoHomeOnExpireChanged(enabled: Boolean)
    fun onFullScreenIntentPermissionResult()
    fun getFullScreenIntentSettingsIntent(): Intent?
    fun getNotificationChannelSettingsIntent(): Intent
    fun onStopMediaOnExpireChanged(enabled: Boolean)
    fun onAboutClick()
}

sealed interface SettingsState {

    data object Loading : SettingsState

    data object Error : SettingsState

    data class Loaded(
        val defaultDuration: Duration,
        val extendDuration: Duration,
        val presets: List<Duration>,
        val showNotification: Boolean,
        val hasNotificationPermission: Boolean,
        val fadeOut: FadeOutSettings,
        val goHomeOnExpire: Boolean,
        val hasFullScreenIntentPermission: Boolean,
        val isActionsChannelEnabled: Boolean,
        val stopMediaOnExpire: Boolean,
    ) : SettingsState
}
