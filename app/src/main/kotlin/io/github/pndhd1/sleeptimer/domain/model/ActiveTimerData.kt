package io.github.pndhd1.sleeptimer.domain.model

import kotlin.time.Duration
import kotlin.time.Instant

data class ActiveTimerData(
    val targetTime: Instant,
    val totalDuration: Duration,
)
