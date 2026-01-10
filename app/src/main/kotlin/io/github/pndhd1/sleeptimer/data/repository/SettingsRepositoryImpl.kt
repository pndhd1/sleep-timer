package io.github.pndhd1.sleeptimer.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.pndhd1.sleeptimer.domain.model.TimerSettings
import io.github.pndhd1.sleeptimer.domain.repository.SettingsRepository
import io.github.pndhd1.sleeptimer.utils.Defaults
import io.github.pndhd1.sleeptimer.utils.toByteArray
import io.github.pndhd1.sleeptimer.utils.toIntArray
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val DefaultDurationSecondsKey = intPreferencesKey("default_duration_seconds")
private val PresetSecondsKey = byteArrayPreferencesKey("preset_seconds")
private val ExtendDurationSecondsKey = intPreferencesKey("extend_duration_seconds")
private val ShowNotificationKey = booleanPreferencesKey("show_notification")

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SettingsRepositoryImpl(
    private val preferences: DataStore<Preferences>,
) : SettingsRepository {

    override val timerSettings: Flow<TimerSettings> = preferences.data.map { it.toDomain() }

    override suspend fun updateTimerDefaultDuration(duration: Duration) {
        preferences.updateData { current ->
            current.toMutablePreferences().apply {
                this[DefaultDurationSecondsKey] = duration.inWholeSeconds.toInt()
            }
        }
    }

    override suspend fun updateTimerPresets(presets: List<Duration>) {
        preferences.updateData { current ->
            current.toMutablePreferences().apply {
                this[PresetSecondsKey] = presets.map { it.inWholeSeconds.toInt() }.toByteArray()
            }
        }
    }

    override suspend fun updateExtendDuration(duration: Duration) {
        preferences.updateData { current ->
            current.toMutablePreferences().apply {
                this[ExtendDurationSecondsKey] = duration.inWholeSeconds.toInt()
            }
        }
    }

    override suspend fun updateShowNotification(show: Boolean) {
        preferences.updateData { current ->
            current.toMutablePreferences().apply {
                this[ShowNotificationKey] = show
            }
        }
    }

    override suspend fun resetToDefaults() {
        preferences.updateData { current ->
            current.toMutablePreferences().apply {
                remove(DefaultDurationSecondsKey)
                remove(PresetSecondsKey)
                remove(ExtendDurationSecondsKey)
                remove(ShowNotificationKey)
            }
        }
    }
}

private fun Preferences.toDomain() = TimerSettings(
    defaultDuration = get(DefaultDurationSecondsKey)?.seconds ?: Defaults.DefaultDuration,
    presets = get(PresetSecondsKey)?.let { it.toIntArray().map { it.seconds } }
        ?: Defaults.DefaultPresets,
    extendDuration = get(ExtendDurationSecondsKey)?.seconds ?: Defaults.DefaultExtendDuration,
    showNotification = get(ShowNotificationKey) ?: Defaults.DefaultShowNotification,
)
