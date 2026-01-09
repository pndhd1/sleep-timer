package io.github.pndhd1.sleeptimer.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import io.github.pndhd1.sleeptimer.domain.repository.DeviceAdminRepository
import io.github.pndhd1.sleeptimer.requireAppGraph
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TimerAlarmReceiver : BroadcastReceiver() {

    @Inject
    private lateinit var deviceAdminRepository: DeviceAdminRepository

    @Inject
    private lateinit var activeTimerRepository: ActiveTimerRepository

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_TIMER_EXPIRED) return
        context.requireAppGraph().inject(this)

        val pendingResult = goAsync()
        deviceAdminRepository.lockScreen()

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

    companion object {
        const val ACTION_TIMER_EXPIRED = "io.github.pndhd1.sleeptimer.ACTION_TIMER_EXPIRED"
    }
}
