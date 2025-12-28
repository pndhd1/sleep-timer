package io.github.pndhd1.sleeptimer.ui.screens.timer.active

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.utils.Formatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun ActiveTimerContent(
    component: ActiveTimerComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val remainingTime by produceState(Duration.ZERO, state.targetTime) {
        while (isActive) {
            value = (state.targetTime - Clock.System.now()).coerceAtLeast(Duration.ZERO)
            // update often then 1 second to make countdown precise
            delay(0.5.seconds)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = Formatter.formatTime(remainingTime),
            style = MaterialTheme.typography.displayLarge,
        )

        Spacer(modifier = Modifier.height(48.dp))

        FilledTonalButton(
            onClick = component::onStopClick,
            modifier = Modifier.height(56.dp),
        ) {
            Text(
                text = stringResource(R.string.button_stop),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
