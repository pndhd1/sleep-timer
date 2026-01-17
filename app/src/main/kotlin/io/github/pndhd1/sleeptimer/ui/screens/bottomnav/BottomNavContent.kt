package io.github.pndhd1.sleeptimer.ui.screens.bottomnav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.screens.bottomnav.BottomNavComponent.Child
import io.github.pndhd1.sleeptimer.ui.screens.settings.PreviewSettingsComponent
import io.github.pndhd1.sleeptimer.ui.screens.settings.SettingsContent
import io.github.pndhd1.sleeptimer.ui.screens.settings.SettingsState
import io.github.pndhd1.sleeptimer.ui.screens.timer.PreviewTimerComponent
import io.github.pndhd1.sleeptimer.ui.screens.timer.TimerContent
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme

@Composable
fun BottomNavContent(
    component: BottomNavComponent,
    modifier: Modifier = Modifier,
) {
    val stack by component.stack.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomNavigationBar(
                activeChild = stack.active.instance,
                onTimerTabClick = component::onTimerTabClick,
                onSettingsTabClick = component::onSettingsTabClick,
            )
        },
    ) { innerPadding ->
        Children(
            stack = stack,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            animation = stackAnimation(fade()),
        ) { child ->
            when (val instance = child.instance) {
                is Child.Timer -> TimerContent(
                    component = instance.component,
                    modifier = Modifier.fillMaxSize(),
                )

                is Child.Settings -> SettingsContent(
                    component = instance.component,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    activeChild: Child,
    onTimerTabClick: () -> Unit,
    onSettingsTabClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = activeChild is Child.Timer,
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
            selected = activeChild is Child.Settings,
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
        Child.Timer(PreviewTimerComponent(null)),
        Child.Settings(PreviewSettingsComponent(SettingsState.Loading)),
    )
}

@Preview
@Composable
private fun BottomNavigationBarPreview(
    @PreviewParameter(ChildProvider::class) child: Child,
) {
    SleepTimerTheme {
        BottomNavigationBar(
            activeChild = child,
            onTimerTabClick = {},
            onSettingsTabClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// endregion
