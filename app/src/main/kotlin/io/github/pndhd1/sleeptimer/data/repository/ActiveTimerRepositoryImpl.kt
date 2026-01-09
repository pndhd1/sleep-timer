package io.github.pndhd1.sleeptimer.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.pndhd1.sleeptimer.data.receiver.TimerAlarmReceiver
import io.github.pndhd1.sleeptimer.data.service.TimerService
import io.github.pndhd1.sleeptimer.domain.model.ActiveTimerData
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val TargetTimeKey = longPreferencesKey("target_time_epoch_seconds")
private val TotalDurationKey = longPreferencesKey("total_duration_seconds")
private const val ALARM_REQUEST_CODE = 1001

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

    override suspend fun startTimer(targetTime: Instant, totalDuration: Duration) {
        preferences.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                this[TargetTimeKey] = targetTime.epochSeconds
                this[TotalDurationKey] = totalDuration.inWholeSeconds
            }
        }
        scheduleAlarm(targetTime)
        TimerService.start(context)
    }

    override suspend fun extendTimer(additionalDuration: Duration) {
        val current = activeTimer.first() ?: return
        val newTargetTime = current.targetTime + additionalDuration
        val newTotalDuration = current.totalDuration + additionalDuration
        cancelAlarm()
        preferences.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                this[TargetTimeKey] = newTargetTime.epochSeconds
                this[TotalDurationKey] = newTotalDuration.inWholeSeconds
            }
        }
        scheduleAlarm(newTargetTime)
    }

    override suspend fun clearTimer() {
        cancelAlarm()
        preferences.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                this.remove(TargetTimeKey)
                this.remove(TotalDurationKey)
            }
        }
        TimerService.stop(context)
    }

    private fun scheduleAlarm(targetTime: Instant) {
        val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
            action = TimerAlarmReceiver.ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
        val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
            action = TimerAlarmReceiver.ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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