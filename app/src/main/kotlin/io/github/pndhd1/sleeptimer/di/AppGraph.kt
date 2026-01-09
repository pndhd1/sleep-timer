package io.github.pndhd1.sleeptimer.di

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.pndhd1.sleeptimer.data.receiver.BootCompletedReceiver
import io.github.pndhd1.sleeptimer.data.receiver.DeviceAdminReceiverImpl
import io.github.pndhd1.sleeptimer.data.receiver.TimerAlarmReceiver
import io.github.pndhd1.sleeptimer.data.service.TimerService
import io.github.pndhd1.sleeptimer.ui.MainActivity

@DependencyGraph(AppScope::class)
interface AppGraph {

    fun inject(receiver: TimerAlarmReceiver)
    fun inject(receiver: BootCompletedReceiver)
    fun inject(receiver: DeviceAdminReceiverImpl)
    fun inject(activity: MainActivity)
    fun inject(service: TimerService)

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides context: Context): AppGraph
    }
}
