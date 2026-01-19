package io.github.pndhd1.sleeptimer.utils.ui

import androidx.compose.runtime.staticCompositionLocalOf

enum class NavigationMode {

    Buttons, Gestures
}

val LocalNavigationMode = staticCompositionLocalOf { NavigationMode.Gestures }
