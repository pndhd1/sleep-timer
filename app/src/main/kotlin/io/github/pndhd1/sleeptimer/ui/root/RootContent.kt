package io.github.pndhd1.sleeptimer.ui.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.root.RootComponent.Child

@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    val stack by component.stack.subscribeAsState()
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
            when (activeChild) {
                is Child.TimerChild -> Text("Timer Screen")
                is Child.SettingsChild -> Text("Settings Screen")
            }
        }
    }
}
