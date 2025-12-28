package io.github.pndhd1.sleeptimer.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.pndhd1.sleeptimer.domain.model.ActiveTimerData
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

private val TargetTimeKey = longPreferencesKey("target_time_epoch_seconds")

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ActiveTimerRepositoryImpl(
    private val preferences: DataStore<Preferences>,
) : ActiveTimerRepository {

    override val activeTimer: Flow<ActiveTimerData?> =
        preferences.data.map(Preferences::toDomain)

    override suspend fun startTimer(targetTime: Instant) {
        preferences.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                this[TargetTimeKey] = targetTime.epochSeconds
            }
        }
    }

    override suspend fun clearTimer() {
        preferences.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                this.remove(TargetTimeKey)
            }
        }
    }
}

private fun Preferences.toDomain(): ActiveTimerData? {
    val targetTimeSeconds = this[TargetTimeKey] ?: return null
    return ActiveTimerData(
        targetTime = Instant.fromEpochSeconds(targetTimeSeconds),
    )
}
