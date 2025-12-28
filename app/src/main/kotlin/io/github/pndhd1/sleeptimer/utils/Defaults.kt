package io.github.pndhd1.sleeptimer.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object Defaults {

    inline val DefaultDuration: Duration get() = 30.minutes

    inline val DefaultPresets: List<Duration>
        get() = listOf(
            15.minutes,
            30.minutes,
            45.minutes,
            60.minutes
        )
}
