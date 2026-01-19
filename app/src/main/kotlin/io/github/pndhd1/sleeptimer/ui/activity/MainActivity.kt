package io.github.pndhd1.sleeptimer.ui.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.retainedComponent
import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.requireAppGraph
import io.github.pndhd1.sleeptimer.ui.screen.root.DefaultRootComponent
import io.github.pndhd1.sleeptimer.ui.screen.root.RootContent
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.utils.ui.LocalNavigationMode
import io.github.pndhd1.sleeptimer.utils.ui.NavigationMode

class MainActivity : ComponentActivity() {

    @Inject
    private lateinit var rootComponentFactory: DefaultRootComponent.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        requireAppGraph().inject(this)

        val navigationMode = navigationMode()
        val rootComponent = retainedComponent(factory = rootComponentFactory::create)
        setContent {
            CompositionLocalProvider(LocalNavigationMode provides navigationMode) {
                SleepTimerTheme {
                    RootContent(
                        component = rootComponent,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }

    fun navigationMode(): NavigationMode {
        val isGestures = runCatching {
            Settings.Secure.getInt(
                contentResolver,
                "navigation_mode",
                0
            ) == 2
        }.getOrDefault(true)
        return if (isGestures) {
            NavigationMode.Gestures
        } else {
            NavigationMode.Buttons
        }
    }
}
