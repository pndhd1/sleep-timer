package io.github.pndhd1.sleeptimer.domain.notification

import android.content.Intent

interface NotificationChannelManager {

    val progressChannelId: String
    val actionsChannelId: String

    fun createChannels()
    fun isActionsChannelEnabled(): Boolean
    fun getActionsChannelSettingsIntent(): Intent
}
