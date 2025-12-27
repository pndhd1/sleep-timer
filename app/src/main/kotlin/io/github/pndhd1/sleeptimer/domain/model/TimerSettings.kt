package io.github.pndhd1.sleeptimer.domain.model

import kotlin.time.Duration

data class TimerSettings(
    val defaultDuration: Duration,
    val presets: List<Duration>,
)
