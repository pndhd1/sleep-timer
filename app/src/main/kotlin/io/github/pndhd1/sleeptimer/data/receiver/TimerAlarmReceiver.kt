package io.github.pndhd1.sleeptimer.data.receiver

import android.app.PendingIntent
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

        if (intent?.action == ACTION_TIMER_EXPIRED) launchAsync {
            completeTimerUseCase()
        }
    }

    companion object {
        private const val ACTION_TIMER_EXPIRED = "io.github.pndhd1.sleeptimer.ACTION_TIMER_EXPIRED"

        fun getPendingIntent(context: Context, requestCode: Int): PendingIntent {
            val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
                action = ACTION_TIMER_EXPIRED
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
