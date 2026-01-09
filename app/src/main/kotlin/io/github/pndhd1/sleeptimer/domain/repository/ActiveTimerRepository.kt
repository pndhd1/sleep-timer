package io.github.pndhd1.sleeptimer.domain.repository

import io.github.pndhd1.sleeptimer.domain.model.ActiveTimerData
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Instant

interface ActiveTimerRepository {

    val activeTimer: Flow<ActiveTimerData?>

    suspend fun startTimer(targetTime: Instant, totalDuration: Duration)
    suspend fun extendTimer(additionalDuration: Duration)
    suspend fun clearTimer()
}
