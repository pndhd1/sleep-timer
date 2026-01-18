package io.github.pndhd1.sleeptimer.utils.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

object UIDefaults {

    val SystemBarsBackgroundColor
        @Composable
        get() = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.8f)

    val defaultInsets: WindowInsets
        @Composable
        get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)
}
