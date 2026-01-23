package io.github.pndhd1.sleeptimer

import android.app.Application
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory
import io.github.pndhd1.sleeptimer.di.AppGraph
import io.github.pndhd1.sleeptimer.domain.notification.NotificationChannelManager
import io.github.pndhd1.sleeptimer.utils.YandexAdsState

class SleepTimerApplication : Application() {

    lateinit var appGraph: AppGraph
        private set

    @Inject
    private lateinit var notificationChannelManager: NotificationChannelManager

    override fun onCreate() {
        super.onCreate()
        appGraph = createGraphFactory<AppGraph.Factory>().create(this)
        appGraph.inject(this)
        notificationChannelManager.createChannels()
        initLibs()
    }

    private fun initLibs() {
        // Disable Crashlytics collection in debug builds
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        YandexAdsState.initialize(this)
    }
}

fun Context.requireAppGraph(): AppGraph = (applicationContext as SleepTimerApplication).appGraph
