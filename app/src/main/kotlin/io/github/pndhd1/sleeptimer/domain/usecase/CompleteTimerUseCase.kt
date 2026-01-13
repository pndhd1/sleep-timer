package io.github.pndhd1.sleeptimer.domain.usecase

import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import io.github.pndhd1.sleeptimer.domain.repository.DeviceAdminRepository

@Inject
class CompleteTimerUseCase(
    private val deviceAdminRepository: DeviceAdminRepository,
    private val activeTimerRepository: ActiveTimerRepository,
) {

    suspend operator fun invoke() {
        deviceAdminRepository.lockScreen()
        activeTimerRepository.clearTimer()
    }
}
