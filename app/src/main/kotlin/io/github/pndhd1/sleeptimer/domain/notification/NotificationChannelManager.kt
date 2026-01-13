package io.github.pndhd1.sleeptimer.domain.notification

interface NotificationChannelManager {
    val channelId: String

    fun createChannel()
}
