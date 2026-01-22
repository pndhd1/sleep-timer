package io.github.pndhd1.sleeptimer.ui.screen.timer.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.domain.model.FabAlignment
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.ui.widgets.DurationSlider
import io.github.pndhd1.sleeptimer.utils.Defaults
import io.github.pndhd1.sleeptimer.utils.Formatter
import io.github.pndhd1.sleeptimer.utils.isPortrait
import io.github.pndhd1.sleeptimer.utils.ui.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val TimerSliderMax = 90.minutes
private val TimerSliderStep = 1.minutes

private data class CardPosition(
    val offset: DpOffset = DpOffset.Zero,
    val width: Dp = 0.dp,
    val height: Dp = 0.dp,
)

private data class ContainerBounds(
    val offset: DpOffset = DpOffset.Zero,
    val width: Dp = 0.dp,
    val horizontalPadding: Dp = 0.dp,
)

@Composable
fun TimerConfigContent(
    component: TimerConfigComponent,
    modifier: Modifier = Modifier,
    isInTransition: Boolean = false,
) {
    val state by component.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    val density = LocalDensity.current

    var customCardPosition by remember { mutableStateOf(CardPosition()) }
    var containerBounds by remember { mutableStateOf(ContainerBounds()) }

    val gridInsets = WindowInsets.systemBarsForVisualComponents
        .union(WindowInsets.appBottomNavigationBar)
        .add(WindowInsets.adBanner)
        .add(WindowInsets(left = 16.dp, top = 16.dp, right = 16.dp, bottom = 16.dp))

    Box(modifier = modifier.fillMaxSize()) {
        CardGrid(
            state = state,
            gridState = gridState,
            contentInsets = gridInsets,
            onDefaultClick = {
                component.onDurationChanged(state.defaultDuration)
                component.onStartClick()
            },
            onCustomClick = { component.onCustomExpandedChanged(true) },
            onPresetClick = { duration ->
                component.onDurationChanged(duration)
                component.onStartClick()
            },
            onCustomCardPositioned = { position ->
                customCardPosition = position
            },
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(gridInsets.only(WindowInsetsSides.Horizontal))
                .onGloballyPositioned { coordinates ->
                    val windowPosition = coordinates.positionInWindow()
                    containerBounds = with(density) {
                        ContainerBounds(
                            offset = DpOffset(windowPosition.x.toDp(), windowPosition.y.toDp()),
                            width = coordinates.size.width.toDp(),
                        )
                    }
                },
        )

        InsetsBackground(
            visible = gridState.canScrollBackward,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsTopHeight(WindowInsets.systemBars)
                .fillMaxWidth(),
        )

        InsetsBackground(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsBottomHeight(
                    WindowInsets.appBottomNavigationBar
                        .union(WindowInsets.systemBars)
                        .let {
                            if (!isPortrait() && LocalNavigationMode.current == NavigationMode.Gestures) {
                                it.exclude(WindowInsets.systemBars)
                            } else {
                                it
                            }
                        }
                )
                .fillMaxWidth()
                .clickable(interactionSource = null, indication = null) {
                    // Intercept clicks under banner
                },
        )

        FloatingActionButton(
            onClick = {
                component.onDurationChanged(state.defaultDuration)
                component.onStartClick()
            },
            modifier = Modifier
                .align(
                    when (state.fabAlignment) {
                        FabAlignment.Start -> Alignment.BottomStart
                        FabAlignment.End -> Alignment.BottomEnd
                    }
                )
                .windowInsetsPadding(
                    WindowInsets.systemBarsForVisualComponents
                        .union(WindowInsets.appBottomNavigationBar)
                        .add(WindowInsets.adBanner)
                )
                .padding(16.dp),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_timer),
                contentDescription = stringResource(R.string.button_start),
            )
        }
    }

    if (state.isCustomExpanded && !isInTransition) {
        CustomCardDialog(
            duration = state.duration,
            hasTime = state.hasTime,
            cardPosition = customCardPosition,
            containerBounds = containerBounds,
            onDurationChanged = component::onDurationChanged,
            onStartClick = component::onStartClick,
            onDismiss = { component.onCustomExpandedChanged(false) },
        )
    }
}

@Composable
private fun CardGrid(
    state: TimerConfigState,
    gridState: LazyGridState,
    contentInsets: WindowInsets,
    onDefaultClick: () -> Unit,
    onCustomClick: () -> Unit,
    onPresetClick: (Duration) -> Unit,
    onCustomCardPositioned: (CardPosition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val columns = if (isPortrait()) 2 else 4
    val density = LocalDensity.current

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = contentInsets.only(WindowInsetsSides.Vertical).asPaddingValues(),
    ) {
        // Default duration card
        item {
            TimerCard(
                title = stringResource(R.string.timer_quick_start),
                subtitle = Formatter.formatTimeWithUnits(state.defaultDuration),
                icon = R.drawable.ic_timer,
                filled = true,
                onClick = onDefaultClick,
                modifier = Modifier.aspectRatio(1f),
            )
        }

        // Custom card
        item {
            TimerCard(
                title = stringResource(R.string.timer_custom),
                subtitle = stringResource(R.string.timer_set_your_time),
                icon = R.drawable.ic_edit,
                onClick = onCustomClick,
                modifier = Modifier
                    .aspectRatio(1f)
                    .onGloballyPositioned { coordinates ->
                        val windowPosition = coordinates.positionInWindow()
                        onCustomCardPositioned(
                            with(density) {
                                CardPosition(
                                    offset = DpOffset(
                                        windowPosition.x.toDp(),
                                        windowPosition.y.toDp()
                                    ),
                                    width = coordinates.size.width.toDp(),
                                    height = coordinates.size.height.toDp(),
                                )
                            }

                        )
                    },
            )
        }

        // Preset cards
        items(state.presets) { preset ->
            TimerCard(
                title = Formatter.formatTimeWithUnits(preset),
                subtitle = stringResource(R.string.timer_preset),
                icon = R.drawable.ic_bolt,
                onClick = { onPresetClick(preset) },
                modifier = Modifier.aspectRatio(1f),
            )
        }
    }
}

@Composable
private fun CustomCardDialog(
    duration: Duration,
    hasTime: Boolean,
    cardPosition: CardPosition,
    containerBounds: ContainerBounds,
    onDurationChanged: (Duration) -> Unit,
    onStartClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(interactionSource = null, indication = null, onClick = onDismiss)
        ) {
            Card(
                modifier = Modifier
                    .offset(containerBounds.offset.x, cardPosition.offset.y)
                    .size(containerBounds.width, cardPosition.height)
                    .clickable(interactionSource = null, indication = null) {
                        // Intercept clicks inside the card
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DurationSlider(
                        duration = duration,
                        onDurationChanged = onDurationChanged,
                        minDuration = Defaults.MinTimerDuration,
                        maxDuration = TimerSliderMax,
                        step = TimerSliderStep,
                        modifier = Modifier.weight(1f),
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onStartClick,
                            enabled = hasTime,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_timer),
                                contentDescription = stringResource(R.string.button_start),
                            )
                        }

                        FilledTonalButton(onClick = onDismiss) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                                contentDescription = stringResource(R.string.button_close),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    duration: Duration? = null,
    icon: Int? = null,
    filled: Boolean = false,
) {
    val containerColor = if (filled) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val contentColor = if (filled) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val accentColor = if (filled) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = ImageVector.vectorResource(icon),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (duration != null) {
                Text(
                    text = Formatter.formatTimeWithUnits(duration),
                    style = MaterialTheme.typography.headlineLarge,
                    color = accentColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// region Preview

private class TimerConfigStateProvider : PreviewParameterProvider<TimerConfigState> {
    override val values = sequenceOf(
        TimerConfigState(
            loading = false,
            duration = 45.minutes,
            defaultDuration = 45.minutes,
            presets = Defaults.DefaultPresets,
            fabAlignment = FabAlignment.End,
            isCustomExpanded = false,
        ),
        TimerConfigState(
            loading = false,
            duration = 30.minutes,
            defaultDuration = 45.minutes,
            presets = Defaults.DefaultPresets,
            fabAlignment = FabAlignment.Start,
            isCustomExpanded = true,
        ),
        TimerConfigState(
            loading = true,
            duration = 15.minutes,
            defaultDuration = 45.minutes,
            presets = Defaults.DefaultPresets,
            fabAlignment = FabAlignment.End,
            isCustomExpanded = true,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun TimerConfigContentPreview(
    @PreviewParameter(TimerConfigStateProvider::class) state: TimerConfigState,
) {
    SleepTimerTheme {
        TimerConfigContent(
            component = PreviewTimerConfigComponent(state),
        )
    }
}

// endregion
