package io.github.pndhd1.sleeptimer.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import io.github.pndhd1.sleeptimer.R
import java.util.Locale
import kotlin.time.Duration

object Formatter {

    fun formatTimeWithDots(locale: Locale, value: Duration): String = "%02d:%02d:%02d".format(
        locale = locale,
        value.inWholeHours,
        value.inWholeMinutes % 60,
        value.inWholeSeconds % 60
    )

    @Composable
    fun formatTimeWithDots(value: Duration): String {
        val configuration = LocalConfiguration.current
        val locale = configuration.locales[0]
        return formatTimeWithDots(locale, value)
    }

    @Composable
    fun formatTimeWithUnits(duration: Duration): String {
        val hours = duration.inWholeHours
        val minutes = duration.inWholeMinutes % 60
        val seconds = duration.inWholeSeconds % 60

        return buildString {
            if (hours > 0) append(stringResource(R.string.preset_hours, hours.toInt()))
            if (minutes > 0) {
                if (isNotEmpty()) append(" ")
                append(stringResource(R.string.preset_minutes, minutes.toInt()))
            }
            if (seconds > 0) {
                if (isNotEmpty()) append(" ")
                append(stringResource(R.string.preset_seconds, seconds.toInt()))
            }
            if (isEmpty()) append(stringResource(R.string.preset_seconds, 0))
        }
    }

    @Composable
    fun formatShortTimeWithUnits(duration: Duration): String {
        val totalMinutes = duration.inWholeMinutes
        val seconds = duration.inWholeSeconds % 60

        return buildString {
            if (totalMinutes > 0) append(
                stringResource(
                    R.string.preset_minutes,
                    totalMinutes.toInt()
                )
            )
            if (seconds > 0) {
                if (isNotEmpty()) append(" ")
                append(stringResource(R.string.preset_seconds, seconds.toInt()))
            }
            if (isEmpty()) append(stringResource(R.string.preset_seconds, 0))
        }
    }
}
