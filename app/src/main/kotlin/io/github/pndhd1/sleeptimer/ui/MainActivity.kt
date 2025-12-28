package io.github.pndhd1.sleeptimer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.retainedComponent
import io.github.pndhd1.sleeptimer.SleepTimerApplication
import io.github.pndhd1.sleeptimer.ui.screens.root.RootContent
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appGraph = (application as SleepTimerApplication).appGraph
        val rootComponent = retainedComponent(factory = appGraph.rootComponentFactory::create)

        setContent {
            SleepTimerTheme {
                RootContent(
                    component = rootComponent,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
