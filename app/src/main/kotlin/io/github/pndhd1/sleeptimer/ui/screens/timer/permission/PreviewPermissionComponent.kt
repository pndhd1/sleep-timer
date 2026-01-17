package io.github.pndhd1.sleeptimer.ui.screens.timer.permission

class PreviewPermissionComponent(
    override val permissionType: PermissionType = PermissionType.DeviceAdmin,
) : PermissionComponent {
    override fun getActivationIntent() = null
    override fun getRuntimePermission() = null
    override fun onPermissionResult() {}
}
