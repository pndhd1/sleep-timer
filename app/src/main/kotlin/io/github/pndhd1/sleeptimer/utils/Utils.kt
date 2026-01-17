package io.github.pndhd1.sleeptimer.utils

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.coroutines.repeatOnLifecycle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Copy of androidx.lifecycle:lifecycle-runtime-ktx's flowWithLifecycle for Decompose's Lifecycle.
 */
fun <T> Flow<T>.flowWithLifecycle(
    lifecycle: Lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
) = callbackFlow<T> {
    lifecycle.repeatOnLifecycle(minActiveState, EmptyCoroutineContext) {
        this@flowWithLifecycle.collect { send(it) }
    }
    close()
}

inline fun <T> runCatchingSuspend(block: () -> T) = runCatching(block).onFailure {
    if (it is CancellationException) throw it
}

fun BroadcastReceiver.launchAsync(job: suspend () -> Unit) {
    val pendingResult = goAsync()
    // BroadcastReceiver does not have a lifecycle, so we need to use GlobalScope
    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.launch {
        try {
            job()
        } finally {
            pendingResult.finish()
        }
    }
}

fun ComponentContext.componentScope(
    context: CoroutineContext = Dispatchers.Main.immediate + SupervisorJob()
) = coroutineScope(context)

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


fun List<Int>.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(size * Int.SIZE_BYTES)
    forEach { buffer.putInt(it) }
    return buffer.array()
}

fun ByteArray.toIntArray(): IntArray {
    val buffer = ByteBuffer.wrap(this)
    return IntArray(size / Int.SIZE_BYTES) { buffer.getInt() }
}

fun Context.startActivityCatching(intent: Intent): Boolean = try {
    startActivity(intent)
    true
} catch (_: ActivityNotFoundException) {
    false
}

fun <I> ActivityResultLauncher<I>.launchCatching(input: I, onError: () -> Unit) {
    try {
        launch(input)
    } catch (_: ActivityNotFoundException) {
        onError()
    }
}

@Composable
@Stable
fun PaddingValues.plus(
    other: PaddingValues,
    layoutDirection: LayoutDirection = LocalLayoutDirection.current,
): PaddingValues {
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + other.calculateStartPadding(layoutDirection),
        top = calculateTopPadding() + other.calculateTopPadding(),
        end = calculateEndPadding(layoutDirection) + other.calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + other.calculateBottomPadding(),
    )
}
