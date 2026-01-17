package io.github.pndhd1.sleeptimer.domain.usecase

import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import io.github.pndhd1.sleeptimer.domain.repository.SystemRepository

@Inject
class CompleteTimerUseCase(
    private val systemRepository: SystemRepository,
    private val activeTimerRepository: ActiveTimerRepository,
) {

    suspend operator fun invoke() {
        systemRepository.lockScreen()
        activeTimerRepository.clearTimer()
    }
}
