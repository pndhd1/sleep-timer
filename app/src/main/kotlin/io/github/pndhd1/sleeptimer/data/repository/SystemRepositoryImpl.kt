package io.github.pndhd1.sleeptimer.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.data.activity.GoHomeActivity
import io.github.pndhd1.sleeptimer.data.receiver.DeviceAdminReceiverImpl
import io.github.pndhd1.sleeptimer.domain.notification.NotificationChannelManager
import io.github.pndhd1.sleeptimer.domain.repository.SystemRepository
import kotlinx.coroutines.flow.*

private val NotificationPermissionRequestedKey =
    booleanPreferencesKey("notification_permission_requested")

private const val REQUEST_CODE_GO_HOME_NOTIFICATION = 2001

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SystemRepositoryImpl(
    private val context: Context,
    private val preferences: DataStore<Preferences>,
    private val notificationChannelManager: NotificationChannelManager,
) : SystemRepository {

    private val devicePolicyManager: DevicePolicyManager? = context.getSystemService()
    private val alarmManager: AlarmManager? = context.getSystemService()
    private val audioManager: AudioManager? = context.getSystemService()
    private val notificationManager: NotificationManager? = context.getSystemService()

    private val componentName = DeviceAdminReceiverImpl.getComponentName(context)

    private val _isAdminActive = MutableStateFlow(devicePolicyManager.isAdminActive)
    override val isAdminActive: StateFlow<Boolean> = _isAdminActive.asStateFlow()

    private val _canScheduleExactAlarms = MutableStateFlow(checkCanScheduleExactAlarms())
    override val canScheduleExactAlarms: StateFlow<Boolean> = _canScheduleExactAlarms.asStateFlow()

    private val _canSendNotifications = MutableStateFlow(checkNotificationsPermission())
    override val canSendNotifications: StateFlow<Boolean> = _canSendNotifications.asStateFlow()

    private val _canUseFullScreenIntent = MutableStateFlow(checkFullScreenIntentPermission())
    override val canUseFullScreenIntent: StateFlow<Boolean> = _canUseFullScreenIntent.asStateFlow()

    override val wasNotificationPermissionRequested: Flow<Boolean> =
        preferences.data.map { it[NotificationPermissionRequestedKey] ?: false }

    override fun lockScreen() {
        if (devicePolicyManager.isAdminActive) devicePolicyManager?.lockNow()
    }

    override fun goHomeAfterLock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            goHomeViaFullScreenIntent()
        } else {
            goHomeViaStartActivity()
        }
    }

    private fun goHomeViaStartActivity() {
        context.startActivity(GoHomeActivity.getIntent(context))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun goHomeViaFullScreenIntent() {
        if (notificationManager == null || !canUseFullScreenIntent.value) return

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_GO_HOME_NOTIFICATION,
            GoHomeActivity.getIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        @SuppressLint("FullScreenIntentPolicy") // false-positive, checked above
        val notification =
            NotificationCompat.Builder(context, notificationChannelManager.actionsChannelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.go_home_notification_text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .build()

        notificationManager.notify(GoHomeActivity.NOTIFICATION_ID, notification)
    }

    @Suppress("DEPRECATION")
    override fun requestAudioFocusToStopMedia() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .build()
            am.requestAudioFocus(focusRequest)
        } else {
            am.requestAudioFocus(
                { /* no-op listener */ },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN,
            )
        }
    }

    override fun getAdminActivationIntent() =
        Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            val explanation = context.getString(R.string.permission_device_admin_system_explanation)
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, explanation)
        }

    override fun getAlarmPermissionIntent() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    } else {
        null
    }

    override fun getNotificationPermission() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }

    override fun getFullScreenIntentSettingsIntent(): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return null
        return Intent(
            Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
            "package:${context.packageName}".toUri()
        )
    }

    override fun refreshAdminState() {
        _isAdminActive.value = devicePolicyManager.isAdminActive
    }

    override fun refreshAlarmPermissionState() {
        _canScheduleExactAlarms.value = checkCanScheduleExactAlarms()
    }

    override fun refreshNotificationPermissionState() {
        _canSendNotifications.value = checkNotificationsPermission()
    }

    override fun refreshFullScreenIntentPermissionState() {
        _canUseFullScreenIntent.value = checkFullScreenIntentPermission()
    }

    override suspend fun markNotificationPermissionRequested() {
        preferences.edit { it[NotificationPermissionRequestedKey] = true }
    }

    private fun checkCanScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager?.canScheduleExactAlarms() == true
    }

    private fun checkNotificationsPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkFullScreenIntentPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        return notificationManager?.canUseFullScreenIntent() == true
    }

    private val DevicePolicyManager?.isAdminActive: Boolean
        get() = this?.isAdminActive(componentName) == true
}


