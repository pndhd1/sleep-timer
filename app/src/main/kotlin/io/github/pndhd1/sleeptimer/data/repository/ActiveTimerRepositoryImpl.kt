package io.github.pndhd1.sleeptimer.data.repository

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.pndhd1.sleeptimer.data.receiver.TimerAlarmReceiver
import io.github.pndhd1.sleeptimer.data.service.AudioFadeService
import io.github.pndhd1.sleeptimer.domain.model.ActiveTimerData
import io.github.pndhd1.sleeptimer.domain.model.FadeOutSettings
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val TargetTimeKey = longPreferencesKey("target_time_epoch_seconds")
private val TotalDurationKey = longPreferencesKey("total_duration_seconds")
private const val AlarmRequestCode = 1001
private const val FadeAlarmRequestCode = 1002

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ActiveTimerRepositoryImpl(
    private val context: Context,
    private val preferences: DataStore<Preferences>,
) : ActiveTimerRepository {

    private val alarmManager: AlarmManager? = context.getSystemService()

    override val activeTimer: Flow<ActiveTimerData?> =
        preferences.data.map(Preferences::toDomain)

    override suspend fun startTimer(
        targetTime: Instant,
        totalDuration: Duration,
        fadeOutSettings: FadeOutSettings,
    ) {
        preferences.edit {
            it[TargetTimeKey] = targetTime.epochSeconds
            it[TotalDurationKey] = totalDuration.inWholeSeconds
        }
        scheduleAlarm(targetTime)
        scheduleFadeAlarmIfEnabled(targetTime, fadeOutSettings)
    }

    override suspend fun extendTimer(
        additionalDuration: Duration,
        fadeOutSettings: FadeOutSettings,
    ) {
        val current = activeTimer.first() ?: return
        val newTargetTime = current.targetTime + additionalDuration
        val newTotalDuration = current.totalDuration + additionalDuration
        cancelAlarm()
        cancelFadeAlarm()
        AudioFadeService.stop(context)
        preferences.edit {
            it[TargetTimeKey] = newTargetTime.epochSeconds
            it[TotalDurationKey] = newTotalDuration.inWholeSeconds
        }
        scheduleAlarm(newTargetTime)
        scheduleFadeAlarmIfEnabled(newTargetTime, fadeOutSettings)
    }

    override suspend fun clearTimer() {
        cancelAlarm()
        cancelFadeAlarm()
        AudioFadeService.stop(context)
        preferences.edit {
            it.remove(TargetTimeKey)
            it.remove(TotalDurationKey)
        }
    }

    private fun scheduleAlarm(targetTime: Instant) {
        val pendingIntent = TimerAlarmReceiver.getPendingIntent(context, AlarmRequestCode)
        val triggerAtMillis = targetTime.toEpochMilliseconds()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager?.canScheduleExactAlarms() == true) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm() {
        val pendingIntent = TimerAlarmReceiver.getPendingIntent(context, AlarmRequestCode)
        alarmManager?.cancel(pendingIntent)
    }

    private fun scheduleFadeAlarmIfEnabled(targetTime: Instant, fadeOutSettings: FadeOutSettings) {
        if (!fadeOutSettings.enabled) return

        val now = Clock.System.now()
        val timeUntilEnd = targetTime - now
        if (timeUntilEnd.isNegative()) return

        val fadeStartTime = targetTime - fadeOutSettings.startBefore
        val startImmediately = fadeStartTime <= now

        // Fade duration should not exceed remaining time
        val timeFromFadeToEnd = if (startImmediately) timeUntilEnd else fadeOutSettings.startBefore
        val actualFadeDuration = minOf(fadeOutSettings.duration, timeFromFadeToEnd)

        if (startImmediately) {
            startFadeService(actualFadeDuration, fadeOutSettings.targetVolumePercent)
        } else {
            scheduleFadeAlarm(
                fadeStartTime,
                actualFadeDuration,
                fadeOutSettings.targetVolumePercent
            )
        }
    }

    private fun startFadeService(fadeDuration: Duration, targetVolumePercent: Int) {
        AudioFadeService.start(context, fadeDuration.inWholeSeconds, targetVolumePercent)
    }

    private fun scheduleFadeAlarm(
        fadeStartTime: Instant,
        fadeDuration: Duration,
        targetVolumePercent: Int
    ) {
        val pendingIntent = AudioFadeService.createStartPendingIntent(
            context,
            FadeAlarmRequestCode,
            fadeDuration.inWholeSeconds,
            targetVolumePercent
        )

        val triggerAtMillis = fadeStartTime.toEpochMilliseconds()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager?.canScheduleExactAlarms() == true) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun cancelFadeAlarm() {
        val pendingIntent =
            AudioFadeService.createCancelPendingIntent(context, FadeAlarmRequestCode)
        alarmManager?.cancel(pendingIntent)
    }
}

private fun Preferences.toDomain(): ActiveTimerData? {
    val targetTimeSeconds = this[TargetTimeKey] ?: return null
    val totalDurationSeconds = this[TotalDurationKey] ?: return null
    return ActiveTimerData(
        targetTime = Instant.fromEpochSeconds(targetTimeSeconds),
        totalDuration = totalDurationSeconds.seconds,
    )
}
