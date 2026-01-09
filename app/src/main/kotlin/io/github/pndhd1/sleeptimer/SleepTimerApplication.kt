package io.github.pndhd1.sleeptimer

import android.app.Application
import android.content.Context
import dev.zacsweers.metro.createGraphFactory
import io.github.pndhd1.sleeptimer.di.AppGraph

class SleepTimerApplication : Application() {

    lateinit var appGraph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        appGraph = createGraphFactory<AppGraph.Factory>().create(this)
    }
}

fun Context.requireAppGraph(): AppGraph = (applicationContext as SleepTimerApplication).appGraph
