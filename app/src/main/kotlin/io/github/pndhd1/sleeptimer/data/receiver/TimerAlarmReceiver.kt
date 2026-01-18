package io.github.pndhd1.sleeptimer.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.data.service.AudioFadeService
import io.github.pndhd1.sleeptimer.domain.usecase.CompleteTimerUseCase
import io.github.pndhd1.sleeptimer.requireAppGraph
import io.github.pndhd1.sleeptimer.utils.launchAsync

class TimerAlarmReceiver : BroadcastReceiver() {

    @Inject
    private lateinit var completeTimerUseCase: CompleteTimerUseCase

    override fun onReceive(context: Context, intent: Intent?) {
        context.requireAppGraph().inject(this)

        when (intent?.action) {
            ACTION_TIMER_EXPIRED -> {
                launchAsync { completeTimerUseCase() }
            }
            ACTION_FADE_START -> {
                val fadeDurationSeconds = intent.getLongExtra(EXTRA_FADE_DURATION_SECONDS, 3L)
                val targetVolumePercent = intent.getIntExtra(EXTRA_TARGET_VOLUME_PERCENT, 0)
                AudioFadeService.start(context, fadeDurationSeconds, targetVolumePercent)
            }
        }
    }

    companion object {
        const val ACTION_TIMER_EXPIRED = "io.github.pndhd1.sleeptimer.ACTION_TIMER_EXPIRED"
        const val ACTION_FADE_START = "io.github.pndhd1.sleeptimer.ACTION_FADE_START"
        const val EXTRA_FADE_DURATION_SECONDS = "fade_duration_seconds"
        const val EXTRA_TARGET_VOLUME_PERCENT = "target_volume_percent"
    }
}
