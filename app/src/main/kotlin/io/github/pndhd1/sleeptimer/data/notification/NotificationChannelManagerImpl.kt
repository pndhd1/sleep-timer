package io.github.pndhd1.sleeptimer.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.domain.notification.NotificationChannelManager

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class NotificationChannelManagerImpl(
    private val context: Context,
) : NotificationChannelManager {

    private val notificationManager = context.getSystemService<NotificationManager>()

    override val progressChannelId: String = TIMER_CHANNEL_ID
    override val actionsChannelId: String = ACTIONS_CHANNEL_ID

    override fun createChannels() {
        notificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timerChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                setShowBadge(false)
            }

            val actionsChannel = NotificationChannel(
                ACTIONS_CHANNEL_ID,
                context.getString(R.string.high_priority_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.high_priority_channel_description)
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }

            notificationManager.createNotificationChannels(
                listOf(timerChannel, actionsChannel)
            )
        }
    }

    override fun isActionsChannelEnabled(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true
        notificationManager ?: return false
        val channel = notificationManager.getNotificationChannel(ACTIONS_CHANNEL_ID) ?: return false
        return channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    override fun getActionsChannelSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, ACTIONS_CHANNEL_ID)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:${context.packageName}".toUri()
            }
        }
    }

    companion object {
        private const val TIMER_CHANNEL_ID = "timer_channel"
        private const val ACTIONS_CHANNEL_ID = "timer_actions_channel"
    }
}
