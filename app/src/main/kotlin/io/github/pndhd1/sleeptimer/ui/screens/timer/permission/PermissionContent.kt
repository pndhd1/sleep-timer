package io.github.pndhd1.sleeptimer.ui.screens.timer.permission

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme

@Composable
fun PermissionContent(
    component: PermissionComponent,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { component.onPermissionResult() },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.permission_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.permission_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                @SuppressLint("LocalContextGetResourceValueCall") // we do not need this string in composition
                val explanation = context.getString(R.string.permission_system_explanation)
                launcher.launch(component.getActivationIntent(explanation))
            }
        ) {
            Text(text = stringResource(R.string.permission_grant_button))
        }
    }
}

// region Preview

@Preview(showBackground = true)
@Composable
private fun PermissionContentPreview() {
    SleepTimerTheme {
        PermissionContent(
            component = PreviewPermissionComponent(),
        )
    }
}

// endregion
