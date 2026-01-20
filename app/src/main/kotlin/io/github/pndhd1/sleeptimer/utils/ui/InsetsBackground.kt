package io.github.pndhd1.sleeptimer.utils.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

private const val SemiTransparentBackgroundAlpha = 0.9f

@Composable
fun SolidInsetsBackground(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) = Box(modifier = modifier.background(color))

@Composable
fun SolidInsetsBackground(
    visible: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) = AnimatedInsetsBackground(
    visible = visible,
    modifier = modifier,
    color = color,
    solid = true,
)

@Composable
fun InsetsBackground(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) = Box(modifier = modifier.background(color.copy(alpha = SemiTransparentBackgroundAlpha)))

@Composable
fun InsetsBackground(
    visible: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) = AnimatedInsetsBackground(
    visible = visible,
    modifier = modifier,
    color = color,
    solid = false,
)

@Composable
private fun AnimatedInsetsBackground(
    visible: Boolean,
    color: Color,
    solid: Boolean,
    modifier: Modifier,
) {
    val alpha by animateFloatAsState(
        when {
            !visible -> 0f
            solid -> 1f
            else -> SemiTransparentBackgroundAlpha
        }
    )
    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .background(color),
    )
}
