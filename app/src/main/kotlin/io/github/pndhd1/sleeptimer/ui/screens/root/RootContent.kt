package io.github.pndhd1.sleeptimer.ui.screens.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.screens.root.RootComponent.Child
import io.github.pndhd1.sleeptimer.ui.screens.timer.TimerContent

@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    val stack by component.stack.collectAsStateWithLifecycle()
    val activeChild = stack.active.instance

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = activeChild is Child.TimerChild,
                    onClick = component::onTimerTabClick,
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
                    onClick = component::onSettingsTabClick,
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                            contentDescription = stringResource(R.string.tab_settings),
                        )
                    },
                    label = { Text(stringResource(R.string.tab_settings)) },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when (val child = activeChild) {
                is Child.TimerChild -> TimerContent(
                    component = child.component,
                    modifier = Modifier.fillMaxSize(),
                )

                is Child.SettingsChild -> Text("Settings Screen")
            }
        }
    }
}
