package io.github.pndhd1.sleeptimer.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object Defaults {

    inline val DefaultPresets: List<Duration>
        get() = listOf(
            15.minutes,
            30.minutes,
            45.minutes,
            60.minutes
        )

    inline val DefaultDuration: Duration get() = 30.minutes
    inline val DefaultExtendDuration: Duration get() = 5.minutes
    const val DefaultShowNotification: Boolean = true

    val MinTimerDuration: Duration = 5.seconds
    val MaxTimerDuration: Duration = 23.hours + 59.minutes + 59.seconds
}
