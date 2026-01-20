package io.github.pndhd1.sleeptimer.ui.screen.timer

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pndhd1.sleeptimer.ui.screen.timer.active.ActiveTimerContent
import io.github.pndhd1.sleeptimer.ui.screen.timer.active.PreviewActiveTimerComponent
import io.github.pndhd1.sleeptimer.ui.screen.timer.config.PreviewTimerConfigComponent
import io.github.pndhd1.sleeptimer.ui.screen.timer.config.TimerConfigContent
import io.github.pndhd1.sleeptimer.ui.screen.timer.config.TimerConfigState
import io.github.pndhd1.sleeptimer.ui.screen.timer.permission.PermissionContent
import io.github.pndhd1.sleeptimer.ui.screen.timer.permission.PreviewPermissionComponent
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.ui.widgets.ErrorLayout
import io.github.pndhd1.sleeptimer.ui.widgets.LoadingLayout
import io.github.pndhd1.sleeptimer.utils.Defaults
import io.github.pndhd1.sleeptimer.utils.ui.SolidInsetsBackground
import io.github.pndhd1.sleeptimer.utils.ui.adBannerIgnoringVisibility
import io.github.pndhd1.sleeptimer.utils.ui.appBottomNavigationBar
import io.github.pndhd1.sleeptimer.utils.ui.systemBarsForVisualComponents
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun TimerContent(
    component: TimerComponent,
    modifier: Modifier = Modifier,
) {
    val slot by component.slot.collectAsStateWithLifecycle()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.systemBarsForVisualComponents
                        .union(WindowInsets.appBottomNavigationBar)
                        .add(WindowInsets.adBannerIgnoringVisibility)
                        .union(WindowInsets.ime)
                ),
        ) {
            Crossfade(
                targetState = slot?.child?.instance,
                modifier = Modifier,
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

                    is TimerComponent.Child.Error -> ErrorLayout(modifier = Modifier.fillMaxSize())

                    null -> Unit // Loading is handled outside the Crossfade
                }
            }

            if (slot?.child?.instance == null) LoadingLayout(modifier = Modifier.fillMaxSize())
        }

        SolidInsetsBackground(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.appBottomNavigationBar)
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
