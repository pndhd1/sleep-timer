package io.github.pndhd1.sleeptimer.ui.screens.timer

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.slot.ChildSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreviewTimerComponent(
    child: TimerComponent.Child?,
) : TimerComponent {
    override val slot: StateFlow<ChildSlot<Unit, TimerComponent.Child>?> = MutableStateFlow(
        child?.let { ChildSlot(Child.Created(Unit, it)) }
    )
}
