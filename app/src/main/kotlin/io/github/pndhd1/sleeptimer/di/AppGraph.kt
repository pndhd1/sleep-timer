package io.github.pndhd1.sleeptimer.di

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.pndhd1.sleeptimer.ui.screens.root.RootComponent

@DependencyGraph(AppScope::class)
interface AppGraph {

    val rootComponentFactory: RootComponent.Factory

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides context: Context): AppGraph
    }
}
