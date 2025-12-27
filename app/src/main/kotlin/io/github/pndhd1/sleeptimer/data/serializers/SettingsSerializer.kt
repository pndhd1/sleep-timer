package io.github.pndhd1.sleeptimer.data.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import io.github.pndhd1.sleeptimer.data.proto.ProtoSettings
import io.github.pndhd1.sleeptimer.data.proto.protoSettings
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val DefaultDuration inline get() = 15.minutes
private val DefaultsPresets: List<Duration>
    inline get() = listOf(5L, 10L, 15L, 30L, 60L).map { it.minutes }

object SettingsSerializer : Serializer<ProtoSettings> {

    override val defaultValue: ProtoSettings = protoSettings {
        defaultDurationSeconds = DefaultDuration.inWholeSeconds
        presetSeconds.addAll(DefaultsPresets.map { it.inWholeSeconds })
    }

    override suspend fun readFrom(input: InputStream): ProtoSettings {
        try {
            return ProtoSettings.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: ProtoSettings, output: OutputStream) {
        t.writeTo(output)
    }
}
