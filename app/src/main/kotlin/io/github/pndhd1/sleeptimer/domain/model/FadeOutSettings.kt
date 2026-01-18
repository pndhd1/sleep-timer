package io.github.pndhd1.sleeptimer.domain.model

import kotlin.time.Duration

data class FadeOutSettings(
    val enabled: Boolean,
    val startBefore: Duration,
    val duration: Duration,
    val targetVolumePercent: Int,
)
