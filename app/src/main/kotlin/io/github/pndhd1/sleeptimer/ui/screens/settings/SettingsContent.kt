package io.github.pndhd1.sleeptimer.ui.screens.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.domain.model.FadeOutSettings
import io.github.pndhd1.sleeptimer.ui.screens.bottomnav.widgets.BottomNavAdBannerState
import io.github.pndhd1.sleeptimer.ui.screens.bottomnav.widgets.rememberBottomNavAdBannerState
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.ui.widgets.OpenSettingsDialog
import io.github.pndhd1.sleeptimer.ui.widgets.PresetChip
import io.github.pndhd1.sleeptimer.utils.Defaults
import io.github.pndhd1.sleeptimer.utils.Formatter
import io.github.pndhd1.sleeptimer.utils.ui.UIDefaults
import io.github.pndhd1.sleeptimer.utils.ui.UIDefaults.SystemBarsBackgroundColor
import io.github.pndhd1.sleeptimer.utils.ui.VisibilityCrossfade
import kotlinx.coroutines.delay
import kotlin.math.round
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private inline val DefaultDurationSliderMin get() = 5.minutes
private inline val DefaultDurationSliderMax get() = 120.minutes
private inline val DefaultDurationSliderStep get() = 1.minutes

private inline val ExtendDurationSliderMin get() = 1.minutes
private inline val ExtendDurationSliderMax get() = 30.minutes
private inline val ExtendDurationSliderStep get() = 30.seconds

private inline val FadeStartBeforeSliderMin get() = 15.seconds
private inline val FadeStartBeforeSliderMax get() = 5.minutes
private inline val FadeStartBeforeSliderStep get() = 15.seconds

private inline val FadeDurationSliderMin get() = 1.seconds
private inline val FadeDurationSliderMax get() = 1.minutes
private inline val FadeDurationSliderStep get() = 1.seconds

private const val TargetVolumeSliderMin = 0
private const val TargetVolumeSliderMax = 100
private const val TargetVolumeSliderSteps = 9

private const val PresetAnimationDurationMillis = 200

@Composable
fun SettingsContent(
    component: SettingsComponent,
    bannerState: BottomNavAdBannerState,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    var showPermissionSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            component.onNotificationPermissionResult(granted)
            if (!granted) {
                val permission = component.getNotificationPermission()
                val shouldShowRationale = permission != null && activity != null &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                if (shouldShowRationale) {
                    showPermissionSettingsDialog = true
                }
            }
        },
    )

    val layoutModifier = modifier
        .background(MaterialTheme.colorScheme.surface)
        .fillMaxSize()

    when (val currentState = state) {
        is SettingsState.Loading -> LoadingContent(modifier = layoutModifier)
        is SettingsState.Error -> ErrorContent(
            onResetClick = component::onResetSettings,
            modifier = layoutModifier,
        )

        is SettingsState.Loaded -> SettingsLayout(
            state = currentState,
            component = component,
            bannerState = bannerState,
            onRequestNotificationPermission = {
                component.getNotificationPermission()?.let(permissionLauncher::launch)
            },
            modifier = layoutModifier,
        )
    }

    if (showPermissionSettingsDialog) {
        OpenSettingsDialog(
            title = stringResource(R.string.settings_notification_permission_title),
            message = stringResource(R.string.settings_notification_permission_message),
            onDismiss = { showPermissionSettingsDialog = false },
        )
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.error_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onResetClick) {
            Text(stringResource(R.string.settings_reset))
        }
    }
}

@Composable
private fun SettingsLayout(
    state: SettingsState.Loaded,
    component: SettingsComponent,
    bannerState: BottomNavAdBannerState,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = run {
                    val bannerSize = bannerState.bannerSize
                    if (bannerState.isBannerVisible && bannerSize != null) {
                        bannerSize.height.dp
                    } else {
                        0.dp
                    }
                },
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(
                    modifier = Modifier.windowInsetsTopHeight(UIDefaults.defaultInsets)
                )
            }
            item {
                DefaultDurationCard(
                    duration = state.defaultDuration,
                    onDurationChanged = component::onDefaultDurationChanged,
                )
            }
            item {
                ExtendDurationCard(
                    duration = state.extendDuration,
                    onDurationChanged = component::onExtendDurationChanged,
                )
            }
            item {
                PresetsCard(
                    presets = state.presets,
                    onPresetAdded = component::onPresetAdded,
                    onPresetRemoved = component::onPresetRemoved,
                )
            }
            item {
                ShowNotificationCard(
                    showNotification = state.showNotification,
                    hasPermission = state.hasNotificationPermission,
                    onShowNotificationChanged = component::onShowNotificationChanged,
                    onRequestPermission = onRequestNotificationPermission,
                )
            }
            item {
                FadeOutCard(
                    fadeOut = state.fadeOut,
                    onFadeOutEnabledChanged = component::onFadeOutEnabledChanged,
                    onFadeOutStartBeforeChanged = component::onFadeOutStartBeforeChanged,
                    onFadeOutDurationChanged = component::onFadeOutDurationChanged,
                    onFadeTargetVolumePercentChanged = component::onFadeTargetVolumePercentChanged,
                )
            }
            item {
                StopMediaOnExpireCard(
                    enabled = state.stopMediaOnExpire,
                    onEnabledChanged = component::onStopMediaOnExpireChanged,
                )
            }
            item {
                GoHomeOnExpireCard(
                    enabled = state.goHomeOnExpire,
                    onEnabledChanged = component::onGoHomeOnExpireChanged,
                )
            }
            item {
                AboutCard(onClick = component::onAboutClick)
            }
            item {
                Spacer(
                    modifier = Modifier.windowInsetsBottomHeight(UIDefaults.defaultInsets)
                )
            }
        }

        VisibilityCrossfade(
            isVisible = listState.canScrollBackward,
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Box(
                modifier = Modifier
                    .windowInsetsTopHeight(UIDefaults.defaultInsets)
                    .fillMaxWidth()
                    .background(SystemBarsBackgroundColor)
            )
        }

        VisibilityCrossfade(
            isVisible = listState.canScrollForward,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Box(
                modifier = Modifier
                    .windowInsetsBottomHeight(UIDefaults.defaultInsets)
                    .fillMaxWidth()
                    .background(SystemBarsBackgroundColor)
            )
        }
    }
}

@Composable
private fun GoHomeOnExpireCard(
    enabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = stringResource(R.string.settings_go_home_title),
        description = stringResource(R.string.settings_go_home_description),
        modifier = modifier,
        endContent = {
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChanged,
            )
        },
    )
}

@Composable
private fun AboutCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = stringResource(R.string.settings_about_title),
        description = stringResource(R.string.settings_about_description),
        modifier = modifier,
        onClick = onClick,
        startContent = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_info),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        endContent = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

@Composable
private fun DefaultDurationCard(
    duration: Duration,
    onDurationChanged: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = stringResource(R.string.settings_default_duration_title),
        description = stringResource(R.string.settings_default_duration_description),
        modifier = modifier,
    ) {
        DurationSlider(
            duration = duration,
            onDurationChanged = onDurationChanged,
            minDuration = DefaultDurationSliderMin,
            maxDuration = DefaultDurationSliderMax,
            step = DefaultDurationSliderStep,
        )
    }
}

@Composable
private fun ExtendDurationCard(
    duration: Duration,
    onDurationChanged: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = stringResource(R.string.settings_extend_duration_title),
        description = stringResource(R.string.settings_extend_duration_description),
        modifier = modifier,
    ) {
        DurationSlider(
            duration = duration,
            onDurationChanged = onDurationChanged,
            minDuration = ExtendDurationSliderMin,
            maxDuration = ExtendDurationSliderMax,
            step = ExtendDurationSliderStep,
        )
    }
}

@Composable
private fun PresetsCard(
    presets: List<Duration>,
    onPresetAdded: (Duration) -> Unit,
    onPresetRemoved: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    SettingsCard(
        title = stringResource(R.string.settings_presets_title),
        description = stringResource(R.string.settings_presets_description),
        modifier = modifier,
    ) {
        PresetChips(
            presets = presets,
            onPresetRemoved = onPresetRemoved,
            onAddClick = { showAddDialog = true },
        )
    }

    if (showAddDialog) {
        DurationEditDialog(
            currentDuration = Duration.ZERO,
            title = stringResource(R.string.settings_add_preset_title),
            onConfirm = { duration ->
                if (duration !in presets && duration >= Defaults.MinTimerDuration) {
                    onPresetAdded(duration)
                }
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun ShowNotificationCard(
    showNotification: Boolean,
    hasPermission: Boolean,
    onShowNotificationChanged: (Boolean) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = stringResource(R.string.settings_show_notification_title),
        description = stringResource(R.string.settings_show_notification_description),
        modifier = modifier,
        endContent = {
            Switch(
                checked = showNotification && hasPermission,
                onCheckedChange = { enabled ->
                    if (enabled && !hasPermission) {
                        onRequestPermission()
                    } else {
                        onShowNotificationChanged(enabled)
                    }
                },
            )
        },
    )
}

@Composable
private fun FadeOutCard(
    fadeOut: FadeOutSettings,
    onFadeOutEnabledChanged: (Boolean) -> Unit,
    onFadeOutStartBeforeChanged: (Duration) -> Unit,
    onFadeOutDurationChanged: (Duration) -> Unit,
    onFadeTargetVolumePercentChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = stringResource(R.string.settings_fade_out_title),
        description = stringResource(R.string.settings_fade_out_description),
        modifier = modifier,
        endContent = {
            Switch(
                checked = fadeOut.enabled,
                onCheckedChange = onFadeOutEnabledChanged,
            )
        },
        content = {
            AnimatedVisibility(visible = fadeOut.enabled) {
                Column {
                    Text(
                        text = stringResource(R.string.settings_fade_start_before_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DurationSlider(
                        duration = fadeOut.startBefore,
                        onDurationChanged = onFadeOutStartBeforeChanged,
                        minDuration = FadeStartBeforeSliderMin,
                        maxDuration = FadeStartBeforeSliderMax,
                        step = FadeStartBeforeSliderStep,
                        dialogMinDuration = FadeStartBeforeSliderMin,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.settings_fade_duration_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DurationSlider(
                        duration = fadeOut.duration,
                        onDurationChanged = onFadeOutDurationChanged,
                        minDuration = FadeDurationSliderMin,
                        maxDuration = FadeDurationSliderMax,
                        step = FadeDurationSliderStep,
                        dialogMinDuration = FadeDurationSliderMin,
                        dialogMaxDuration = FadeDurationSliderMax,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.settings_fade_target_volume_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PercentSlider(
                        percent = fadeOut.targetVolumePercent,
                        onPercentChanged = onFadeTargetVolumePercentChanged,
                    )
                }
            }
        },
    )
}

@Composable
private fun StopMediaOnExpireCard(
    enabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = stringResource(R.string.settings_stop_media_title),
        description = stringResource(R.string.settings_stop_media_description),
        modifier = modifier,
        endContent = {
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChanged,
            )
        },
    )
}

@Composable
private fun PercentSlider(
    percent: Int,
    onPercentChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sliderValue by remember(percent) {
        mutableFloatStateOf(percent.toFloat())
    }

    Column(modifier = modifier) {
        Text(
            text = "${sliderValue.toInt()}%",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = round(it) },
            onValueChangeFinished = { onPercentChanged(sliderValue.toInt()) },
            valueRange = TargetVolumeSliderMin.toFloat()..TargetVolumeSliderMax.toFloat(),
            steps = TargetVolumeSliderSteps,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "$TargetVolumeSliderMin%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$TargetVolumeSliderMax%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    startContent: @Composable (() -> Unit)? = null,
    endContent: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    val cardModifier = modifier.fillMaxWidth()
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            colors = cardColors,
        ) {
            SettingsCardContent(title, description, startContent, endContent, content)
        }
    } else {
        Card(
            modifier = cardModifier,
            colors = cardColors,
        ) {
            SettingsCardContent(title, description, startContent, endContent, content)
        }
    }
}

@Composable
private fun SettingsCardContent(
    title: String,
    description: String,
    startContent: @Composable (() -> Unit)?,
    endContent: @Composable (RowScope.() -> Unit)?,
    content: @Composable (ColumnScope.() -> Unit)?,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (startContent != null) {
                startContent()
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(modifier = if (endContent != null || startContent != null) Modifier.weight(1f) else Modifier) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            endContent?.invoke(this)
        }

        if (content != null) {
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun DurationSlider(
    duration: Duration,
    onDurationChanged: (Duration) -> Unit,
    minDuration: Duration,
    maxDuration: Duration,
    modifier: Modifier = Modifier,
    step: Duration? = null,
    dialogMaxDuration: Duration = Defaults.MaxTimerDuration,
    dialogMinDuration: Duration = Defaults.MinTimerDuration,
) {
    var showDialog by remember { mutableStateOf(false) }

    val minSeconds = minDuration.inWholeSeconds.toFloat()
    val maxSeconds = maxDuration.inWholeSeconds.toFloat()

    var sliderValue by remember(duration) {
        mutableFloatStateOf(duration.inWholeSeconds.toFloat())
    }

    // Show slider value during drag, otherwise show actual duration
    val sliderSeconds = sliderValue.toInt()
    val durationSeconds = duration.inWholeSeconds.toInt()
    val displayDuration = if (sliderSeconds != durationSeconds) {
        sliderSeconds.seconds
    } else {
        duration
    }

    val steps = step?.let {
        val range = (maxSeconds - minSeconds).toInt()
        val stepSeconds = it.inWholeSeconds.toInt()
        if (stepSeconds > 0) (range / stepSeconds - 1).coerceAtLeast(0) else 0
    } ?: 0

    Column(modifier = modifier) {
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(
                text = Formatter.formatTime(displayDuration),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = sliderValue.coerceIn(minSeconds, maxSeconds),
            onValueChange = { sliderValue = round(it) },
            onValueChangeFinished = { onDurationChanged(sliderValue.toInt().seconds) },
            valueRange = minSeconds..maxSeconds,
            steps = steps,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = Formatter.formatTimeWithUnits(minDuration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = Formatter.formatTimeWithUnits(maxDuration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showDialog) {
        DurationEditDialog(
            currentDuration = duration,
            maxDuration = dialogMaxDuration,
            minDuration = dialogMinDuration,
            onConfirm = { newDuration ->
                onDurationChanged(newDuration)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun DurationEditDialog(
    currentDuration: Duration,
    onConfirm: (Duration) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.settings_edit_duration_title),
    maxDuration: Duration = Defaults.MaxTimerDuration,
    minDuration: Duration = Defaults.MinTimerDuration,
) {
    var hours by remember { mutableStateOf(currentDuration.inWholeHours.toString()) }
    var minutes by remember { mutableStateOf((currentDuration.inWholeMinutes % 60).toString()) }
    var seconds by remember { mutableStateOf((currentDuration.inWholeSeconds % 60).toString()) }

    val parsedHours = hours.toIntOrNull() ?: 0
    val parsedMinutes = minutes.toIntOrNull() ?: 0
    val parsedSeconds = seconds.toIntOrNull() ?: 0
    val totalDuration = parsedHours.hours + parsedMinutes.minutes + parsedSeconds.seconds
    val isTooShort = totalDuration < minDuration
    val isTooLong = totalDuration > maxDuration
    val isValid = !isTooShort && !isTooLong

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { newValue ->
                            hours = newValue.filter { it.isDigit() }.take(2)
                        },
                        label = { Text(stringResource(R.string.label_hours_short), maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(72.dp),
                    )

                    Text(":", style = MaterialTheme.typography.headlineMedium)

                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { newValue ->
                            minutes = newValue.filter { it.isDigit() }.take(2)
                        },
                        label = {
                            Text(
                                stringResource(R.string.label_minutes_short),
                                maxLines = 1
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(72.dp),
                    )

                    Text(":", style = MaterialTheme.typography.headlineMedium)

                    OutlinedTextField(
                        value = seconds,
                        onValueChange = { newValue ->
                            seconds = newValue.filter { it.isDigit() }.take(2)
                        },
                        label = {
                            Text(
                                stringResource(R.string.label_seconds_short),
                                maxLines = 1
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(72.dp),
                    )
                }

                if (!isValid) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            isTooShort -> stringResource(
                                R.string.settings_min_duration_error,
                                Formatter.formatTime(minDuration),
                            )

                            else -> stringResource(
                                R.string.settings_max_duration_error,
                                Formatter.formatTime(maxDuration),
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(totalDuration) },
                enabled = isValid,
            ) {
                Text(stringResource(R.string.settings_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetChips(
    presets: List<Duration>,
    onPresetRemoved: (Duration) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Track items with visibility state for enter/exit animations
    var items by remember { mutableStateOf(presets.map { it to true }) }

    // Sync display items with presets
    LaunchedEffect(presets) {
        val presetsSet = presets.toSet()
        val existingDurations = items.map { it.first }.toSet()

        // Mark removed items as invisible for exit animation
        items = items.map { (duration, _) ->
            duration to (duration in presetsSet)
        }

        // Add new items
        val newItems = presets
            .filter { it !in existingDurations }
            .map { it to true }
        items = items + newItems
    }

    // Clean up invisible items after animation completes
    LaunchedEffect(items) {
        val hasInvisible = items.any { !it.second }
        if (hasInvisible) {
            delay(PresetAnimationDurationMillis.toLong())
            items = items.filter { it.second }
        }
    }

    FlowRow(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { (preset, visible) ->
            key(preset) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(PresetAnimationDurationMillis)) +
                        scaleIn(tween(PresetAnimationDurationMillis), initialScale = 0.8f),
                    exit = fadeOut(tween(PresetAnimationDurationMillis)) +
                        scaleOut(tween(PresetAnimationDurationMillis), targetScale = 0.8f),
                ) {
                    PresetChip(
                        duration = preset,
                        onClick = { onPresetRemoved(preset) },
                        showRemoveIcon = true,
                    )
                }
            }
        }

        FilledTonalIconButton(onClick = onAddClick) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.settings_add_preset),
            )
        }
    }
}

// region Preview

private class SettingsStateProvider : PreviewParameterProvider<SettingsState> {
    override val values = sequenceOf(
        SettingsState.Loading,
        SettingsState.Error,
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview(
    @PreviewParameter(SettingsStateProvider::class) state: SettingsState,
) {
    SleepTimerTheme {
        SettingsContent(
            component = PreviewSettingsComponent(state),
            bannerState = rememberBottomNavAdBannerState(),
        )
    }
}

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun SettingsContentLoadedPreview() {
    val state = SettingsState.Loaded(
        defaultDuration = 30.minutes,
        extendDuration = 5.minutes,
        presets = listOf(15.minutes, 30.minutes, 45.minutes, 60.minutes),
        showNotification = true,
        hasNotificationPermission = true,
        fadeOut = FadeOutSettings(
            enabled = true,
            startBefore = 1.minutes,
            duration = 3.seconds,
            targetVolumePercent = 0,
        ),
        goHomeOnExpire = false,
        stopMediaOnExpire = false,
    )
    SleepTimerTheme {
        SettingsContent(
            component = PreviewSettingsComponent(state),
            bannerState = rememberBottomNavAdBannerState(),
        )
    }
}

// endregion
