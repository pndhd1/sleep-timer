package io.github.pndhd1.sleeptimer.ui.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.domain.model.ActiveTimerData
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import io.github.pndhd1.sleeptimer.domain.repository.SettingsRepository
import io.github.pndhd1.sleeptimer.requireAppGraph
import io.github.pndhd1.sleeptimer.ui.MainActivity
import io.github.pndhd1.sleeptimer.utils.Defaults.DefaultExtendDuration
import io.github.pndhd1.sleeptimer.utils.Formatter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private inline val NotificationUpdateInterval get() = 0.5.seconds

class TimerNotificationService : LifecycleService() {

    @Inject
    private lateinit var activeTimerRepository: ActiveTimerRepository

    @Inject
    private lateinit var settingsRepository: SettingsRepository

    private val notificationManager: NotificationManager? by lazy(::getSystemService)
    private var notificationUpdateJob: Job? = null
    private var cachedExtendDuration = DefaultExtendDuration

    override fun onCreate() {
        super.onCreate()
        requireAppGraph().inject(this)
        createNotificationChannel()

        settingsRepository.timerSettings
            .onEach {
                if (cachedExtendDuration != it.extendDuration) {
                    cachedExtendDuration = it.extendDuration
                    updateNotificationIfRunning()
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_STOP -> stopTimer()
            ACTION_EXTEND -> extendTimer()
        }
        return START_STICKY
    }

    private fun startTimer() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            // Initial notification with no data; we need to foreground service ASAP
            createNotification(null),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
            } else {
                0
            }
        )
        launchNotificationUpdateJob()
    }

    private fun stopTimer() {
        lifecycleScope.launch {
            activeTimerRepository.clearTimer()
            stopSelf()
        }
    }

    private fun extendTimer() {
        lifecycleScope.launch {
            activeTimerRepository.extendTimer(cachedExtendDuration)
        }
    }

    private fun launchNotificationUpdateJob() {
        val prevJob = notificationUpdateJob
        notificationUpdateJob = lifecycleScope.launch {
            prevJob?.cancelAndJoin()
            while (isActive) {
                val timerData = activeTimerRepository.activeTimer.first() ?: run {
                    stopSelf()
                    return@launch
                }

                if (timerData.remaining.isNegative()) {
                    stopSelf()
                    return@launch
                }

                updateNotification(timerData)
                delay(NotificationUpdateInterval)
            }
        }
    }

    private fun updateNotification(data: ActiveTimerData?) {
        notificationManager?.notify(NOTIFICATION_ID, createNotification(data))
    }

    private suspend fun updateNotificationIfRunning() {
        if (notificationUpdateJob?.isActive != true) return
        val timerData = activeTimerRepository.activeTimer.first() ?: return
        updateNotification(timerData)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun createNotification(timerData: ActiveTimerData?): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            REQUEST_CODE_CONTENT,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            REQUEST_CODE_STOP,
            Intent(this, TimerNotificationService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val extendIntent = PendingIntent.getService(
            this,
            REQUEST_CODE_EXTEND,
            Intent(this, TimerNotificationService::class.java).apply { action = ACTION_EXTEND },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (remainingText, progress) = if (timerData != null) {
            val remaining = timerData.remaining
            val totalSeconds = timerData.totalDuration.inWholeSeconds.toInt()
            val remainingSeconds = remaining.inWholeSeconds.toInt().coerceAtLeast(0)
            val progressPercent = if (totalSeconds > 0) {
                ((totalSeconds - remainingSeconds) * 100 / totalSeconds)
            } else 0

            val text = if (remaining.isNegative()) {
                ""
            } else {
                Formatter.formatTime(Locale.getDefault(), remaining)
            }
            text to progressPercent
        } else {
            "" to 0
        }

        val extendMinutes = cachedExtendDuration.inWholeMinutes.toInt()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(remainingText)
            .setSmallIcon(R.drawable.ic_timer)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(contentIntent)
            .setProgress(100, progress, false)
            .addAction(
                0,
                getString(R.string.notification_action_stop),
                stopIntent
            )
            .addAction(
                0,
                getString(R.string.notification_action_extend, extendMinutes),
                extendIntent
            )
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "timer_channel"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_START = "io.github.pndhd1.sleeptimer.ACTION_START_TIMER_SERVICE"
        private const val ACTION_STOP = "io.github.pndhd1.sleeptimer.ACTION_STOP_TIMER_SERVICE"
        private const val ACTION_EXTEND = "io.github.pndhd1.sleeptimer.ACTION_EXTEND_TIMER"
        private const val REQUEST_CODE_CONTENT = 99
        private const val REQUEST_CODE_STOP = 100
        private const val REQUEST_CODE_EXTEND = 101

        fun start(context: Context) {
            val intent = Intent(context, TimerNotificationService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerNotificationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}

private inline val ActiveTimerData.remaining
    get() = targetTime - Clock.System.now()
