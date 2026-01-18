package io.github.pndhd1.sleeptimer.ui.screen.timer

import com.arkivanov.decompose.router.slot.ChildSlot
import io.github.pndhd1.sleeptimer.ui.screen.timer.active.ActiveTimerComponent
import io.github.pndhd1.sleeptimer.ui.screen.timer.config.TimerConfigComponent
import io.github.pndhd1.sleeptimer.ui.screen.timer.permission.PermissionComponent
import kotlinx.coroutines.flow.StateFlow

interface TimerComponent {

    val slot: StateFlow<ChildSlot<*, Child>?>

    sealed interface Child {
        data object Error : Child

        data class Permission(val component: PermissionComponent) : Child

        data class Config(val component: TimerConfigComponent) : Child

        data class Active(val component: ActiveTimerComponent) : Child
    }
}

