package io.github.pndhd1.sleeptimer.ui.screen.timer.permission

import android.content.Intent
import com.arkivanov.decompose.ComponentContext
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pndhd1.sleeptimer.domain.repository.SystemRepository
import io.github.pndhd1.sleeptimer.utils.componentScope
import kotlinx.coroutines.launch

@AssistedInject
class DefaultPermissionComponent(
    @Assisted componentContext: ComponentContext,
    @Assisted override val permissionType: PermissionType,
    private val systemRepository: SystemRepository,
) : PermissionComponent, ComponentContext by componentContext {

    @AssistedFactory
    fun interface Factory {
        fun create(
            componentContext: ComponentContext,
            permissionType: PermissionType,
        ): DefaultPermissionComponent
    }

    private val scope = componentScope()

    override fun getActivationIntent(): Intent? = when (permissionType) {
        PermissionType.DeviceAdmin -> systemRepository.getAdminActivationIntent()
        PermissionType.ExactAlarm -> systemRepository.getAlarmPermissionIntent()
        PermissionType.Notification -> null
    }

    override fun getRuntimePermission(): String? = when (permissionType) {
        PermissionType.Notification -> systemRepository.getNotificationPermission()
        else -> null
    }

    override fun onPermissionResult() {
        when (permissionType) {
            PermissionType.DeviceAdmin -> systemRepository.refreshAdminState()
            PermissionType.ExactAlarm -> systemRepository.refreshAlarmPermissionState()
            PermissionType.Notification -> {
                systemRepository.refreshNotificationPermissionState()
                scope.launch { systemRepository.markNotificationPermissionRequested() }
            }
        }
    }
}
