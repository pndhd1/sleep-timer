package io.github.pndhd1.sleeptimer.ui.screen.bottomnav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.androidPredictiveBackAnimatableV1
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.screen.bottomnav.BottomNavComponent.Child
import io.github.pndhd1.sleeptimer.ui.screen.bottomnav.widgets.BottomNavAdBanner
import io.github.pndhd1.sleeptimer.ui.screen.bottomnav.widgets.BottomNavAdBannerState
import io.github.pndhd1.sleeptimer.ui.screen.bottomnav.widgets.rememberBottomNavAdBannerState
import io.github.pndhd1.sleeptimer.ui.screen.settings.PreviewSettingsComponent
import io.github.pndhd1.sleeptimer.ui.screen.settings.SettingsContent
import io.github.pndhd1.sleeptimer.ui.screen.settings.SettingsState
import io.github.pndhd1.sleeptimer.ui.screen.timer.PreviewTimerComponent
import io.github.pndhd1.sleeptimer.ui.screen.timer.TimerContent
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.utils.isPortrait
import io.github.pndhd1.sleeptimer.utils.ui.UIDefaults

@Composable
fun BottomNavContent(
    component: BottomNavComponent,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val stack by component.stack.collectAsStateWithLifecycle()
    val isPortrait = isPortrait()
    val bannerState = rememberBottomNavAdBannerState()
    var timerBottomInsetCompensation by remember { mutableStateOf(0.dp) }
    val navContent = remember {
        movableContentOf {
            NavContent(
                component = component,
                stack = stack,
                bannerState = bannerState,
                modifier = Modifier.fillMaxSize(),
                timerBottomInsetCompensation = timerBottomInsetCompensation,
            )
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (isPortrait) BottomNavigationBar(
                activeChild = stack.active.instance,
                onTimerTabClick = component::onTimerTabClick,
                onSettingsTabClick = component::onSettingsTabClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged {
                        timerBottomInsetCompensation = with(density) { it.height.toDp() }
                    },
            )
        },
        // Window insets are handled manually inside the content
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Box {
                if (!isPortrait) {
                    Row {
                        LeftNavigationRail(
                            activeChild = stack.active.instance,
                            onTimerTabClick = component::onTimerTabClick,
                            onSettingsTabClick = component::onSettingsTabClick,
                            modifier = Modifier.fillMaxHeight(),
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(
                                    NavigationRailDefaults.windowInsets.only(WindowInsetsSides.Start)
                                ),
                        ) {
                            navContent()
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .consumeWindowInsets(
                                NavigationBarDefaults.windowInsets.only(WindowInsetsSides.Bottom)
                            )
                    ) {
                        navContent()
                    }
                }

                Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                    BottomNavAdBanner(state = bannerState)
                    if (!isPortrait) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .windowInsetsBottomHeight(UIDefaults.defaultInsets)
                                .let {
                                    if (bannerState.isBannerVisible) {
                                        it.background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    } else {
                                        it
                                    }
                                }

                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun NavContent(
    component: BottomNavComponent,
    stack: ChildStack<*, Child>,
    bannerState: BottomNavAdBannerState,
    timerBottomInsetCompensation: Dp,
    modifier: Modifier = Modifier,
) {
    Children(
        stack = stack,
        modifier = modifier,
        animation = predictiveBackAnimation(
            backHandler = component.backHandler,
            fallbackAnimation = stackAnimation(fade()),
            onBack = component::onBackClicked,
            selector = { backEvent, _, _ -> androidPredictiveBackAnimatableV1(backEvent) }
        ),
    ) { child ->
        when (val instance = child.instance) {
            is Child.Timer -> TimerContent(
                component = instance.component,
                bottomInsetCompensation = timerBottomInsetCompensation,
            )

            is Child.Settings -> SettingsContent(
                component = instance.component,
                bannerState = bannerState,
            )
        }
    }
}

@Composable
private fun LeftNavigationRail(
    activeChild: Child,
    onTimerTabClick: () -> Unit,
    onSettingsTabClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        NavigationRailItem(
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
        NavigationRailItem(
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

@Preview
@Composable
private fun LeftNavigationRailPreview(
    @PreviewParameter(ChildProvider::class) child: Child,
) {
    SleepTimerTheme {
        LeftNavigationRail(
            activeChild = child,
            onTimerTabClick = {},
            onSettingsTabClick = {},
        )
    }
}

// endregion
