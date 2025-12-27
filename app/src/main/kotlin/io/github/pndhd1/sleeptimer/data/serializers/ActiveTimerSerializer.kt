package io.github.pndhd1.sleeptimer.data.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import io.github.pndhd1.sleeptimer.data.proto.ProtoActiveTimerState
import io.github.pndhd1.sleeptimer.data.proto.protoActiveTimerState
import java.io.InputStream
import java.io.OutputStream

object ActiveTimerSerializer : Serializer<ProtoActiveTimerState> {

    override val defaultValue: ProtoActiveTimerState = protoActiveTimerState {}

    override suspend fun readFrom(input: InputStream): ProtoActiveTimerState {
        try {
            return ProtoActiveTimerState.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: ProtoActiveTimerState, output: OutputStream) {
        t.writeTo(output)
    }
}