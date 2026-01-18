package io.github.pndhd1.sleeptimer.domain.repository

import android.content.Intent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SystemRepository {
    val isAdminActive: StateFlow<Boolean>
    val canScheduleExactAlarms: StateFlow<Boolean>
    val canSendNotifications: StateFlow<Boolean>
    val canUseFullScreenIntent: StateFlow<Boolean>
    val wasNotificationPermissionRequested: Flow<Boolean>

    fun lockScreen()
    fun goHomeAfterLock()
    fun requestAudioFocusToStopMedia()

    fun getAdminActivationIntent(): Intent
    fun getAlarmPermissionIntent(): Intent?
    fun getNotificationPermission(): String?
    fun getFullScreenIntentSettingsIntent(): Intent?

    fun refreshAdminState()
    fun refreshAlarmPermissionState()
    fun refreshNotificationPermissionState()
    fun refreshFullScreenIntentPermissionState()

    suspend fun markNotificationPermissionRequested()
}
