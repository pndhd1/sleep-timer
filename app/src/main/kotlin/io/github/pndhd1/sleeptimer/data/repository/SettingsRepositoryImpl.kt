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
import io.github.pndhd1.sleeptimer.domain.model.FadeOutSettings
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
private val FadeOutEnabledKey = booleanPreferencesKey("fade_out_enabled")
private val FadeStartBeforeSecondsKey = intPreferencesKey("fade_start_before_seconds")
private val FadeOutDurationSecondsKey = intPreferencesKey("fade_out_duration_seconds")
private val FadeTargetVolumePercentKey = intPreferencesKey("fade_target_volume_percent")
private val GoHomeOnExpireKey = booleanPreferencesKey("go_home_on_expire")
private val StopMediaOnExpireKey = booleanPreferencesKey("stop_media_on_expire")

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

    override suspend fun updateFadeOutEnabled(enabled: Boolean) {
        preferences.updateData { current ->
            current.toMutablePreferences().apply {
                this[FadeOutEnabledKey] = enabled
            }
        }
    }

    override suspend fun updateFadeStartBefore(duration: Duration) {
        preferences.updateData { current ->
            current.toMutablePreferences().apply {
                this[FadeStartBeforeSecondsKey] = duration.inWholeSeconds.toInt()
            }
        }
    }

    override suspend fun updateFadeOutDuration(duration: Duration) {
        preferences.updateData { current ->
            current.toMutablePreferences().apply {
                this[FadeOutDurationSecondsKey] = duration.inWholeSeconds.toInt()
            }
        }
    }

    override suspend fun updateFadeTargetVolumePercent(percent: Int) {
        preferences.updateData { current ->
            current.toMutablePreferences().apply {
                this[FadeTargetVolumePercentKey] = percent.coerceIn(0, 100)
            }
        }
    }

    override suspend fun updateGoHomeOnExpire(enabled: Boolean) {
        preferences.updateData { current ->
            current.toMutablePreferences().apply {
                this[GoHomeOnExpireKey] = enabled
            }
        }
    }

    override suspend fun updateStopMediaOnExpire(enabled: Boolean) {
        preferences.updateData { current ->
            current.toMutablePreferences().apply {
                this[StopMediaOnExpireKey] = enabled
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
    fadeOut = FadeOutSettings(
        enabled = get(FadeOutEnabledKey) ?: Defaults.DefaultFadeOutEnabled,
        startBefore = get(FadeStartBeforeSecondsKey)?.seconds ?: Defaults.DefaultFadeStartBefore,
        duration = get(FadeOutDurationSecondsKey)?.seconds ?: Defaults.DefaultFadeOutDuration,
        targetVolumePercent = get(FadeTargetVolumePercentKey)
            ?: Defaults.DefaultFadeTargetVolumePercent,
    ),
    goHomeOnExpire = get(GoHomeOnExpireKey) ?: Defaults.DefaultGoHomeOnExpire,
    stopMediaOnExpire = get(StopMediaOnExpireKey) ?: Defaults.DefaultStopMediaOnExpire,
)
