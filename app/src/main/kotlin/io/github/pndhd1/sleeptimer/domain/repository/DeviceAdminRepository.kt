package io.github.pndhd1.sleeptimer.domain.repository

import android.content.Intent
import kotlinx.coroutines.flow.StateFlow

interface DeviceAdminRepository {
    val isAdminActive: StateFlow<Boolean>
    val canScheduleExactAlarms: StateFlow<Boolean>

    fun getAdminActivationIntent(): Intent
    fun getAlarmPermissionIntent(): Intent?

    fun lockScreen()
    fun refreshAdminState()
    fun refreshAlarmPermissionState()
}
