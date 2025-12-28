package io.github.pndhd1.sleeptimer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface DataStoreModule {

    @Provides
    @SingleIn(AppScope::class)
    fun providePreferencesDataStore(context: Context): DataStore<Preferences> = context.dataStore
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    // Failback to empty preferences on corruption
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)
