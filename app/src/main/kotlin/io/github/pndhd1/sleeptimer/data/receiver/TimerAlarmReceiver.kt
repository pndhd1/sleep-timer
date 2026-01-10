package io.github.pndhd1.sleeptimer.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import io.github.pndhd1.sleeptimer.domain.repository.DeviceAdminRepository
import io.github.pndhd1.sleeptimer.requireAppGraph
import io.github.pndhd1.sleeptimer.utils.launchAsync

class TimerAlarmReceiver : BroadcastReceiver() {

    @Inject
    private lateinit var deviceAdminRepository: DeviceAdminRepository

    @Inject
    private lateinit var activeTimerRepository: ActiveTimerRepository

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_TIMER_EXPIRED) return
        context.requireAppGraph().inject(this)

        deviceAdminRepository.lockScreen()
        launchAsync { activeTimerRepository.clearTimer() }
    }

    companion object {
        const val ACTION_TIMER_EXPIRED = "io.github.pndhd1.sleeptimer.ACTION_TIMER_EXPIRED"
    }
}
