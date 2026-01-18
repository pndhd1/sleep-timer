package io.github.pndhd1.sleeptimer.domain.model

import kotlin.time.Duration

data class TimerSettings(
    val defaultDuration: Duration,
    val presets: List<Duration>,
    val extendDuration: Duration,
    val showNotification: Boolean,
    val fadeOut: FadeOutSettings,
    val goHomeOnExpire: Boolean,
    val stopMediaOnExpire: Boolean,
)
