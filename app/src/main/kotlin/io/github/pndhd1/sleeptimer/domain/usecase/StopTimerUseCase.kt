package io.github.pndhd1.sleeptimer.domain.usecase

import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository

@Inject
class StopTimerUseCase(
    private val activeTimerRepository: ActiveTimerRepository,
) {

    suspend operator fun invoke() {
        activeTimerRepository.clearTimer()
    }
}
