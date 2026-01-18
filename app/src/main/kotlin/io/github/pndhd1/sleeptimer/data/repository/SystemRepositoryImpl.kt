package io.github.pndhd1.sleeptimer.data.repository

import android.Manifest
import android.app.AlarmManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.data.receiver.DeviceAdminReceiverImpl
import io.github.pndhd1.sleeptimer.domain.repository.SystemRepository
import kotlinx.coroutines.flow.*

private val NotificationPermissionRequestedKey =
    booleanPreferencesKey("notification_permission_requested")

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SystemRepositoryImpl(
    private val context: Context,
    private val preferences: DataStore<Preferences>,
) : SystemRepository {

    private val devicePolicyManager: DevicePolicyManager? = context.getSystemService()
    private val alarmManager: AlarmManager? = context.getSystemService()
    private val audioManager: AudioManager? = context.getSystemService()

    private val componentName = DeviceAdminReceiverImpl.getComponentName(context)

    private val _isAdminActive = MutableStateFlow(devicePolicyManager.isAdminActive)
    override val isAdminActive: StateFlow<Boolean> = _isAdminActive.asStateFlow()

    private val _canScheduleExactAlarms = MutableStateFlow(checkCanScheduleExactAlarms())
    override val canScheduleExactAlarms: StateFlow<Boolean> = _canScheduleExactAlarms.asStateFlow()

    private val _canSendNotifications = MutableStateFlow(checkNotificationsPermission())
    override val canSendNotifications: StateFlow<Boolean> = _canSendNotifications.asStateFlow()

    override val wasNotificationPermissionRequested: Flow<Boolean> =
        preferences.data.map { it[NotificationPermissionRequestedKey] ?: false }

    override fun lockScreen() {
        if (devicePolicyManager.isAdminActive) devicePolicyManager?.lockNow()
    }

    override fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(homeIntent)
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

    override fun refreshAdminState() {
        _isAdminActive.value = devicePolicyManager.isAdminActive
    }

    override fun refreshAlarmPermissionState() {
        _canScheduleExactAlarms.value = checkCanScheduleExactAlarms()
    }

    override fun refreshNotificationPermissionState() {
        _canSendNotifications.value = checkNotificationsPermission()
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

    private val DevicePolicyManager?.isAdminActive: Boolean
        get() = this?.isAdminActive(componentName) == true
}


