package io.github.pndhd1.sleeptimer.data.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class TimerService : Service() {

    @Inject
    lateinit var activeTimerRepository: ActiveTimerRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var updateJob: Job? = null
    private val notificationManager: NotificationManager? by lazy(::getSystemService)

    private val extendDuration by lazy {
        settingsRepository.timerSettings.map { it.extendDuration }
            .stateIn(serviceScope, SharingStarted.Eagerly, DefaultExtendDuration)
    }

    override fun onCreate() {
        super.onCreate()
        requireAppGraph().inject(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_STOP -> stopTimer()
            ACTION_EXTEND -> extendTimer()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun startTimer() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            createNotification(null),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
            } else {
                0
            }
        )
        startUpdatingNotification()
    }

    private fun stopTimer() {
        serviceScope.launch {
            activeTimerRepository.clearTimer()
            stopSelf()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun extendTimer() {
        serviceScope.launch {
            val settings = settingsRepository.timerSettings.first()
            activeTimerRepository.extendTimer(settings.extendDuration)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun startUpdatingNotification() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (true) {
                val timerData = activeTimerRepository.activeTimer.first()
                if (timerData == null) {
                    stopSelf()
                    return@launch
                }

                val now = Clock.System.now()
                val remaining = timerData.targetTime - now
                if (remaining.isNegative()) {
                    stopSelf()
                    return@launch
                }

                notificationManager?.notify(
                    NOTIFICATION_ID,
                    createNotification(timerData)
                )
                delay(0.5.seconds)
            }
        }
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
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            REQUEST_CODE_STOP,
            Intent(this, TimerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val extendIntent = PendingIntent.getService(
            this,
            REQUEST_CODE_EXTEND,
            Intent(this, TimerService::class.java).apply { action = ACTION_EXTEND },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (remainingText, progress) = if (timerData != null) {
            val remaining = timerData.targetTime - Clock.System.now()
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

        val extendMinutes = extendDuration.value.inWholeMinutes.toInt()

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
        private const val REQUEST_CODE_STOP = 100
        private const val REQUEST_CODE_EXTEND = 101

        fun start(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
