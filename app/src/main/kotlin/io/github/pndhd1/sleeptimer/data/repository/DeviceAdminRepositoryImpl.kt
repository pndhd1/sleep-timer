package io.github.pndhd1.sleeptimer.data.repository

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.pndhd1.sleeptimer.data.receiver.DeviceAdminReceiverImpl
import io.github.pndhd1.sleeptimer.domain.repository.DeviceAdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DeviceAdminRepositoryImpl(
    context: Context,
) : DeviceAdminRepository {

    private val devicePolicyManager: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val componentName = DeviceAdminReceiverImpl.getComponentName(context)

    private val _isAdminActive = MutableStateFlow(devicePolicyManager.isAdminActive(componentName))
    override val isAdminActive: StateFlow<Boolean> = _isAdminActive.asStateFlow()

    override fun getActivationIntent(explanation: String): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, explanation)
        }
    }

    override fun lockScreen() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()
        }
    }

    override fun refreshAdminState() {
        _isAdminActive.value = devicePolicyManager.isAdminActive(componentName)
    }
}
