package io.github.pndhd1.sleeptimer.domain.usecase

import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import io.github.pndhd1.sleeptimer.domain.repository.SettingsRepository
import io.github.pndhd1.sleeptimer.domain.repository.SystemRepository
import kotlinx.coroutines.flow.first

@Inject
class CompleteTimerUseCase(
    private val systemRepository: SystemRepository,
    private val activeTimerRepository: ActiveTimerRepository,
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke() {
        val settings = settingsRepository.timerSettings.first()
        if (settings.goHomeOnExpire) {
            systemRepository.goHome()
        }
        systemRepository.lockScreen()
        activeTimerRepository.clearTimer()
    }
}
