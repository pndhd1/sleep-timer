package io.github.pndhd1.sleeptimer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.pndhd1.sleeptimer.ui.root.RootComponent
import io.github.pndhd1.sleeptimer.ui.root.RootContent
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme

@Composable
fun SleepTimerApplication(
    component: RootComponent,
    modifier: Modifier = Modifier,
) = SleepTimerTheme {
    RootContent(
        component = component,
        modifier = modifier,
    )
}