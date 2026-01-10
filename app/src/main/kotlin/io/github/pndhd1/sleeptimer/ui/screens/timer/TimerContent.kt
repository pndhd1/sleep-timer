package io.github.pndhd1.sleeptimer.ui.screens.timer

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.screens.timer.active.ActiveTimerContent
import io.github.pndhd1.sleeptimer.ui.screens.timer.active.PreviewActiveTimerComponent
import io.github.pndhd1.sleeptimer.ui.screens.timer.config.PreviewTimerConfigComponent
import io.github.pndhd1.sleeptimer.ui.screens.timer.config.TimerConfigContent
import io.github.pndhd1.sleeptimer.ui.screens.timer.config.TimerConfigState
import io.github.pndhd1.sleeptimer.ui.screens.timer.permission.PermissionContent
import io.github.pndhd1.sleeptimer.ui.screens.timer.permission.PreviewPermissionComponent
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.utils.Defaults
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun TimerContent(
    component: TimerComponent,
    modifier: Modifier = Modifier,
) {
    val slot by component.slot.collectAsStateWithLifecycle()
    Crossfade(slot?.child?.instance, modifier = modifier) { child ->
        when (child) {
            is TimerComponent.Child.Permission -> {
                PermissionContent(
                    component = child.component,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is TimerComponent.Child.Config -> {
                TimerConfigContent(
                    component = child.component,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is TimerComponent.Child.Active -> {
                ActiveTimerContent(
                    component = child.component,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            null -> LoadingContent(modifier = Modifier.fillMaxSize())
            is TimerComponent.Child.Error -> ErrorContent(modifier = Modifier.fillMaxSize())
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

@Composable
private fun ErrorContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.error_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// region Preview

private class TimerChildProvider : PreviewParameterProvider<TimerComponent.Child?> {
    override val values = sequenceOf(
        null, // Loading
        TimerComponent.Child.Error,
        TimerComponent.Child.Permission(PreviewPermissionComponent()),
        TimerComponent.Child.Config(
            PreviewTimerConfigComponent(
                TimerConfigState(
                    loading = false,
                    duration = 30.minutes,
                    presets = Defaults.DefaultPresets,
                )
            )
        ),
        TimerComponent.Child.Active(
            PreviewActiveTimerComponent(remainingDuration = 1.hours + 15.minutes)
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun TimerContentPreview(
    @PreviewParameter(TimerChildProvider::class) child: TimerComponent.Child?,
) {
    SleepTimerTheme {
        TimerContent(
            component = PreviewTimerComponent(child),
        )
    }
}

// endregion
