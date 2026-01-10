package io.github.pndhd1.sleeptimer.ui.screens.root.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.screens.root.RootComponent.Child
import io.github.pndhd1.sleeptimer.ui.screens.settings.PreviewSettingsComponent
import io.github.pndhd1.sleeptimer.ui.screens.settings.SettingsState
import io.github.pndhd1.sleeptimer.ui.screens.timer.PreviewTimerComponent
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme

@Composable
fun RootNavigationBar(
    activeChild: Child,
    onTimerTabClick: () -> Unit,
    onSettingsTabClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = activeChild is Child.TimerChild,
            onClick = onTimerTabClick,
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_timer),
                    contentDescription = stringResource(R.string.tab_timer),
                )
            },
            label = { Text(stringResource(R.string.tab_timer)) },
        )
        NavigationBarItem(
            selected = activeChild is Child.SettingsChild,
            onClick = onSettingsTabClick,
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.tab_settings),
                )
            },
            label = { Text(stringResource(R.string.tab_settings)) },
        )
    }
}

// region Preview

private class ChildProvider : PreviewParameterProvider<Child> {
    override val values = sequenceOf(
        Child.TimerChild(PreviewTimerComponent(null)),
        Child.SettingsChild(PreviewSettingsComponent(SettingsState.Loading)),
    )
}

@Preview
@Composable
private fun RootNavigationBarPreview(
    @PreviewParameter(ChildProvider::class) activeChild: Child,
) {
    SleepTimerTheme {
        RootNavigationBar(
            activeChild = activeChild,
            onTimerTabClick = {},
            onSettingsTabClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// endregion
