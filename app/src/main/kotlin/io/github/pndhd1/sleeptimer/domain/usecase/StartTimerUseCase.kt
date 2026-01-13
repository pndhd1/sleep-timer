package io.github.pndhd1.sleeptimer.domain.usecase

import dev.zacsweers.metro.Inject
import io.github.pndhd1.sleeptimer.domain.repository.ActiveTimerRepository
import io.github.pndhd1.sleeptimer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Duration
import kotlin.time.Instant

@Inject
class StartTimerUseCase(
    private val activeTimerRepository: ActiveTimerRepository,
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke(targetTime: Instant, totalDuration: Duration) {
        val settings = settingsRepository.timerSettings.first()
        activeTimerRepository.startTimer(
            targetTime = targetTime,
            totalDuration = totalDuration,
            fadeOutSettings = settings.fadeOut,
        )
    }
}
