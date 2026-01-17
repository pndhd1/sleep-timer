package io.github.pndhd1.sleeptimer

import android.app.Application
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.yandex.mobile.ads.common.MobileAds
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
        initLibs()
    }

    private fun initLibs() {
        // Disable Crashlytics collection in debug builds
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        // Disable Ad debug error indicator
        MobileAds.enableDebugErrorIndicator(BuildConfig.DEBUG)
    }
}

fun Context.requireAppGraph(): AppGraph = (applicationContext as SleepTimerApplication).appGraph
