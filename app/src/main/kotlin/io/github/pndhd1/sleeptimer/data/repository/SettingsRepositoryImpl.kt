package io.github.pndhd1.sleeptimer.data.repository

import androidx.datastore.core.DataStore
import io.github.pndhd1.sleeptimer.data.proto.ProtoSettings
import io.github.pndhd1.sleeptimer.data.proto.copy
import io.github.pndhd1.sleeptimer.domain.model.TimerSettings
import io.github.pndhd1.sleeptimer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SettingsRepositoryImpl(
    private val settingsStore: DataStore<ProtoSettings>,
) : SettingsRepository {

    override val timerSettings: Flow<TimerSettings> = settingsStore.data.map { it.toDomain() }

    override suspend fun updateTimerDefaultDuration(duration: Duration) {
        settingsStore.updateData { current ->
            current.copy { defaultDurationSeconds = duration.inWholeSeconds }
        }
    }

    override suspend fun updateTimerPresets(presets: List<Duration>) {
        settingsStore.updateData { current ->
            current.copy {
                presetSeconds.clear()
                presetSeconds.addAll(presets.map { it.inWholeSeconds })
            }
        }
    }
}

private fun ProtoSettings.toDomain() = TimerSettings(
    defaultDuration = defaultDurationSeconds.seconds,
    presets = presetSecondsList.map { it.seconds },
)
