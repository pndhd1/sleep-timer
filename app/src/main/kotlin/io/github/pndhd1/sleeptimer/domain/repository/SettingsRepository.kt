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
    suspend fun resetToDefaults()
}
