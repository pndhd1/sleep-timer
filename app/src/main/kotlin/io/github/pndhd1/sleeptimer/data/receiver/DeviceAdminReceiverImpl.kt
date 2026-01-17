package io.github.pndhd1.sleeptimer.data.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.repository.SystemRepository
import io.github.pndhd1.sleeptimer.requireAppGraph

class DeviceAdminReceiverImpl : DeviceAdminReceiver() {

    @Inject
    private lateinit var systemRepository: SystemRepository

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        context.requireAppGraph().inject(this)
        systemRepository.refreshAdminState()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        context.requireAppGraph().inject(this)
        systemRepository.refreshAdminState()
    }

    companion object {
        fun getComponentName(context: Context): ComponentName =
            ComponentName(context, DeviceAdminReceiverImpl::class.java)
    }
}
