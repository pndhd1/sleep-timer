package io.github.pndhd1.sleeptimer.ui.screen.timer.active

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.utils.Formatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun ActiveTimerContent(
    component: ActiveTimerComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsStateWithLifecycle()
    val remainingTime by produceState(Duration.ZERO, state.targetTime) {
        while (isActive) {
            value = (state.targetTime - Clock.System.now()).coerceAtLeast(Duration.ZERO)
            // update often then 1 second to make countdown precise
            delay(0.5.seconds)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = Formatter.formatTimeWithDots(remainingTime),
            style = MaterialTheme.typography.displayLarge,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.extendDuration > Duration.ZERO) {
                OutlinedButton(
                    onClick = component::onExtendClick,
                    modifier = Modifier.height(56.dp),
                ) {
                    Text(
                        text = stringResource(R.string.button_extend),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

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
}

// region Preview

private class RemainingDurationProvider : PreviewParameterProvider<Duration> {
    override val values = sequenceOf(
        1.hours + 30.minutes,
        45.minutes + 23.seconds,
        15.seconds,
    )
}

@Preview(showBackground = true)
@PreviewScreenSizes
@Composable
private fun ActiveTimerContentPreview(
    @PreviewParameter(RemainingDurationProvider::class) remainingDuration: Duration,
) {
    SleepTimerTheme {
        ActiveTimerContent(
            component = PreviewActiveTimerComponent(remainingDuration),
        )
    }
}

// endregion
