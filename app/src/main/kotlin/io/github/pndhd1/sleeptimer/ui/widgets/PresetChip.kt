package io.github.pndhd1.sleeptimer.ui.widgets

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import io.github.pndhd1.sleeptimer.R
import kotlin.time.Duration

@Composable
fun PresetChip(
    duration: Duration,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showRemoveIcon: Boolean = false,
) {
    val label = formatPresetLabel(duration)

    if (showRemoveIcon) {
        InputChip(
            selected = false,
            onClick = onClick,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.settings_remove_preset),
                    modifier = Modifier.height(InputChipDefaults.IconSize),
                )
            },
            modifier = modifier,
        )
    } else {
        FilterChip(
            selected = false,
            onClick = onClick,
            label = { Text(label) },
            modifier = modifier,
        )
    }
}

@Composable
private fun formatPresetLabel(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60
    val seconds = duration.inWholeSeconds % 60

    return buildString {
        if (hours > 0) append(stringResource(R.string.preset_hours, hours.toInt()))
        if (minutes > 0) {
            if (isNotEmpty()) append(" ")
            append(stringResource(R.string.preset_minutes, minutes.toInt()))
        }
        if (seconds > 0) {
            if (isNotEmpty()) append(" ")
            append(stringResource(R.string.preset_seconds, seconds.toInt()))
        }
        if (isEmpty()) append(stringResource(R.string.preset_seconds, 0))
    }
}
