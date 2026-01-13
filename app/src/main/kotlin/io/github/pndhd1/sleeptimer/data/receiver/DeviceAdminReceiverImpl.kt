package io.github.pndhd1.sleeptimer.data.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.repository.DeviceAdminRepository
import io.github.pndhd1.sleeptimer.requireAppGraph

class DeviceAdminReceiverImpl : DeviceAdminReceiver() {

    @Inject
    private lateinit var deviceAdminRepository: DeviceAdminRepository

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        context.requireAppGraph().inject(this)
        deviceAdminRepository.refreshAdminState()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        context.requireAppGraph().inject(this)
        deviceAdminRepository.refreshAdminState()
    }

    companion object {
        fun getComponentName(context: Context): ComponentName =
            ComponentName(context, DeviceAdminReceiverImpl::class.java)
    }
}
