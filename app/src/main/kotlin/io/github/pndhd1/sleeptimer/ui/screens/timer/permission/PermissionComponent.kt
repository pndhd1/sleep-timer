package io.github.pndhd1.sleeptimer.ui.screens.timer.permission

import android.content.Intent

interface PermissionComponent {

    val permissionType: PermissionType

    fun getActivationIntent(): Intent?

    fun onPermissionResult()
}

enum class PermissionType {
    DeviceAdmin,
    ExactAlarm,
}
