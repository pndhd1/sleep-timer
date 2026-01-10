package io.github.pndhd1.sleeptimer.utils

import android.content.BroadcastReceiver
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.coroutines.repeatOnLifecycle
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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
