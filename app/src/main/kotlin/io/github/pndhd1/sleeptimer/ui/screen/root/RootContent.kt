package io.github.pndhd1.sleeptimer.ui.screen.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.androidPredictiveBackAnimatableV1
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.screen.about.AboutContent
import io.github.pndhd1.sleeptimer.ui.screen.bottomnav.BottomNavContent
import io.github.pndhd1.sleeptimer.ui.screen.root.RootComponent.Child
import io.github.pndhd1.sleeptimer.ui.screen.root.RootComponent.State
import io.github.pndhd1.sleeptimer.ui.widgets.LoadingLayout

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    val stack by component.stack.collectAsStateWithLifecycle()
    val state by component.state.collectAsStateWithLifecycle()

    when (state) {
        is State.Loading -> LoadingLayout(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        )

        is State.GdprConsent -> Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            GdprConsentDialog(onResult = component::onGdprConsentResult)
        }

        is State.Root -> Children(
            stack = stack,
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            animation = predictiveBackAnimation(
                backHandler = component.backHandler,
                fallbackAnimation = stackAnimation(fade()),
                onBack = component::onBackClicked,
                selector = { backEvent, _, _ -> androidPredictiveBackAnimatableV1(backEvent) }
            ),
        ) { child ->
            when (val instance = child.instance) {
                is Child.BottomNav -> BottomNavContent(
                    component = instance.component,
                    modifier = Modifier.fillMaxSize(),
                )

                is Child.About -> AboutContent(
                    component = instance.component,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun GdprConsentDialog(
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

