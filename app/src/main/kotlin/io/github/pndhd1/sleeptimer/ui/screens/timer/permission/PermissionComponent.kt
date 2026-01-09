package io.github.pndhd1.sleeptimer.ui.screens.timer.permission

import android.content.Intent

interface PermissionComponent {

    fun getActivationIntent(explanation: String): Intent

    fun onPermissionResult()
}
