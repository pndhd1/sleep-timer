package io.github.pndhd1.sleeptimer.ui.screens.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

class PreviewSettingsComponent(
    initialState: SettingsState,
) : SettingsComponent {
    override val state: StateFlow<SettingsState> = MutableStateFlow(initialState)
    override fun onDefaultDurationChanged(duration: Duration) = Unit
    override fun onExtendDurationChanged(duration: Duration) = Unit
    override fun onPresetAdded(duration: Duration) = Unit
    override fun onPresetRemoved(duration: Duration) = Unit
    override fun onShowNotificationChanged(show: Boolean) = Unit
    override fun onNotificationPermissionResult(granted: Boolean) = Unit
    override fun getNotificationPermission(): String? = null
    override fun onFadeOutEnabledChanged(enabled: Boolean) = Unit
    override fun onFadeOutStartBeforeChanged(duration: Duration) = Unit
    override fun onFadeOutDurationChanged(duration: Duration) = Unit
    override fun onFadeTargetVolumePercentChanged(percent: Int) = Unit
    override fun onGoHomeOnExpireChanged(enabled: Boolean) = Unit
    override fun onStopMediaOnExpireChanged(enabled: Boolean) = Unit
    override fun onResetSettings() = Unit
    override fun onAboutClick() = Unit
}
