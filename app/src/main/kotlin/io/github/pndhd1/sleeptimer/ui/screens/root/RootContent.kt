package io.github.pndhd1.sleeptimer.ui.screens.root

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pndhd1.sleeptimer.ui.screens.root.RootComponent.Child
import io.github.pndhd1.sleeptimer.ui.screens.root.widgets.RootNavigationBar
import io.github.pndhd1.sleeptimer.ui.screens.settings.SettingsContent
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
            RootNavigationBar(
                activeChild = activeChild,
                onTimerTabClick = component::onTimerTabClick,
                onSettingsTabClick = component::onSettingsTabClick,
            )
        },
    ) { innerPadding ->
        Crossfade(activeChild) { activeChild ->
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

                    is Child.SettingsChild -> SettingsContent(
                        component = child.component,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
