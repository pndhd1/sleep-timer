package io.github.pndhd1.sleeptimer.ui.timer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.pndhd1.sleeptimer.ui.timer.active.DefaultActiveTimerComponent
import io.github.pndhd1.sleeptimer.ui.timer.config.DefaultTimerConfigComponent
import io.github.pndhd1.sleeptimer.ui.timer.config.TimerConfigParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class DefaultTimerComponent(
    componentContext: ComponentContext,
) : TimerComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val slotNavigation = SlotNavigation<SlotConfig>()

    private val childSlot: Value<ChildSlot<SlotConfig, TimerComponent.Child>> = childSlot(
        source = slotNavigation,
        serializer = SlotConfig.serializer(),
        initialConfiguration = { null },
        childFactory = ::createChild,
    )

    override val state: Value<TimerComponent.TimerState> = childSlot.map { slot ->
        if (slot.child != null) {
            TimerComponent.TimerState.Content(childSlot = slot)
        } else {
            TimerComponent.TimerState.Loading
        }
    }

    init {
        scope.launch {
            // TODO: Load last state
            delay(1000)
            slotNavigation.activate(SlotConfig.Config)
        }
    }

    private fun createChild(
        config: SlotConfig,
        componentContext: ComponentContext,
    ): TimerComponent.Child = when (config) {
        SlotConfig.Config -> TimerComponent.Child.Config(
            component = DefaultTimerConfigComponent(
                componentContext = componentContext,
                params = TimerConfigParams(
                    // TODO: Load saved duration
                    duration = 15.minutes,
                    presets = listOf(5.minutes, 10.minutes, 15.minutes, 30.minutes, 60.minutes),
                ),
                onStartTimer = ::onStartTimer,
            ),
        )

        is SlotConfig.Active -> TimerComponent.Child.Active(
            component = DefaultActiveTimerComponent(
                componentContext = componentContext,
                onStop = ::onStopTimer,
            ),
        )
    }

    private fun onStartTimer(duration: Duration) {
        slotNavigation.activate(SlotConfig.Active(duration.inWholeSeconds))
    }

    private fun onStopTimer() {
        slotNavigation.activate(SlotConfig.Config)
    }

    @Serializable
    private sealed interface SlotConfig {

        @Serializable
        data object Config : SlotConfig

        @Serializable
        data class Active(val durationSeconds: Long) : SlotConfig
    }
}
