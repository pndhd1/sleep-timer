package io.github.pndhd1.sleeptimer.ui.screens.timer.permission

import com.arkivanov.decompose.ComponentContext
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.pndhd1.sleeptimer.domain.repository.DeviceAdminRepository

@AssistedInject
class DefaultPermissionComponent(
    @Assisted componentContext: ComponentContext,
    private val deviceAdminRepository: DeviceAdminRepository,
) : PermissionComponent, ComponentContext by componentContext {

    @AssistedFactory
    fun interface Factory {
        fun create(componentContext: ComponentContext): DefaultPermissionComponent
    }

    override fun getActivationIntent(explanation: String) =
        deviceAdminRepository.getActivationIntent(explanation)

    override fun onPermissionResult() {
        deviceAdminRepository.refreshAdminState()
    }
}
