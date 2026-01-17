package io.github.pndhd1.sleeptimer.ui.screens.timer.permission

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.ui.widgets.OpenSettingsDialog
import io.github.pndhd1.sleeptimer.utils.launchCatching

@Composable
fun PermissionContent(
    component: PermissionComponent,
    modifier: Modifier = Modifier,
) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    val intentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { component.onPermissionResult() },
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { component.onPermissionResult() },
    )

    val (titleRes, descriptionRes) = when (component.permissionType) {
        PermissionType.DeviceAdmin -> R.string.permission_device_admin_title to R.string.permission_device_admin_description
        PermissionType.ExactAlarm -> R.string.permission_exact_alarm_title to R.string.permission_exact_alarm_description
        PermissionType.Notification -> R.string.permission_notification_title to R.string.permission_notification_description
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val onError = { showSettingsDialog = true }
                component.getRuntimePermission()?.let {
                    permissionLauncher.launchCatching(it, onError)
                }
                component.getActivationIntent()?.let {
                    intentLauncher.launchCatching(it, onError)
                }
            },
        ) {
            Text(text = stringResource(R.string.permission_grant_button))
        }
    }

    if (showSettingsDialog) {
        OpenSettingsDialog(
            title = stringResource(R.string.settings_permission_unavailable_title),
            message = stringResource(R.string.settings_permission_unavailable_message),
            onDismiss = { showSettingsDialog = false },
        )
    }
}

// region Preview

private class PermissionTypePreviewProvider : PreviewParameterProvider<PermissionType> {
    override val values = PermissionType.entries.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun PermissionContentPreview(
    @PreviewParameter(PermissionTypePreviewProvider::class) permissionType: PermissionType,
) {
    SleepTimerTheme {
        PermissionContent(
            component = PreviewPermissionComponent(permissionType),
        )
    }
}

// endregion
