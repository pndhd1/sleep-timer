package io.github.pndhd1.sleeptimer.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.usecase.CompleteTimerUseCase
import io.github.pndhd1.sleeptimer.requireAppGraph
import io.github.pndhd1.sleeptimer.utils.launchAsync

class TimerAlarmReceiver : BroadcastReceiver() {

    @Inject
    private lateinit var completeTimerUseCase: CompleteTimerUseCase

    override fun onReceive(context: Context, intent: Intent?) {
        context.requireAppGraph().inject(this)

        launchAsync {
            when (intent?.action) {
                ACTION_TIMER_EXPIRED -> completeTimerUseCase()
            }
        }
    }

    companion object {
        const val ACTION_TIMER_EXPIRED = "io.github.pndhd1.sleeptimer.ACTION_TIMER_EXPIRED"
    }
}
