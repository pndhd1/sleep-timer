package io.github.pndhd1.sleeptimer.ui.widgets

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.utils.Formatter
import kotlin.time.Duration

@Composable
fun PresetChip(
    duration: Duration,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showRemoveIcon: Boolean = false,
) {
    val label = Formatter.formatTimeWithUnits(duration)

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
