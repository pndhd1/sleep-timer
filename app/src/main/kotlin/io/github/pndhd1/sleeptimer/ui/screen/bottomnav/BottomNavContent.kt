package io.github.pndhd1.sleeptimer.ui.screen.bottomnav

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.androidPredictiveBackAnimatableV1
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import com.yandex.mobile.ads.banner.BannerAdSize
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.screen.bottomnav.BottomNavComponent.Child
import io.github.pndhd1.sleeptimer.ui.screen.bottomnav.widgets.BottomNavAdBanner
import io.github.pndhd1.sleeptimer.ui.screen.settings.PreviewSettingsComponent
import io.github.pndhd1.sleeptimer.ui.screen.settings.SettingsContent
import io.github.pndhd1.sleeptimer.ui.screen.settings.SettingsState
import io.github.pndhd1.sleeptimer.ui.screen.timer.PreviewTimerComponent
import io.github.pndhd1.sleeptimer.ui.screen.timer.TimerContent
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.utils.YandexAdsState
import io.github.pndhd1.sleeptimer.utils.applyIf
import io.github.pndhd1.sleeptimer.utils.isPortrait
import io.github.pndhd1.sleeptimer.utils.ui.LocalAdBannerInsets
import io.github.pndhd1.sleeptimer.utils.ui.LocalAdBannerInsetsIgnoringVisibility
import io.github.pndhd1.sleeptimer.utils.ui.LocalBottomNavigationBarInsets
import io.github.pndhd1.sleeptimer.utils.ui.systemBarsForVisualComponents

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BottomNavContent(
    component: BottomNavComponent,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val stack by component.stack.collectAsStateWithLifecycle()
    val isPortrait = isPortrait()

    val bottomNavInsets = remember { MutableWindowInsets() }
    val adBannerInsets = remember { MutableWindowInsets() }

    BoxWithConstraints(modifier = modifier) {
        val constraints = this

        // https://ads.yandex.com/helpcenter/en/dev/android/adaptive-sticky-banner
        // It is recommended to recalculate the size on initialization
        val adSize = remember(context, YandexAdsState.isInitialized) {
            BannerAdSize.stickySize(context, constraints.maxWidth.value.toInt())
        }

        CompositionLocalProvider(
            LocalBottomNavigationBarInsets provides bottomNavInsets,
            LocalAdBannerInsets provides adBannerInsets,
            LocalAdBannerInsetsIgnoringVisibility provides WindowInsets(bottom = adSize.height.dp)
        ) {

            Row {
                if (!isPortrait) LeftNavigationRail(
                    activeChild = stack.active.instance,
                    onTimerTabClick = component::onTimerTabClick,
                    onSettingsTabClick = component::onSettingsTabClick,
                    modifier = Modifier.fillMaxHeight(),
                )

                NavContent(
                    component = component,
                    stack = stack,
                    modifier = Modifier
                        .weight(1f)
                        .let {
                            if (isPortrait) it.consumeWindowInsets(
                                NavigationRailDefaults.windowInsets.only(WindowInsetsSides.Start)
                            ) else {
                                it
                            }
                        },
                )
            }

            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                BottomNavAdBanner(
                    size = adSize,
                    modifier = Modifier.applyIf(!isPortrait) {
                        val inset = WindowInsets.systemBarsForVisualComponents
                        val symmetricPadding = with(density) {
                            maxOf(
                                inset.getLeft(density, layoutDirection),
                                inset.getRight(density, layoutDirection)
                            ).toDp()
                        }
                        Modifier
                            .padding(horizontal = symmetricPadding)
                            .windowInsetsPadding(inset.only(WindowInsetsSides.Bottom))
                    },
                    onAdVisibilityChanged = {
                        adBannerInsets.insets = if (it) {
                            WindowInsets(bottom = adSize.height.dp)
                        } else {
                            WindowInsets()
                        }
                    },
                )

                if (isPortrait) BottomNavigationBar(
                    activeChild = stack.active.instance,
                    onTimerTabClick = component::onTimerTabClick,
                    onSettingsTabClick = component::onSettingsTabClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged {
                            bottomNavInsets.insets = WindowInsets(
                                bottom = with(density) { it.height.toDp() }
                            )
                        },
                ) else {
                    LaunchedEffect(bottomNavInsets) {
                        bottomNavInsets.insets = WindowInsets()
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
            )

            is Child.Settings -> SettingsContent(
                component = instance.component,
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
    NavigationBar(
        modifier = modifier,
        containerColor = Color.Transparent
    ) {
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
