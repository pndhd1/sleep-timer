package io.github.pndhd1.sleeptimer.ui.timer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.pndhd1.sleeptimer.ui.timer.active.ActiveTimerContent
import io.github.pndhd1.sleeptimer.ui.timer.config.TimerConfigContent

@Composable
fun TimerContent(
    component: TimerComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()

    when (val currentState = state) {
        is TimerComponent.TimerState.Loading -> {
            LoadingContent(modifier = modifier)
        }
        is TimerComponent.TimerState.Content -> {
            when (val child = currentState.childSlot.child?.instance) {
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
                null -> {
                    LoadingContent(modifier = modifier)
                }
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
