package io.github.pndhd1.sleeptimer

import android.app.Application
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