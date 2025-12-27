package io.github.pndhd1.sleeptimer.domain.repository

import io.github.pndhd1.sleeptimer.domain.model.ActiveTimerData
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface ActiveTimerRepository {

    val activeTimer: Flow<ActiveTimerData?>

    suspend fun startTimer(duration: Duration)
    suspend fun clearTimer()
}
