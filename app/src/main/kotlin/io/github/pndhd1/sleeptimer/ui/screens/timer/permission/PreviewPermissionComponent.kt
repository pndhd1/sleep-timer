package io.github.pndhd1.sleeptimer.ui.screens.timer.permission

import android.content.Intent

class PreviewPermissionComponent : PermissionComponent {
    override fun getActivationIntent(explanation: String): Intent = Intent()
    override fun onPermissionResult() {}
}
