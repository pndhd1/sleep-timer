package io.github.pndhd1.sleeptimer.domain.repository

import io.github.pndhd1.sleeptimer.domain.model.ActiveTimerData
import io.github.pndhd1.sleeptimer.domain.model.FadeOutSettings
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Instant

interface ActiveTimerRepository {

    val activeTimer: Flow<ActiveTimerData?>

    suspend fun startTimer(
        targetTime: Instant,
        totalDuration: Duration,
        fadeOutSettings: FadeOutSettings,
    )
    suspend fun extendTimer(additionalDuration: Duration, fadeOutSettings: FadeOutSettings)
    suspend fun clearTimer()
}
