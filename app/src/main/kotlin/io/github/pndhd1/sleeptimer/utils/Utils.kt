package io.github.pndhd1.sleeptimer.utils

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.coroutines.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
