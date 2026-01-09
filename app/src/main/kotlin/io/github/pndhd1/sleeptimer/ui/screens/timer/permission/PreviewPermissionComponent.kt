package io.github.pndhd1.sleeptimer.ui.screens.timer.permission

import android.content.Intent

class PreviewPermissionComponent(
    override val permissionType: PermissionType = PermissionType.DeviceAdmin,
) : PermissionComponent {
    override fun getActivationIntent(): Intent = Intent()
    override fun onPermissionResult() {}
}
