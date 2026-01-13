package io.github.pndhd1.sleeptimer.data.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.domain.notification.NotificationChannelManager
import io.github.pndhd1.sleeptimer.requireAppGraph
import io.github.pndhd1.sleeptimer.ui.MainActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class AudioFadeService : LifecycleService() {

    @Inject
    private lateinit var notificationChannelManager: NotificationChannelManager

    private val audioManager: AudioManager? by lazy(::getSystemService)
    private var fadeJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        requireAppGraph().inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_START_FADE) {
            val fadeDurationSeconds = intent.getLongExtra(EXTRA_FADE_DURATION_SECONDS, 3L)
            startForegroundAndFade(fadeDurationSeconds)
        }
        return START_NOT_STICKY
    }

    private fun startForegroundAndFade(durationSeconds: Long) {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            createNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
        )

        startFadeJob(durationSeconds.seconds)
    }

    private fun startFadeJob(totalDuration: kotlin.time.Duration) {
        fadeJob?.cancel()

        val am = audioManager ?: run {
            stopSelf()
            return
        }

        val originalVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)

        if (originalVolume <= 1) {
            stopSelf()
            return
        }

        fadeJob = lifecycleScope.launch {
            val stepCount = (totalDuration.inWholeMilliseconds / FADE_STEP_INTERVAL_MS).toInt()
                .coerceAtLeast(1)
            val volumeDecrement = (originalVolume - 1).toFloat() / stepCount
            var currentVolume = originalVolume.toFloat()

            repeat(stepCount) {
                if (!isActive) return@launch

                delay(FADE_STEP_INTERVAL_MS.milliseconds)
                currentVolume = (currentVolume - volumeDecrement).coerceAtLeast(1f)

                am.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    currentVolume.toInt(),
                    0
                )
            }

            am.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0)
            stopSelf()
        }
    }

    override fun onDestroy() {
        fadeJob?.cancel()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, notificationChannelManager.channelId)
            .setContentTitle(getString(R.string.fade_notification_title))
            .setContentText(getString(R.string.fade_notification_text))
            .setSmallIcon(R.drawable.ic_volume_down)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(contentIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 2
        private const val ACTION_START_FADE = "io.github.pndhd1.sleeptimer.ACTION_START_FADE"
        private const val EXTRA_FADE_DURATION_SECONDS = "fade_duration_seconds"
        private const val FADE_STEP_INTERVAL_MS = 100L

        fun start(context: Context, fadeDurationSeconds: Long) {
            val intent = Intent(context, AudioFadeService::class.java).apply {
                action = ACTION_START_FADE
                putExtra(EXTRA_FADE_DURATION_SECONDS, fadeDurationSeconds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AudioFadeService::class.java)
            context.stopService(intent)
        }
    }
}
