package io.github.pndhd1.sleeptimer.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import io.github.pndhd1.sleeptimer.requireAppGraph
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    private lateinit var activeTimerRepository: ActiveTimerRepository

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        context.requireAppGraph().inject(this)

        val pendingResult = goAsync()

        // BroadcastReceiver does not have a lifecycle, so we need to use GlobalScope
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                activeTimerRepository.clearTimer()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
