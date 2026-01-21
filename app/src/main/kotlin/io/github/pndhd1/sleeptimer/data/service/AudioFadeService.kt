package io.github.pndhd1.sleeptimer.data.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Build
import androidx.compose.ui.util.lerp
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.domain.notification.NotificationChannelManager
import io.github.pndhd1.sleeptimer.requireAppGraph
import io.github.pndhd1.sleeptimer.ui.activity.MainActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

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
        if (intent?.action == ActionStartFade) {
            val fadeDurationSeconds = intent.getLongExtra(ExtraFadeDurationSeconds, -1)
            if (fadeDurationSeconds < 0L) {
                stopSelf()
                return START_NOT_STICKY
            }
            val targetVolumePercent = intent.getIntExtra(ExtraTargetVolumePercent, 0)
            startForegroundAndFade(fadeDurationSeconds, targetVolumePercent)
        }
        return START_NOT_STICKY
    }

    private fun startForegroundAndFade(durationSeconds: Long, targetVolumePercent: Int) {
        ServiceCompat.startForeground(
            this,
            NotificationId,
            createNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
        )

        startFadeJob(durationSeconds.seconds, targetVolumePercent)
    }

    private fun startFadeJob(totalDuration: Duration, targetVolumePercent: Int) {
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
            val startMark = TimeSource.Monotonic.markNow()

            while (isActive) {
                val elapsed = startMark.elapsedNow()
                if (elapsed >= totalDuration) break

                val progress = (elapsed / totalDuration).coerceIn(0.0, 1.0).toFloat()
                val currentVolume =
                    lerp(originalVolume.toFloat(), targetVolume.toFloat(), progress).toInt()

                am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
                delay(FadeStepIntervalMs)
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

        return NotificationCompat.Builder(this, notificationChannelManager.progressChannelId)
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
        private const val NotificationId = 2
        private const val ActionStartFade = "io.github.pndhd1.sleeptimer.ActionStartFade"
        private const val ExtraFadeDurationSeconds = "fade_duration_seconds"
        private const val ExtraTargetVolumePercent = "target_volume_percent"
        private const val FadeStepIntervalMs = 100L

        fun start(context: Context, fadeDurationSeconds: Long, targetVolumePercent: Int = 0) {
            val intent = Intent(context, AudioFadeService::class.java).apply {
                action = ActionStartFade
                putExtra(ExtraFadeDurationSeconds, fadeDurationSeconds)
                putExtra(ExtraTargetVolumePercent, targetVolumePercent)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, AudioFadeService::class.java)
            context.stopService(intent)
        }

        fun createStartPendingIntent(
            context: Context,
            requestCode: Int,
            fadeDurationSeconds: Long,
            targetVolumePercent: Int,
        ): PendingIntent {
            val intent = Intent(context, AudioFadeService::class.java).apply {
                action = ActionStartFade
                putExtra(ExtraFadeDurationSeconds, fadeDurationSeconds)
                putExtra(ExtraTargetVolumePercent, targetVolumePercent)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(context, requestCode, intent, flags)
            } else {
                PendingIntent.getService(context, requestCode, intent, flags)
            }
        }

        fun createCancelPendingIntent(context: Context, requestCode: Int): PendingIntent {
            val intent = Intent(context, AudioFadeService::class.java).apply {
                action = ActionStartFade
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(context, requestCode, intent, flags)
            } else {
                PendingIntent.getService(context, requestCode, intent, flags)
            }
        }
    }
}
