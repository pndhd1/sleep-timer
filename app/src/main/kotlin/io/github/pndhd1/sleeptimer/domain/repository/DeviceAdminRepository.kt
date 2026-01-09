package io.github.pndhd1.sleeptimer.domain.repository

import android.content.Intent
import kotlinx.coroutines.flow.StateFlow

interface DeviceAdminRepository {
    val isAdminActive: StateFlow<Boolean>
    fun getActivationIntent(explanation: String): Intent
    fun lockScreen()
    fun refreshAdminState()
}
