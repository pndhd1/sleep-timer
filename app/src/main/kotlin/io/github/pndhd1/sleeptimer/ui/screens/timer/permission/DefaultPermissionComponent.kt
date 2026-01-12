package io.github.pndhd1.sleeptimer.ui.screens.timer.permission

import android.content.Intent
import com.arkivanov.decompose.ComponentContext
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pndhd1.sleeptimer.domain.repository.DeviceAdminRepository

@AssistedInject
class DefaultPermissionComponent(
    @Assisted componentContext: ComponentContext,
    @Assisted override val permissionType: PermissionType,
    private val deviceAdminRepository: DeviceAdminRepository,
) : PermissionComponent, ComponentContext by componentContext {

    @AssistedFactory
    fun interface Factory {
        fun create(
            componentContext: ComponentContext,
            permissionType: PermissionType,
        ): DefaultPermissionComponent
    }

    override fun getActivationIntent(): Intent? = when (permissionType) {
        PermissionType.DeviceAdmin -> deviceAdminRepository.getAdminActivationIntent()
        PermissionType.ExactAlarm -> deviceAdminRepository.getAlarmPermissionIntent()
    }

    override fun onPermissionResult() {
        when (permissionType) {
            PermissionType.DeviceAdmin -> deviceAdminRepository.refreshAdminState()
            PermissionType.ExactAlarm -> deviceAdminRepository.refreshAlarmPermissionState()
        }
    }
}
