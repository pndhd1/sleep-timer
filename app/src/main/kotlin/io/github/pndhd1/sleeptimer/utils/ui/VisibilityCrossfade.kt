package io.github.pndhd1.sleeptimer.utils.ui

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A composable that crossfades its content based on the visibility state.
 * Respects initial state, AnimatedVisibility does not.
 */
@Composable
fun VisibilityCrossfade(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Crossfade(
        targetState = isVisible,
        modifier = modifier,
    ) { visible ->
        if (visible) content()
    }
}
