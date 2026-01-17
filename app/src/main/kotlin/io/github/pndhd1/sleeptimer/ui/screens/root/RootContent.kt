package io.github.pndhd1.sleeptimer.ui.screens.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import io.github.pndhd1.sleeptimer.ui.screens.about.AboutContent
import io.github.pndhd1.sleeptimer.ui.screens.bottomnav.BottomNavContent
import io.github.pndhd1.sleeptimer.ui.screens.root.RootComponent.Child

@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    val stack by component.stack.collectAsStateWithLifecycle()
    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        animation = stackAnimation(fade()),
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
