package io.github.pndhd1.sleeptimer.di

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.pndhd1.sleeptimer.data.activity.GoHomeActivity
import io.github.pndhd1.sleeptimer.data.receiver.BootCompletedReceiver
import io.github.pndhd1.sleeptimer.data.receiver.DeviceAdminReceiverImpl
import io.github.pndhd1.sleeptimer.data.receiver.TimerAlarmReceiver
import io.github.pndhd1.sleeptimer.data.service.AudioFadeService
import io.github.pndhd1.sleeptimer.SleepTimerApplication
import io.github.pndhd1.sleeptimer.ui.service.TimerNotificationService
import io.github.pndhd1.sleeptimer.ui.activity.MainActivity

@DependencyGraph(AppScope::class)
interface AppGraph {

    fun inject(application: SleepTimerApplication)
    fun inject(receiver: TimerAlarmReceiver)
    fun inject(receiver: BootCompletedReceiver)
    fun inject(receiver: DeviceAdminReceiverImpl)
    fun inject(activity: MainActivity)
    fun inject(activity: GoHomeActivity)
    fun inject(service: TimerNotificationService)
    fun inject(service: AudioFadeService)

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides context: Context): AppGraph
    }
}
