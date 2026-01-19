package io.github.pndhd1.sleeptimer.ui.screen.timer

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pndhd1.sleeptimer.ui.screen.timer.active.ActiveTimerContent
import io.github.pndhd1.sleeptimer.ui.screen.timer.active.PreviewActiveTimerComponent
import io.github.pndhd1.sleeptimer.ui.screen.timer.config.PreviewTimerConfigComponent
import io.github.pndhd1.sleeptimer.ui.screen.timer.config.TimerConfigContent
import io.github.pndhd1.sleeptimer.ui.screen.timer.config.TimerConfigState
import io.github.pndhd1.sleeptimer.ui.screen.timer.permission.PermissionContent
import io.github.pndhd1.sleeptimer.ui.screen.timer.permission.PreviewPermissionComponent
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.ui.widgets.ErrorScreen
import io.github.pndhd1.sleeptimer.utils.AdStickySizeInset
import io.github.pndhd1.sleeptimer.utils.Defaults
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun TimerContent(
    component: TimerComponent,
    modifier: Modifier = Modifier,
    bottomInsetCompensation: Dp = 0.dp,
) {
    val slot by component.slot.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Crossfade(
            targetState = slot?.child?.instance,
            modifier = Modifier
                .weight(1f)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.End)),
        ) { child ->
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

                is TimerComponent.Child.Error -> ErrorScreen(modifier = Modifier.fillMaxSize())
                null -> LoadingContent(modifier = Modifier.fillMaxSize())
            }
        }


        Box(contentAlignment = Alignment.BottomCenter) {
            val imePadding = WindowInsets.ime.asPaddingValues()
            val resultPadding = PaddingValues(
                start = imePadding.calculateStartPadding(LocalLayoutDirection.current),
                end = imePadding.calculateEndPadding(LocalLayoutDirection.current),
                top = imePadding.calculateTopPadding(),
                bottom = (imePadding.calculateBottomPadding() - bottomInsetCompensation)
                    .coerceAtLeast(0.dp),
            )

            Spacer(Modifier.padding(resultPadding))

            // We are adding ad banner inset here directly without checking BottomNavAdBannerState.visible
            // to avoid UI jump when banner visibility changes
            AdStickySizeInset()
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
