package io.github.pndhd1.sleeptimer.ui.screens.bottomnav

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.MutableValue
import io.github.pndhd1.sleeptimer.utils.toStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreviewBottomNavComponent(
    child: BottomNavComponent.Child,
) : BottomNavComponent {
    override val stack: StateFlow<ChildStack<Unit, BottomNavComponent.Child>> =
        MutableValue(
            ChildStack(
                configuration = Unit,
                instance = child,
            )
        ).toStateFlow()

    override fun onTimerTabClick() {}
    override fun onSettingsTabClick() {}
}
