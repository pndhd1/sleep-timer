package io.github.pndhd1.sleeptimer.data.repository

import androidx.datastore.core.DataStore
import io.github.pndhd1.sleeptimer.data.proto.ProtoActiveTimerState
import io.github.pndhd1.sleeptimer.data.proto.protoActiveTimer
import io.github.pndhd1.sleeptimer.data.proto.protoActiveTimerState
import io.github.pndhd1.sleeptimer.domain.model.ActiveTimerData
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class ActiveTimerRepositoryImpl(
    private val activeTimerStore: DataStore<ProtoActiveTimerState>,
) : ActiveTimerRepository {

    override val activeTimer: Flow<ActiveTimerData?> = activeTimerStore.data.map { it.toDomain() }

    override suspend fun startTimer(duration: Duration) {
        val targetTime = Clock.System.now() + duration
        activeTimerStore.updateData {
            protoActiveTimerState {
                activeTimer = protoActiveTimer {
                    targetTimeSeconds = targetTime.epochSeconds
                }
            }
        }
    }

    override suspend fun clearTimer() {
        activeTimerStore.updateData {
            protoActiveTimerState {}
        }
    }

    private fun ProtoActiveTimerState.toDomain(): ActiveTimerData? {
        if (!hasActiveTimer()) return null
        return ActiveTimerData(
            targetTime = Instant.fromEpochSeconds(activeTimer.targetTimeSeconds),
        )
    }
}
