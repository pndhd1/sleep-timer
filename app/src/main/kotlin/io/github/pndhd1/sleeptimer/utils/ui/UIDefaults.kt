package io.github.pndhd1.sleeptimer.utils.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object UIDefaults {

    val SystemBarsBackgroundColor get() = Color.Black.copy(alpha = 0.5f)

    val defaultInsets: WindowInsets
        @Composable
        get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)
}
