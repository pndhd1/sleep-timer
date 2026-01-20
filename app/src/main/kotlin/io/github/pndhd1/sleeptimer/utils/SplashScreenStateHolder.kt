package io.github.pndhd1.sleeptimer.utils

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class SplashScreenStateHolder {

    var keepSplashScreen = true
}
