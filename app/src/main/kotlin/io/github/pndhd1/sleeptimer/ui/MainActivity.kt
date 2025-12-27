package io.github.pndhd1.sleeptimer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import io.github.pndhd1.sleeptimer.ui.root.DefaultRootComponent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val root = DefaultRootComponent(defaultComponentContext())

        setContent {
            SleepTimerApplication(
                component = root,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}