package io.github.pndhd1.sleeptimer.ui.widgets

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.pndhd1.sleeptimer.R

@Composable
fun GdprConsentDialog(
    onResult: (accepted: Boolean) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismiss by clicking outside */ },
        title = { Text(stringResource(R.string.gdpr_dialog_title)) },
        text = { Text(stringResource(R.string.gdpr_dialog_message)) },
        confirmButton = {
            TextButton(onClick = { onResult(true) }) {
                Text(stringResource(R.string.gdpr_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = { onResult(false) }) {
                Text(stringResource(R.string.gdpr_decline))
            }
        },
    )
}
