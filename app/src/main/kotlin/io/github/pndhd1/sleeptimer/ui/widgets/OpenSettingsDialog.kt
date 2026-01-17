package io.github.pndhd1.sleeptimer.ui.widgets

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.utils.startActivityCatching

@Composable
fun OpenSettingsDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val settingsIntent = remember {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
    val canOpenSettings = remember {
        settingsIntent.resolveActivity(context.packageManager) != null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            if (canOpenSettings) {
                TextButton(
                    onClick = {
                        context.startActivityCatching(settingsIntent)
                        onDismiss()
                    },
                ) {
                    Text(stringResource(R.string.settings_open_app_settings))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(if (canOpenSettings) R.string.settings_cancel else R.string.dialog_ok))
            }
        },
    )
}
