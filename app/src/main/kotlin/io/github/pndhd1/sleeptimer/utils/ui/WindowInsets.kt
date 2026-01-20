package io.github.pndhd1.sleeptimer.utils.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalBottomNavigationBarInsets = staticCompositionLocalOf { WindowInsets() }
val LocalAdBannerInsets = staticCompositionLocalOf { WindowInsets() }
val LocalAdBannerInsetsIgnoringVisibility = staticCompositionLocalOf { WindowInsets() }

// Copied from androidx.compose.material3.internal
val WindowInsets.Companion.systemBarsForVisualComponents: WindowInsets
    @Composable get() = systemBars.union(displayCutout)

val WindowInsets.Companion.appBottomNavigationBar: WindowInsets
    @Composable get() = LocalBottomNavigationBarInsets.current

val WindowInsets.Companion.adBanner: WindowInsets
    @Composable get() = LocalAdBannerInsets.current

val WindowInsets.Companion.adBannerIgnoringVisibility: WindowInsets
    @Composable get() = LocalAdBannerInsetsIgnoringVisibility.current
