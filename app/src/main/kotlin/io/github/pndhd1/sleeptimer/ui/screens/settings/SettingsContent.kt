package io.github.pndhd1.sleeptimer.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.ui.widgets.PresetChip
import io.github.pndhd1.sleeptimer.utils.Defaults
import io.github.pndhd1.sleeptimer.utils.Formatter
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val DefaultDurationSliderMinMinutes = 5
private const val DefaultDurationSliderMaxMinutes = 120
private const val ExtendDurationSliderMinMinutes = 1
private const val ExtendDurationSliderMaxMinutes = 30

@Composable
fun SettingsContent(
    component: SettingsComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsStateWithLifecycle()
    when (val currentState = state) {
        is SettingsState.Loading -> LoadingContent(modifier = modifier.fillMaxSize())
        is SettingsState.Error -> ErrorContent(
            onResetClick = component::onResetSettings,
            modifier = modifier.fillMaxSize(),
        )

        is SettingsState.Loaded -> SettingsLayout(
            state = currentState,
            onDefaultDurationChanged = component::onDefaultDurationChanged,
            onExtendDurationChanged = component::onExtendDurationChanged,
            onPresetAdded = component::onPresetAdded,
            onPresetRemoved = component::onPresetRemoved,
            onShowNotificationChanged = component::onShowNotificationChanged,
            modifier = modifier.fillMaxSize(),
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
    onDefaultDurationChanged: (Duration) -> Unit,
    onExtendDurationChanged: (Duration) -> Unit,
    onPresetAdded: (Duration) -> Unit,
    onPresetRemoved: (Duration) -> Unit,
    onShowNotificationChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DefaultDurationCard(
            duration = state.defaultDuration,
            onDurationChanged = onDefaultDurationChanged,
        )

        ExtendDurationCard(
            duration = state.extendDuration,
            onDurationChanged = onExtendDurationChanged,
        )

        PresetsCard(
            presets = state.presets,
            onPresetAdded = onPresetAdded,
            onPresetRemoved = onPresetRemoved,
        )

        ShowNotificationCard(
            showNotification = state.showNotification,
            onShowNotificationChanged = onShowNotificationChanged,
        )
    }
}

@Composable
private fun DefaultDurationCard(
    duration: Duration,
    onDurationChanged: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showEditDialog by remember { mutableStateOf(false) }

    SettingsCard(
        title = stringResource(R.string.settings_default_duration_title),
        description = stringResource(R.string.settings_default_duration_description),
        modifier = modifier,
    ) {
        DurationSlider(
            duration = duration,
            onDurationChanged = onDurationChanged,
            onEditClick = { showEditDialog = true },
            minMinutes = DefaultDurationSliderMinMinutes,
            maxMinutes = DefaultDurationSliderMaxMinutes,
        )
    }

    if (showEditDialog) {
        DurationEditDialog(
            currentDuration = duration,
            maxDuration = Defaults.MaxTimerDuration,
            onConfirm = { newDuration ->
                onDurationChanged(newDuration)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false },
        )
    }
}

@Composable
private fun ExtendDurationCard(
    duration: Duration,
    onDurationChanged: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showEditDialog by remember { mutableStateOf(false) }

    SettingsCard(
        title = stringResource(R.string.settings_extend_duration_title),
        description = stringResource(R.string.settings_extend_duration_description),
        modifier = modifier,
    ) {
        DurationSlider(
            duration = duration,
            onDurationChanged = onDurationChanged,
            onEditClick = { showEditDialog = true },
            minMinutes = ExtendDurationSliderMinMinutes,
            maxMinutes = ExtendDurationSliderMaxMinutes,
        )
    }

    if (showEditDialog) {
        DurationEditDialog(
            currentDuration = duration,
            maxDuration = Defaults.MaxTimerDuration,
            onConfirm = { newDuration ->
                onDurationChanged(newDuration)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false },
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
            currentDuration = Defaults.MinTimerDuration,
            maxDuration = Defaults.MaxTimerDuration,
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
    onShowNotificationChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_show_notification_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.settings_show_notification_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Switch(
                checked = showNotification,
                onCheckedChange = onShowNotificationChanged,
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
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

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun DurationSlider(
    duration: Duration,
    onDurationChanged: (Duration) -> Unit,
    onEditClick: () -> Unit,
    minMinutes: Int,
    maxMinutes: Int,
    modifier: Modifier = Modifier,
) {
    var sliderValue by remember(duration) {
        mutableFloatStateOf(duration.inWholeMinutes.toFloat())
    }

    // Show slider value during drag, otherwise show actual duration (which might have seconds)
    val sliderMinutes = sliderValue.toInt()
    val durationMinutes = duration.inWholeMinutes.toInt()
    val displayDuration = if (sliderMinutes != durationMinutes) {
        sliderMinutes.minutes
    } else {
        duration
    }

    Column(modifier = modifier) {
        OutlinedButton(
            onClick = onEditClick,
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
            value = sliderValue.coerceIn(minMinutes.toFloat(), maxMinutes.toFloat()),
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onDurationChanged(sliderValue.toInt().minutes) },
            valueRange = minMinutes.toFloat()..maxMinutes.toFloat(),
            steps = (maxMinutes - minMinutes - 1).coerceAtLeast(0),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.settings_duration_min, minMinutes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.settings_duration_min, maxMinutes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DurationEditDialog(
    currentDuration: Duration,
    maxDuration: Duration,
    onConfirm: (Duration) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.settings_edit_duration_title),
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

private const val PresetAnimationDurationMillis = 200

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
        modifier = modifier.fillMaxWidth(),
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
        SettingsState.Loaded(
            defaultDuration = 30.minutes,
            extendDuration = 5.minutes,
            presets = listOf(15.minutes, 30.minutes, 45.minutes, 60.minutes),
            showNotification = true,
        ),
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
        )
    }
}

// endregion
