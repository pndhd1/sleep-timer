package io.github.pndhd1.sleeptimer

import android.app.Application
import android.content.Context
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory
import io.github.pndhd1.sleeptimer.di.AppGraph
import io.github.pndhd1.sleeptimer.domain.notification.NotificationChannelManager

class SleepTimerApplication : Application() {

    @Inject
    private lateinit var notificationChannelManager: NotificationChannelManager

    lateinit var appGraph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        appGraph = createGraphFactory<AppGraph.Factory>().create(this)
        appGraph.inject(this)
        notificationChannelManager.createChannel()
    }
}

fun Context.requireAppGraph(): AppGraph = (applicationContext as SleepTimerApplication).appGraph
