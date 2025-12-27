package io.github.pndhd1.sleeptimer.ui.timer.active

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.pndhd1.sleeptimer.R

@Composable
fun ActiveTimerContent(
    component: ActiveTimerComponent,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Active Timer",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Coming soon...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        FilledTonalButton(
            onClick = component::onStopClick,
            modifier = Modifier.height(56.dp),
        ) {
            Text(
                text = stringResource(R.string.button_stop),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
