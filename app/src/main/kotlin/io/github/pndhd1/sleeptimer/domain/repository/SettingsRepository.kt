package io.github.pndhd1.sleeptimer.domain.repository

import io.github.pndhd1.sleeptimer.domain.model.TimerSettings
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface SettingsRepository {

    val timerSettings: Flow<TimerSettings>

    suspend fun updateTimerDefaultDuration(duration: Duration)
    suspend fun updateTimerPresets(presets: List<Duration>)
    suspend fun updateExtendDuration(duration: Duration)
    suspend fun updateShowNotification(show: Boolean)
    suspend fun updateFadeOutEnabled(enabled: Boolean)
    suspend fun updateFadeStartBefore(duration: Duration)
    suspend fun updateFadeOutDuration(duration: Duration)
    suspend fun updateFadeTargetVolumePercent(percent: Int)
    suspend fun updateGoHomeOnExpire(enabled: Boolean)
    suspend fun updateStopMediaOnExpire(enabled: Boolean)
    suspend fun resetToDefaults()
}
