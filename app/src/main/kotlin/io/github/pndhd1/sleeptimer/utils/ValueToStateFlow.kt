package io.github.pndhd1.sleeptimer.utils

import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun <T : Any> Value<T>.toStateFlow(
    lifecycle: Lifecycle,
    mode: ObserveLifecycleMode = ObserveLifecycleMode.START_STOP
): StateFlow<T> {
    val stateFlow = MutableStateFlow(value)
    subscribe(lifecycle, mode) { stateFlow.value = it }
    return stateFlow
}

context(owner: LifecycleOwner)
fun <T : Any> Value<T>.toStateFlow(
    mode: ObserveLifecycleMode = ObserveLifecycleMode.START_STOP
): StateFlow<T> = toStateFlow(owner.lifecycle, mode)
