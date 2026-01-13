package io.github.pndhd1.sleeptimer.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.usecase.StopTimerUseCase
import io.github.pndhd1.sleeptimer.requireAppGraph
import io.github.pndhd1.sleeptimer.utils.launchAsync

class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    private lateinit var stopTimerUseCase: StopTimerUseCase

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        context.requireAppGraph().inject(this)
        launchAsync { stopTimerUseCase() }
    }
}
