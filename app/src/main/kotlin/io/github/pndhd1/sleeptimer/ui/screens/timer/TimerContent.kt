package io.github.pndhd1.sleeptimer.ui.screens.timer

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.pndhd1.sleeptimer.ui.screens.timer.active.ActiveTimerContent
import io.github.pndhd1.sleeptimer.ui.screens.timer.config.TimerConfigContent

@Composable
fun TimerContent(
    component: TimerComponent,
    modifier: Modifier = Modifier,
) {
    val slot by component.slot.collectAsState()
    Crossfade(slot?.child?.instance) { child ->
        when (child) {
            null -> LoadingContent(modifier = modifier)
            is TimerComponent.Child.Config -> {
                TimerConfigContent(
                    component = child.component,
                    modifier = modifier,
                )
            }

            is TimerComponent.Child.Active -> {
                ActiveTimerContent(
                    component = child.component,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
