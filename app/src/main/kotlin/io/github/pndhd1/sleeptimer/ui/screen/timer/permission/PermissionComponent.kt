package io.github.pndhd1.sleeptimer.ui.screen.timer.permission

import android.content.Intent
import kotlinx.serialization.Serializable

interface PermissionComponent {

    val permissionType: PermissionType

    fun getActivationIntent(): Intent?

    fun getRuntimePermission(): String?

    fun onPermissionResult()
}

@Serializable
enum class PermissionType {
    DeviceAdmin,
    ExactAlarm,
    Notification,
}
