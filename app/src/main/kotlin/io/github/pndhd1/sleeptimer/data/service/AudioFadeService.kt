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
            val targetVolumePercent = intent.getIntExtra(EXTRA_TARGET_VOLUME_PERCENT, 0)
            startForegroundAndFade(fadeDurationSeconds, targetVolumePercent)
        }
        return START_NOT_STICKY
    }

    private fun startForegroundAndFade(durationSeconds: Long, targetVolumePercent: Int) {
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

        startFadeJob(durationSeconds.seconds, targetVolumePercent)
    }

    private fun startFadeJob(totalDuration: kotlin.time.Duration, targetVolumePercent: Int) {
        fadeJob?.cancel()

        val am = audioManager ?: run {
            stopSelf()
            return
        }

        val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val originalVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = (maxVolume * targetVolumePercent / 100).coerceAtLeast(0)

        // If current volume is already at or below target, nothing to do
        if (originalVolume <= targetVolume) {
            stopSelf()
            return
        }

        fadeJob = lifecycleScope.launch {
            val stepCount = (totalDuration.inWholeMilliseconds / FADE_STEP_INTERVAL_MS).toInt()
                .coerceAtLeast(1)
            val volumeDecrement = (originalVolume - targetVolume).toFloat() / stepCount
            var currentVolume = originalVolume.toFloat()

            repeat(stepCount) {
                if (!isActive) return@launch

                delay(FADE_STEP_INTERVAL_MS.milliseconds)
                currentVolume = (currentVolume - volumeDecrement).coerceAtLeast(targetVolume.toFloat())

                am.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    currentVolume.toInt(),
                    0
                )
            }

            am.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
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
        private const val EXTRA_TARGET_VOLUME_PERCENT = "target_volume_percent"
        private const val FADE_STEP_INTERVAL_MS = 100L

        fun start(context: Context, fadeDurationSeconds: Long, targetVolumePercent: Int = 0) {
            val intent = Intent(context, AudioFadeService::class.java).apply {
                action = ACTION_START_FADE
                putExtra(EXTRA_FADE_DURATION_SECONDS, fadeDurationSeconds)
                putExtra(EXTRA_TARGET_VOLUME_PERCENT, targetVolumePercent)
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
