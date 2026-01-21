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

    override val progressChannelId: String = TimerChannelId
    override val actionsChannelId: String = ActionsChannelId

    override fun createChannels() {
        notificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timerChannel = NotificationChannel(
                TimerChannelId,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                setShowBadge(false)
            }

            val actionsChannel = NotificationChannel(
                ActionsChannelId,
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
        val channel = notificationManager.getNotificationChannel(ActionsChannelId) ?: return false
        return channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    override fun getActionsChannelSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, ActionsChannelId)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:${context.packageName}".toUri()
            }
        }
    }

    companion object {
        private const val TimerChannelId = "timer_channel"
        private const val ActionsChannelId = "timer_actions_channel"
    }
}
