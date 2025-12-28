package io.github.pndhd1.sleeptimer.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale
import kotlin.time.Duration

object Formatter {

    fun formatTime(locale: Locale, value: Duration): String = "%02d:%02d:%02d".format(
        locale = locale,
        value.inWholeHours,
        value.inWholeMinutes % 60,
        value.inWholeSeconds % 60
    )

    @Composable
    fun formatTime(value: Duration): String {
        val configuration = LocalConfiguration.current
        val locale = configuration.locales[0]
        return formatTime(locale, value)
    }
}
