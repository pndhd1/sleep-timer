package io.github.pndhd1.sleeptimer.data.repository

import android.app.AlarmManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.data.receiver.DeviceAdminReceiverImpl
import io.github.pndhd1.sleeptimer.domain.repository.DeviceAdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DeviceAdminRepositoryImpl(
    private val context: Context,
) : DeviceAdminRepository {

    private val devicePolicyManager: DevicePolicyManager? = context.getSystemService()
    private val alarmManager: AlarmManager? = context.getSystemService()

    private val componentName = DeviceAdminReceiverImpl.getComponentName(context)

    private val _isAdminActive = MutableStateFlow(devicePolicyManager.isAdminActive)
    override val isAdminActive: StateFlow<Boolean> = _isAdminActive.asStateFlow()

    private val _canScheduleExactAlarms = MutableStateFlow(checkCanScheduleExactAlarms())
    override val canScheduleExactAlarms: StateFlow<Boolean> = _canScheduleExactAlarms.asStateFlow()

    override fun getAdminActivationIntent() =
        Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            val explanation = context.getString(R.string.permission_device_admin_system_explanation)
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, explanation)
        }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun getAlarmPermissionIntent() =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)

    override fun lockScreen() {
        if (devicePolicyManager.isAdminActive) devicePolicyManager?.lockNow()
    }

    override fun refreshAdminState() {
        _isAdminActive.value = devicePolicyManager.isAdminActive
    }

    override fun refreshAlarmPermissionState() {
        _canScheduleExactAlarms.value = checkCanScheduleExactAlarms()
    }

    private fun checkCanScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager?.canScheduleExactAlarms() ?: false
    }

    private val DevicePolicyManager?.isAdminActive: Boolean
        get() = this?.isAdminActive(componentName) == true
}


